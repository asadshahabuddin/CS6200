package com.ir.crawl;

/* Import list */
import java.net.URL;
import org.jsoup.Jsoup;
import java.util.HashSet;
import java.util.HashMap;
import java.io.FileWriter;
import java.io.IOException;
import com.ir.global.Utils;
import java.util.ArrayList;
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
    private static FileWriter docWriter;
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
            docWriter     = new FileWriter(Properties.DIR_CRAWL + "/as" + docCount, true);
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
     *            The URL.
     * @return
     *            The equivalent canonical URL.
     */
    public String canonicalUrl(String urlStr, String parentDomain)
    {
        if(urlStr.contains("href=\"\""))
        {
            return null;
        }

        String curDomain = null;
        String path = null;
        try
        {
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
            if(urlStr.contains("http://") || urlStr.contains("https://"))
            {
                URL url = new URL(urlStr);
                curDomain = canonicalDomain(url);
                path = url.getPath();
                /* Return if the path corresponds to root. */
                if(path.equals("/") || path.equals(""))
                {
                    return curDomain;
                }
            }
            /* (3) Resolve relative URLs of the form '../x.html'. */
            else
            {
                path = urlStr;
                if(path.charAt(0) == '/')
                {
                    parentDomain = canonicalDomain(new URL(parentDomain));
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
            (4) Remove '/index.*'.
            (5) Remove duplicate slashes.
            (6) Remove section URL.
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
        }
        catch(IOException ioe)
        {
            Utils.error("Malformed URL passed to canonicalUrl(...)");
            Utils.cout(">Stack trace\n");
            ioe.printStackTrace();
        }

        return curDomain + path;
    }

    /**
     * Check if the URL may be crawled by robots.
     * @param urlStr
     *            The URL.
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
                                   .execute(new HttpGet(domain + "/" + Properties.FILE_ROBOTS),
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
                        Properties.AGENT_MOZILLA);
                }
                disallowedLinks.put(domain, rules);
            }
        }
        catch(IOException ioe)
        {
            Utils.error("Malformed URL or negative response in isRobotAllowed(...)");
            Utils.cout(">Stack trace\n");
            ioe.printStackTrace();
        }

        if(rules == null)
        {
            return true;
        }
        return rules.isAllowed(urlStr);
    }

    /**
     * Write the contents to the file system.
     * @param docNo
     *            The URL.
     * @param doc
     *            The document object.
     * @throws IOException
     */
    public void writeDocument(String docNo, Document doc)
    {
        try
        {
            Utils.echo("Crawled item " + docCount);
            /* Create a space-separated list of all the out-links. */
            StringBuilder sb = new StringBuilder();
            for(String s : outLinks.get(docNo))
            {
                sb.append(s + " ");
            }

            /* Write to the file. */
            docWriter.write("<DOC>\n"    +
                            "<DOCNO>"    + docNo                  + "</DOCNO>\n" +
                            "<HEAD>"     + doc.title().trim()     + "</HEAD>\n" +
                            "<OUTLINKS>" + sb.toString().trim()   + "</OUTLINKS>\n" +
                            "<TEXT>\n" + doc.body().text().trim() + "\n</TEXT>\n" +
                            "</DOC>\n");

            /* Create a new file after every 100 web pages. */
            if(++docCount % 100 == 1)
            {
                docWriter.close();
                docWriter = new FileWriter(Properties.DIR_CRAWL + "/as" + docCount, true);
            }
        }
        catch(IOException ioe)
        {
            Utils.error("Write to file failed in writeDocument(...)");
            Utils.cout(">Stack trace\n");
            ioe.printStackTrace();
        }
    }

    /**
     * Write the in-link graph to the file system.
     */
    public void writeGraph()
        throws IOException
    {
        FileWriter graphWriter = new FileWriter(Properties.FILE_GRAPH, true);
        for(String url : inLinks.keySet())
        {
            StringBuilder sb = new StringBuilder(url + " ");
            for(String s : inLinks.get(url))
            {
                sb.append(s + " ");
            }
            graphWriter.write(sb.toString().trim() + "\n");
        }
        graphWriter.close();
    }

    /**
     * Crawl web pages beginning from the list of seed URLs.
     * @param urls
     *            The seed URLs.
     * @throws IOException
     */
    public void crawl(ArrayList<String> urls)
    {
        if(urls == null || urls.size() == 0)
        {
            return;
        }

        Response res;
        Document doc;
        Map map = new Map();
        Map newMap = new Map();

        for(String url : urls)
        {
            url = canonicalUrl(url, "");
            map.add(url, 0);
            inLinks.put(url, new HashSet<String>());
            outLinks.put(url, new HashSet<String>());
        }

        /* Implement breadth-first search. */
        while(map.size() > 0 && docCount <= 20000)
        {
            String url = null;
            try
            {
                url = map.remove().getUrl();
                if(!isRobotAllowed(url))
                {
                    continue;
                }

                Thread.sleep(1000);
                res = Jsoup.connect(url).userAgent(Properties.AGENT_MOZILLA).timeout(2000).execute();
                /* Process only HTML pages. */
                if(res == null || !res.contentType().contains("text/html"))
                {
                    continue;
                }

                Utils.echo("Crawling " + url);
                doc = res.parse();
                for(Element e : doc.select("a[href]"))
                {
                    if(e.toString().contains("javascript"))
                    {
                        continue;
                    }

                    String newUrl = canonicalUrl(e.toString(), url);
                    if(newUrl != null)
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
                        if(map.containsKey(newUrl))
                        {
                            map.update(newUrl);
                        }
                        newMap.update(newUrl);
                    }
                }
                /* Add crawled contents to the document collection. */
                writeDocument(url, doc);

                /* Move to the next level if the current level has been completely
                processed. */
                if(map.size() == 0)
                {
                    map.getMap().putAll(newMap.getMap());
                    newMap.clear();
                }
            }
            catch(InterruptedException intre)
            {
                Utils.echo("Ignoring " + url);
                Utils.error("INTR Error in crawl(...)");
                Utils.cout(">Stack trace\n");
                intre.printStackTrace();
            }
            catch(IOException ioe)
            {
                Utils.echo("Ignoring " + url);
                Utils.error("I/O Error in crawl(...)");
                Utils.cout(">Stack trace\n");
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Main method for unit testing.
     * @param args
     *            Program arguments.
     */
    public static void main(String[] args)
    {
        /* Calculate start time */
        long startTime = System.nanoTime();
        Utils.cout("\n=======");
        Utils.cout("\nCRAWLER");
        Utils.cout("\n=======");
        Utils.cout("\n");

        try
        {
            Crawler c = new Crawler();
            ArrayList<String> urls = new ArrayList<>();

            /* Add seeds URLs. */
            urls.add("http://en.wikipedia.org/wiki/History_of_Apple_Inc.");
            urls.add("http://en.wikipedia.org/wiki/OS_X_Yosemite");
            urls.add("http://en.wikipedia.org/wiki/IOS");
            // urls.clear();
            // urls.add("http://www.dmoz.org/");

            /* Crawl the internet. */
            c.crawl(urls);
            c.writeGraph();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        finally
        {
            Utils.elapsedTime(startTime, "\nCrawling of web pages completed.");
        }
    }
}
/* End of Crawler.java */