package com.ir.crawl;

/* Import list */
import java.net.URL;
import org.jsoup.Jsoup;
import java.util.HashSet;
import java.util.HashMap;
import java.io.FileWriter;
import java.io.IOException;
import com.ir.global.Utils;
import java.io.BufferedReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import com.ir.global.Properties;
import org.apache.http.HttpResponse;
import org.apache.commons.io.IOUtils;
import org.jsoup.Connection.Response;
import org.apache.http.util.EntityUtils;
import crawlercommons.robots.BaseRobotRules;
import org.apache.http.client.methods.HttpGet;
import crawlercommons.robots.SimpleRobotRules;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.entity.BufferedHttpEntity;
import crawlercommons.robots.SimpleRobotRulesParser;
import org.apache.http.impl.client.DefaultHttpClient;
import crawlercommons.robots.SimpleRobotRules.RobotRulesMode;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 22, 2015
 */

public class Crawler
{
    /* Static data members */
    private static int docCount;
    private static BufferedReader br;
    private static FileWriter fw;
    private static Pattern urlPattern;
    private static Pattern domainPattern;
    private static SimpleRobotRulesParser parser;

    /* Non-static data members */
    private Matcher matcher;
    private HashMap<String, HashSet<String>> inLinks;
    private HashMap<String, HashSet<String>> outLinks;
    private HashMap<String, BaseRobotRules> disallowedLinks;

