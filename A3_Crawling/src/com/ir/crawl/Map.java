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

    /* Inner class */
    class Tuple
    {
        String url;
        boolean present;

        /**
         * Inner class' constructor.
         * @param url
         *           A string.
         * @param present
         *           A boolean variable.
         */
        Tuple(String url, boolean present)
        {
            this.url = url;
            this.present = present;
        }
    }

    static
    {
        visited = new HashSet<>();
    }

    /**
     * Constructor.
     */
    public Map()
    {
        map     = new LinkedHashMap<>();
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
     * Check if the object contains the URL.
     * @param obj
     *            The object.
     * @param url
     *            The URL.
     * @return
     *           'true' iff the object contains the URL.
     */
    public boolean contains(Object obj, String url)
    {
        if(obj instanceof HashSet)
        {
            return ((HashSet) obj).contains(url);
        }
        else
        {
            return ((LinkedHashMap) obj).containsKey(url);
        }
    }

    /**
     * Check if the object contains any form of the URL.
     * @param obj
     *            The object.
     * @param url
     *            The URL.
     * @return
     *            'true' iff the object contains a form of the URL.
     */
    public Tuple containsForm(Object obj, String url)
    {
        String minUrl = url.replaceAll(Properties.REGEX_MINURL, "");
        boolean present = false;

        if(contains(obj, "http://" + minUrl))
        {
            url     = "http://" + minUrl;
            present = true;
        }
        else if(contains(obj, "https://" + minUrl))
        {
            url     = "https://" + minUrl;
            present = true;
        }
        else if(contains(obj, "http://www." + minUrl))
        {
            url     = "http://www." + minUrl;
            present = true;
        }
        else if(contains(obj, "https://www." + minUrl))
        {
            url     = "https://www." + minUrl;
            present = true;
        }

        return new Tuple(url, present);
    }

    /**
     * Check if the map contains the URL.
     * @param url
     *            The URL.
     * @return
     *            'true' iff the map contains the URL.
     */
    public boolean containsKey(String url)
    {
        return containsForm(map, url).present;
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
        Tuple t1 = containsForm(map, url);
        Tuple t2 = containsForm(visited, url);

        /* URL is not present and hasn't been visited. */
        if(!t1.present && !t2.present)
        {
            return add(url, 1);
        }
        /* URL has been visited. */
        else if(t2.present)
        {
            return false;
        }

        /* If none of the above condition are met, update the map. */
        map.put(t1.url, inLinkCount);
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