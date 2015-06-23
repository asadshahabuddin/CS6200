package com.ir.crawl;

/* Import list */
import org.jsoup.Jsoup;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;
import com.ir.global.Utils;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import com.ir.global.Properties;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 22, 2015
 */

public class Crawler
{
    /* Static data members */
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
        /* Remove port 80 from HTTP URLs and port 443 from HTTPS URLs. */
        if(url.contains("http:") && url.contains(":80"))
        {
            url = url.replace(":80", "/");
        }
        if(url.contains("https:") && url.contains(":443"))
        {
            url = url.replace(":443", "/");
        }

        matcher = domainPattern.matcher(url);
        if(matcher.find())
        {
            /* Convert the scheme and host to lower case. */
            return matcher.group(1).toLowerCase();
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
    public String canonicalizeUrl(String domain, String url)
    {
        /* Extract the URL part. */
        matcher = canonicalPattern.matcher(url);
        if(matcher.find())
        {
            url = matcher.group(2);
        }

        /* Remove duplicate slashes. */
        url.replaceAll("//", "/");

        /* Remove the URL part corresponding to a section. */
        url.replace("#\\S+$", "");

        /* Make relative URLs absolute. */
        if(url.charAt(0) == '/')
        {
            url = domain + url;
        }

        return "";
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
        ArrayList<String> urls = new ArrayList<String>();
        Queue q = new Queue();
        urls.add(url);
        q.add(new Frontier(url, 0));
        inLinks.put(url, new HashSet<String>());
        outLinks.put(url, new HashSet<String>());

        /* Crux of breadth-first search. */
        while(urls.size() > 0)
        {
            url = urls.remove(0);
            q.add(new Frontier(url, inLinks.get(url).size()));
            Document doc = Jsoup.connect(url).get();

            for(Element elem : doc.select("a[href]"))
            {
                /* Update data structures for the parent URL. */
                outLinks.get(url).add(elem.toString());

                /* Create and update data structures for the child URL. */
                if(!inLinks.containsKey(elem.toString()))
                {
                    urls.add(elem.toString());
                    inLinks.put(elem.toString(), new HashSet<String>());
                    outLinks.put(elem.toString(), new HashSet<String>());
                }
                inLinks.get(elem.toString()).add(url);
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
        Utils.cout(new Crawler().domain("https://www.facebook.com:443"));

        /* Test for various regular expressions. */
        /*
        try
        {
            Pattern pattern = Pattern.compile("href=\"(?=((?:([^\"]+)\")))");
            Matcher matcher = null;
            Document doc = Jsoup.connect("http://www.dmoz.org/").get();

            for(Element elem : doc.select("a[href]"))
            {
                matcher = pattern.matcher(elem.toString());
                if(matcher.find())
                {
                    Utils.cout(matcher.group(2) + "\n");
                }
            }
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        */
    }
}
/* End of Crawler.java */