    static
    {
        try
        {
            docCount      = 1;
            fw            = new FileWriter(Properties.DIR_CRAWL + "/as" + docCount, true);
            urlPattern    = Pattern.compile(Properties.REGEX_URL);
            domainPattern = Pattern.compile(Properties.REGEX_DOMAIN);
            parser        = new SimpleRobotRulesParser();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * Constructor.
     */
    public Crawler()
    {
        inLinks         = new HashMap<>();
        outLinks        = new HashMap<>();
        disallowedLinks = new HashMap<>();
    }

    /**
     * Create the equivalent canonical domain.
     * @param url
     *            The URL object.
     * @return
     *            The equivalent canonical domain.
     */
    public String canonicalDomain(URL url)
    {
        int port = url.getPort();
        if((url.getProtocol().equals("http")  && port == 80) ||
           (url.getProtocol().equals("https") && port == 443))
        {
            port = -1;
        }
        return (url.getProtocol() + "://" +
                url.getHost()     +
                (port == -1 ? "" : ":" + port)).toLowerCase();
    }

    /**
     * Create the equivalent canonical URL.
     * @param urlStr
     *            The string containing a URL.
     * @return
     *            The equivalent canonical URL.
     */
    public String canonicalUrl(String urlStr, String parentDomain)
    {
        /* Sanity check */
        if(urlStr.contains("href=\"\""))
        {
            return null;
        }

        /* Extract the URL from the string. */
        matcher = urlPattern.matcher(urlStr);
        if(matcher.find())
        {
            urlStr = matcher.group(1);
        }
        if(urlStr.startsWith("//"))
        {
            urlStr = "http:" + urlStr;
        }

        /*
        (1) Remove port 80 from HTTP URLs and port 443 from HTTPS URLs.
        (2) Convert the scheme and host to lower case.
        */
        URL url;
        String curDomain = null;
        String path = null;
        if(urlStr.contains("http://") || urlStr.contains("https://"))
        {
            try
            {
                url = new URL(urlStr);
                curDomain = canonicalDomain(url);
                path = url.getPath();
                /* Return if the path corresponds to root. */
                if(path.equals("/") || path.equals(""))
                {
                    return curDomain;
                }
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
        /* (3) Resolve relative URLs of the form '../x.html'. */
        else
        {
            path = urlStr;
            if(path.charAt(0) == '/')
            {
                try
                {
                    parentDomain = canonicalDomain(new URL(parentDomain));
                }
                catch(IOException ioe)
                {
                    ioe.printStackTrace();
                }
            }
            while(path.contains("../"))
            {
                int i = parentDomain.length() - 1;
                while(parentDomain.charAt(i--) != '/');
                while(parentDomain.charAt(i--) != '/');
                parentDomain = parentDomain.substring(0, i + 2);
                path = path.replaceFirst("../", "");
            }
            curDomain = parentDomain.replaceAll("/$", "");
        }

        /*
        (4) Remove '/index.htm(l)'.
        (5) Remove duplicate slashes.
        (6) Remove the URL part corresponding to a section.
        */
        if(path.equalsIgnoreCase("index.htm") ||
           path.equalsIgnoreCase("index.html"))
        {
            path = "/";
        }
        else
        {
            path = path.replaceAll("/{2,}", "/")
                       .replaceAll(Properties.REGEX_SECTION, "")
                       .replaceAll("/$", "");
        }

        /* (7) Make relative URLs absolute. */
        if(curDomain.charAt(curDomain.length() - 1) != '/' &&
           path.length() > 0 &&
           path.charAt(0) != '/')
        {
            path = "/" + path;
        }
        return curDomain + path;
    }

    /**
     * Get the URL's domain.
     * @param url
     *            The URL whose domain is to be extracted.
     * @return
     *            The URL's domain.
     */
    public String domain(String url)
    {
        matcher = domainPattern.matcher(url);
        if(matcher.find())
        {
            /* Convert the scheme and host to lower case. */
            return matcher.group(0).toLowerCase();
        }
        return null;
    }

    /**
     * Verify if the URL may be crawled by robots.
     * @param urlStr
     *            The URL string.
     * @return
     *            'true' iff the URL may be crawled by robots.
     * @throws IOException
     */
    public boolean isRobotAllowed(String urlStr)
    {
        BaseRobotRules rules = null;
        try
        {
            URL url = new URL(urlStr);
            String domain = url.getProtocol() + "://" +
                            url.getHost() +
                            (url.getPort() > -1 ? ":" + url.getPort() : "");
            rules = disallowedLinks.get(domain);

            if(rules == null)
            {
                HttpResponse res = new DefaultHttpClient()
                                   .execute(new HttpGet(domain + "/robots.txt"),
                                           new BasicHttpContext());
                if(res.getStatusLine().getStatusCode() == 404 &&
                   res.getStatusLine() != null)
                {
                    rules = new SimpleRobotRules(RobotRulesMode.ALLOW_ALL);
                    EntityUtils.consume(res.getEntity());
                }
                else
                {
                    rules = parser.parseContent(domain,
                            IOUtils.toByteArray(new BufferedHttpEntity(res.getEntity()).getContent()),
                            "text/plain",
                            "Mozilla 5.0");
                }
                disallowedLinks.put(domain, rules);
            }
        }
        catch(IOException ioe)
        {
            Utils.error("Ignoring " + urlStr);
            ioe.printStackTrace();
        }

        /* Fail safe */
        if(rules == null)
        {
            return true;
        }
        return rules.isAllowed(urlStr);
    }

    /**
     * Write the crawled web page's contents to the file system.
     * @param docNo
     *            The web page's URL.
     * @param doc
     *            The document object.
     * @throws IOException
     */
    public void write(String docNo, Document doc)
    {
        try
        {
            Utils.echo("Crawled item " + docCount);
            /* Create a space-separated list of all the outlinks. */
            StringBuilder sb = new StringBuilder();
            for(String s : outLinks.get(docNo))
            {
                sb.append(s + " ");
            }

            /* Write the in-link graph to the file system. */
            sb = new StringBuilder();
            for(String s : inLinks.get(docNo))
            {
                sb.append(s + " ");
            }

            /* Write the relevant content to the file system. */
            fw.write("<DOC>\n"    +
                     "<DOCNO>"    + docNo                  + "</DOCNO>\n" +
                     "<HEAD>"     + doc.title().trim()     + "</HEAD>\n" +
                     "<OUTLINKS>" + sb.toString().trim()   + "</OUTLINKS>\n" +
                     "<TEXT>\n" + doc.body().text().trim() + "\n</TEXT>\n" +
                     "</DOC>\n");


            /* Create a new file for every 100 web pages. */
            if(++docCount % 100 == 1)
            {
                fw.close();
                fw = new FileWriter(Properties.DIR_CRAWL + "/as" + docCount, true);
            }
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * Crawl web pages beginning from a seed.
     * @param url
     *            The seed URL.
     * @throws IOException
     */
    public void crawl(String url)
    {
        /* Execute the sanity check. */
        if(url == null || url.length() == 0)
        {
            return;
        }

        /* Declare and define the data structures. */
        Response res = null;
        Document doc;
        Map map = new Map();
        HashSet<String> disallowedSet;
        url = canonicalUrl(url, "");
        map.add(url, 0);
        map.add(url, 0);
        inLinks.put(url, new HashSet<String>());
        outLinks.put(url, new HashSet<String>());

        /* Crawl web pages using breadth-first search. */
        while(map.size() > 0 && docCount <= 200)
        {
            try
            {
                // Map newMap = new Map();
                url  = map.remove().getUrl();
                Thread.sleep(1000);
                res = Jsoup.connect(url).userAgent("Mozilla 5.0").timeout(6000).execute();
                /* Process HTML pages only. */
                if(res == null || !res.contentType().contains("text/html"))
                {
                    continue;
                }

                Utils.echo("Crawling " + url);
                doc = res.parse();
                /* Add crawled contents to the document collection. */
                write(url, doc);

                for(Element e : doc.select("a[href]"))
                {
                    String newUrl = canonicalUrl(e.toString(), url);
                    if(newUrl != null && isRobotAllowed(newUrl))
                    {
                        /* Update data structures for the parent URL. */
                        outLinks.get(url).add(newUrl);

                        /* Create and update data structures for the child URL. */
                        if (!inLinks.containsKey(newUrl))
                        {
                            inLinks.put(newUrl, new HashSet<String>());
                            outLinks.put(newUrl, new HashSet<String>());
                        }
                        inLinks.get(newUrl).add(url);
                        map.update(newUrl);
                    }
                }
                /*
                if(map.size() == 0)
                {
                    map = newMap;
                }
                */
            }
            catch(InterruptedException intre) { /* TODO */ }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
                Utils.error(ioe.getMessage());
                Utils.echo("Ignoring " + url);
            }
        }
    }

    /**
     * Main method for unit testing.
     * @param args
     *            Program arguments.
     */
    public static void main(String[] args)
        throws IOException
    {
        /* Calculate start time */
        long startTime = System.nanoTime();
        Utils.cout("\n=======");
        Utils.cout("\nCRAWLER");
        Utils.cout("\n=======");
        Utils.cout("\n");

        Crawler c = new Crawler();
        c.crawl("http://www.dmoz.org/");
        Utils.elapsedTime(startTime, "\nCrawling of web pages completed.");
    }
}
/* End of Crawler.java */