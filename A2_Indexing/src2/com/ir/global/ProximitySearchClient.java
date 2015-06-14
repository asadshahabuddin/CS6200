package com.ir.global;

/* Import list */
import java.util.*;
import com.ir.index.Entry;
import java.io.IOException;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 13, 2015
 */

public class ProximitySearchClient
{
    public void search(String query, ArrayList<String> docs)
            throws IOException
    {
        String[] terms = query.split("\\s+|-");
        for(String doc : docs)
        {
            for(int i = 0; i < terms.length; i++)
            {
                HashMap<String, Entry> termEntryMap = SearchClient.queryTerm(terms[i]);

            }
        }
    }

    /**
     * Calculate the minimum proximity between terms based on their
     * offsets.
     * @param offs
     *            The offsets pertaining to a number of terms.
     * @return
     *            The minimum proximity between the terms.
     */
    public static long minProximity(ArrayList<Long>[] offs)
    {
        long proximity = 0;
        long minProximity = Integer.MAX_VALUE;
        int[] idx = new int[offs.length];
        TreeMap<Long, Integer> map = new TreeMap<Long, Integer>();
        boolean hasMoreElements = true;

        while(hasMoreElements)
        {
            hasMoreElements = false;
            /* Create a tree map of the offsets for various terms based
            on their current indices. */
            for(int i = 0; i < offs.length; i++)
            {
                map.put(offs[i].get(idx[i]), i);
            }

            /* Update the minimum proximity between all the terms. */
            proximity = map.lastKey() - map.firstKey();
            if(proximity < minProximity)
            {
                minProximity = proximity;
            }

            /* Update the current index for the list with the minimum
            offset value at its current index. */
            for(Map.Entry<Long, Integer> entry : map.entrySet())
            {
                int val = entry.getValue();
                if(idx[val] < offs[val].size() - 1)
                {
                    idx[val]++;
                    hasMoreElements = true;
                    break;
                }
            }
            map.clear();
        }

        return minProximity;
    }

    public static void main(String[] args)
    {
        ArrayList<Long> list1 = new ArrayList<Long>();
        ArrayList<Long> list2 = new ArrayList<Long>();
        ArrayList<Long> list3 = new ArrayList<Long>();

        list1.add(new Long(1));
        list1.add(new Long(5));
        list1.add(new Long(9));
        list1.add(new Long(13));

        list2.add(new Long(2));
        list2.add(new Long(6));
        list2.add(new Long(14));

        list3.add(new Long(3));
        list3.add(new Long(4));
        list3.add(new Long(7));
        list3.add(new Long(8));

        Utils.cout(minProximity(new ArrayList[] {list1, list2, list3}));
    }
}
/* End of ProximitySearchClient.java */