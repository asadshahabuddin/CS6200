package com.ir.crawl;

/* Import list */
import org.jsoup.Jsoup;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 22, 2015
 */

public class Crawler
{
    private HashMap<String, HashSet<String>> inLinks;
    private HashMap<String, HashSet<String>> outLinks;

    /**
     * Constructor.
     */
    public Crawler()
    {
        inLinks  = new HashMap<String, HashSet<String>>();
        outLinks = new HashMap<String, HashSet<String>>();
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
        inLinks.put(url, new HashSet<String>());
        outLinks.put(url, new HashSet<String>());

        /* Crux of breadth-first search. */
        while(urls.size() > 0)
        {
            String currUrl = urls.remove(0);
            Document doc = Jsoup.connect(currUrl).get();
            for(Element elem : doc.select("a[href]"))
            {
                if(!inLinks.containsKey(elem.toString()))
                {
                    urls.add(elem.toString());
                    inLinks.put(url.toString(), new HashSet<String>());
                    outLinks.put(url.toString(), new HashSet<String>());
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
        // TODO
    }
}
/* End of Crawler.java */