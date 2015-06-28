package com.ir.crawl;

/* Import list */
import java.util.HashSet;
import java.util.LinkedHashMap;
import com.ir.global.Properties;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 23, 2015
 */

public class Map
{
    /* Static data members. */
    private static HashSet<String> visited;
    /* Non-static data members. */
    private LinkedHashMap<String, Integer> map;

    static
    {
        visited = new HashSet<>();
    }

    /**
     * Constructor.
     */
    public Map()
    {
        map = new LinkedHashMap<>();
    }

    /**
     * Mark the URL as visited.
     * @param url
     *            The URL.
     * @return
     *            'true' iff the set did not already contain the URL.
     */
    public static boolean visit(String url)
    {
        return visited.add(url);
    }

    /**
     * Mark the URL as not visited.
     * @param url
     *            The URL.
     * @return
     *            'true' if the set contained the specified element.
     */
    public static boolean unvisit(String url)
    {
        return visited.remove(url);
    }

    /**
     * Check if the URL has been visited.
     * @param url
     *            The URL.
     * @return
     *            'true' iff the set contains the URL.
     */
    public static boolean visited(String url)
    {
        return visited.contains(url);
    }

    /**
     * Get the internal map.
     * @return
     *            The internal map.
     */
    public LinkedHashMap<String, Integer> getMap()
    {
        return map;
    }

    /**
     * Check if the map contains the key.
     * @param url
     *            The URL.
     * @return
     *            'true' iff the map contains the URL.
     */
    public boolean containsKey(String url)
    {
        return map.containsKey(url);
    }

    /**
     * Add a URL and its in-link count to the map.
     * @param url
     *            The URL.
     * @param inLinkCount
     *            The in-link count.
     * @return
     *            'true' iff the key-value pair is added successfully.
     */
    public boolean add(String url, Integer inLinkCount)
    {
        /* Do not add entries belonging to the list of restricted domains. */
        for(String restrictedDomain : Properties.restrictedDomains)
        {
            if(url.contains(restrictedDomain))
            {
                return false;
            }
        }

        map.put(url, inLinkCount);
        return true;
    }

    /**
     * Update the entry corresponding to the URL.
     * @param url
     *            The URL.
     * @return
     *            'true' if the map is updated successfully.
     */
    public boolean update(String url, int inLinkCount)
    {
        /* URL is not present and hasn't been visited. */
        if(!map.containsKey(url) && !visited.contains(url))
        {
            return add(url, 1);
        }
        /* URL has been visited. */
        else if(visited.contains(url))
        {
            return false;
        }

        /* If none of the above condition are met, update the map. */
        map.put(url, inLinkCount);
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

        /* Mark the canonical Frontier as visited and return it. */
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
        map.remove(maxKey);
        visited.add(maxKey);
        return new Frontier(maxKey, maxValue);
    }

    /**
     * Clear the map.
     */
    public void clear()
    {
        map.clear();
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
}
/* End of Map.java */