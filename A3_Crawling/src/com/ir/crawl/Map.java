package com.ir.crawl;

/* Import list */
import java.util.LinkedHashMap;
import com.ir.global.Properties;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 23, 2015
 */

public class Map
{
    private LinkedHashMap<String, Integer> map;

    /**
     * Constructor
     */
    public Map()
    {
        map = new LinkedHashMap<String, Integer>();
    }

    /**
     * Add a URL and its in-link count to the Frontier.
     * @param url
     *            The web page's URL.
     * @param inLinkCount
     *            The web page's in-link count.
     * @return
     *            'true' iff the key-value pair was added successfully.
     */
    public boolean add(String url, Integer inLinkCount)
    {
        if(url == null || url.length() == 0)
        {
            return false;
        }
        map.put(url, inLinkCount);
        return true;
    }

    /**
     * Update the Frontier for the given URL.
     * @param url
     *            The web page's URL.
     * @return
     *            'true' if the in-link count for the URL is updated
     *            successfully.
     */
    public boolean update(String url)
    {
        /* Add to the map if the key is not present. */
        if(!map.containsKey(url))
        {
            return add(url, 1);
        }
        /* Don't add to the map if the key has previously been removed. */
        if(map.get(url) == Properties.FLAG_REMOVED)
        {
            return false;
        }
        /* If none of the above condition are met, add to the map and return. */
        map.put(url, map.get(url) + 1);
        return true;
    }

    /**
     * Fetch the Frontier object with the maximum in-link count.
     * @return
     *            The Frontier object with the maximum in-link count.
     */
    public Frontier remove()
    {
        if(map.size() == 0)
        {
            return null;
        }

        /* Flag and return the canonical Frontier. */
        String maxKey = "";
        int maxValue = Integer.MIN_VALUE;
        for(String key : map.keySet())
        {
            if(map.get(key) > maxValue)
            {
                maxKey   = key;
                maxValue = map.get(key);
            }
        }
        map.put(maxKey, Properties.FLAG_REMOVED);
        return new Frontier(maxKey, maxValue);
    }

    /**
     * Get the map's size.
     * @return
     *            The map's size.
     */
    public int size()
    {
        return map.size();
    }

    /**
     * Main method for unit testing.
     * @param args
     *            Program arguments.
     */
    public static void main(String[] args)
    {
        Map map = new Map();

        /* Add entries to the map. */
        map.add("a", 2);
        map.add("b", 4);
        map.add("c", 1);
        map.add("d", 2);
        map.add("e", 2);
        map.add("f", 0);

        /* Iterate over the map. */
        Frontier f = null;
        while((f = map.remove()) != null)
        {
            System.out.println("URL: "           + f.getUrl());
            System.out.println("In-link count: " + f.getInLinkCount());
        }
    }
}
/* End of Map.java */