package com.ir.crawl;

/* Import list */
import java.net.URL;
import org.jsoup.Jsoup;
import java.util.HashSet;
import java.util.HashMap;
import java.io.FileWriter;
import java.io.IOException;
import com.ir.global.Utils;
import org.jsoup.Connection;
import java.io.BufferedReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import com.ir.global.Properties;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 22, 2015
 */

public class Crawler
{
    /* Static data members */
    private static BufferedReader br;
    private static FileWriter fw;
    private static Pattern domainPattern;
    private static Pattern canonicalPattern;

    /* Non-static data members */
    private Matcher matcher;
    private HashMap<String, HashSet<String>> inLinks;
    private HashMap<String, HashSet<String>> outLinks;

    static
    {
        domainPattern    = Pattern.compile(Properties.REGEX_DOMAIN);
        canonicalPattern = Pattern.compile(Properties.REGEX_CANONICAL);
    }

    /**
     * Constructor.
     */
    public Crawler()
    {
        inLinks  = new HashMap<String, HashSet<String>>();
        outLinks = new HashMap<String, HashSet<String>>();
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
     * Canonizalize a URL.
     * @param url
     *            The URL to be canonicalized.
     * @return
     *            The canonicalized URL.
     */
    public String canonicalizeUrl(String url, String domain)
    {
        /* Extract the URL part. */
        matcher = canonicalPattern.matcher(url);
        if(matcher.find())
        {
            url = matcher.group(1);
        }

        /* Convert the scheme and host to lower case. */
        matcher = domainPattern.matcher(url);
        String host = null;
        if(matcher.find())
        {
            host = matcher.group(0).toLowerCase();
            url  = url.replace(matcher.group(0), "");
        }

        /* Remove port 80 from HTTP URLs and port 443 from HTTPS URLs. */
        if(host.contains("http:") && host.contains(":80"))
        {
            host = host.replace(":80", "");
        }
        if(host.contains("https:") && host.contains(":443"))
        {
            host = host.replace(":443", "");
        }

        /* Remove duplicate slashes. */
        while(url.charAt(0) == '/')
        {
            url = url.substring(1);
        }
        url = url.replaceAll("/{2,}", "/");

        /* Remove the URL part corresponding to a section. */
        url = url.replaceAll(Properties.REGEX_SECTION, "");

        /* Make relative URLs absolute. */
        if(host == null)
        {
            host = domain;
        }

        return host + url;
    }

    /**
     * Extract the web page's title.
     * @param s
     *            The string containing a web page's title.
     * @return
     *            The web page's title.
     */
    public String title(String s)
    {
        return s.replace("<title>", "").replace("</title>", "");
    }

    /**
     * Parse robots.txt file to create a set of restricted URLs.
     * @param domain
     *            The domain whose robots file is to be parsed.
     * @return
     *            The set of restricted URLs.
     */
    public HashSet<String> robotExclusionSet(String domain)
    {
        HashSet<String> robotExclusionSet = new HashSet<String>();
        URL robotsURL = null;

        try
        {
            robotsURL = new URL(domain + "robots.txt");
            br = new BufferedReader(new InputStreamReader(robotsURL.openStream()));
            String line = "";
            while((line = br.readLine()) != null)
            {
                if(line.contains("Disallow:"))
                {
                    robotExclusionSet.add(line.substring(line.indexOf('/')).trim());
                }
            }
        }
        catch(MalformedURLException mue) {}
        catch(IOException ioe) {}

        return robotExclusionSet;
    }

    /**
     * Crawl web pages beginning from a seed.
     * @param url
     *            The seed URL.
     * @throws IOException
     */
    public void crawl(String url)
        throws IOException
    {
        /* Execute sanity check. */
        if(url == null || url.length() == 0)
        {
            return;
        }

        /* Initialize the data structures. */
        Connection conn = null;
        Document doc = null;
        Map map = new Map();
        HashSet<String> robotExclusionSet = null;
        url = canonicalizeUrl(url, "");
        map.add(url, 0);
        inLinks.put(url, new HashSet<String>());
        outLinks.put(url, new HashSet<String>());

        /* Crux of breadth-first search. */
        while(map.size() > 0)
        {
            url  = map.remove().getUrl();
            conn = Jsoup.connect(url);
            doc  = conn.get();
            robotExclusionSet = robotExclusionSet(domain(url));

            for(Element elem : doc.select("a[href]"))
            {
                String curUrl = canonicalizeUrl(elem.toString(), domain(url));
                /* Update data structures for the parent URL. */
                outLinks.get(url).add(curUrl);

                /* Create and update data structures for the child URL. */
                if(!inLinks.containsKey(curUrl))
                {
                    inLinks.put(curUrl, new HashSet<String>());
                    outLinks.put(curUrl, new HashSet<String>());
                }
                inLinks.get(curUrl).add(url);
                if(!robotExclusionSet.contains(elem.toString()))
                {
                    map.update(curUrl);
                }
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
        Utils.cout("\n=======");
        Utils.cout("\nCRAWLER");
        Utils.cout("\n=======");
        Utils.cout("\n");

        Crawler c = new Crawler();
        Pattern pattern = Pattern.compile("href=\"(?=(?:([^\"]+)))");
        Matcher matcher = null;
        String url = "http://www.northeastern.edu/";

        Utils.cout(">Domains and Canonical URLs\n");
        Utils.cout(c.domain("https://www.facebook.com/people/leosFacemash") + "\n");
        Utils.cout(c.canonicalizeUrl("HTTPS://www.facebook.COM:443///people////leosFacemash#about",
                "http://www.dmoz.org/") + "\n");

        /* Test for various regular expressions. */
        /*
        try
        {
            Connection conn = Jsoup.connect(url);
            Document doc = conn.get();

            Utils.cout("\n>Domain\n");
            String domain = c.domain(url);
            Utils.cout(domain + "\n");

            Utils.cout("\n>Metadata\n");
            Utils.cout(conn.execute().contentType() + "\n");

            Utils.cout("\n>Robots file\n");
            for (String s : c.robotExclusionSet(domain))
            {
                Utils.cout(s + "\n");
            }

            Utils.cout("\n>Page titles\n");
            Utils.cout(doc.title() + "\n");

            Utils.cout("\n>Out links\n");
            for(Element elem : doc.select("a[href]"))
            {
                matcher = pattern.matcher(elem.toString());
                if(matcher.find())
                {
                    Utils.cout(c.canonicalizeUrl(c.title(url), matcher.group(1)) + "\n");
                }
            }

            Utils.cout("\n>Text\n");
            Utils.cout(doc.body().text());
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        */
    }
}
/* End of Crawler.java */