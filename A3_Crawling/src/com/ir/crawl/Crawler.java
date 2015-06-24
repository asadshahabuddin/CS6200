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
        inLinks  = new HashMap<>();
        outLinks = new HashMap<>();
    }

    /**
     * Create a canonical URL.
     * @param url
     *            The string containing a URL.
     * @return
     *            The canonical URL.
     */
    public String canonicalUrl(String url, String parentDomain)
    {
        /* Extract the URL from the string. */
        matcher = canonicalPattern.matcher(url);
        if(matcher.find())
        {
            url = matcher.group(1);
        }

        /* Convert the scheme and host to lower case. */
        matcher = domainPattern.matcher(url);
        String domain = null;
        if(matcher.find())
        {
            domain = matcher.group(0).toLowerCase();
            url    = url.replace(matcher.group(0), "");
        }

        /* Remove port 80 from HTTP URLs and port 443 from HTTPS URLs. */
        if(domain.contains("http:") && domain.contains(":80"))
        {
            domain = domain.replace(":80", "");
        }
        else if(domain.contains("https:") && domain.contains(":443"))
        {
            domain = domain.replace(":443", "");
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
        if(domain == null)
        {
            domain = parentDomain;
        }
        return domain + url;
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
     * Parse the robots file and create a set of disallowed URLs.
     * @param domain
     *            The domain whose robots file is to be parsed.
     * @return
     *            The set of disallowed URLs.
     */
    public HashSet<String> createDisallowedSet(String domain)
    {
        HashSet<String> set = new HashSet<>();
        try
        {
            URL url = new URL(domain + Properties.FILE_ROBOTS);
            br = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;

            while((line = br.readLine()) != null)
            {
                if(line.contains("Disallow:"))
                {
                    set.add(line.substring(line.indexOf('/')).trim());
                }
            }
        }
        catch(MalformedURLException mue) {}
        catch(IOException ioe) {}

        return set;
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
        /* Execute the sanity check. */
        if(url == null || url.length() == 0)
        {
            return;
        }

        /* Declare and define the data structures. */
        Connection conn;
        Document doc;
        Map map = new Map();
        HashSet<String> disallowedSet;
        url = canonicalUrl(url, "");
        map.add(url, 0);
        inLinks.put(url, new HashSet<String>());
        outLinks.put(url, new HashSet<String>());

        /* Crawl web pages using breadth-first search. */
        while(map.size() > 0)
        {
            url  = map.remove().getUrl();
            conn = Jsoup.connect(url);
            doc  = conn.get();
            disallowedSet = createDisallowedSet(domain(url));

            for(Element e : doc.select("a[href]"))
            {
                if(!disallowedSet.contains(e.toString()))
                {
                    String newUrl = canonicalUrl(e.toString(), domain(url));
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

        /* Test individual functions. */
        Utils.cout(">Domains and Canonical URLs\n");
        Utils.cout(c.domain("https://www.facebook.com/people/leosFacemash") + "\n");
        Utils.cout(c.canonicalUrl("HTTPS://www.facebook.COM:443///people////leosFacemash#about",
                                  "http://www.dmoz.org/") + "\n");
    }
}
/* End of Crawler.java */