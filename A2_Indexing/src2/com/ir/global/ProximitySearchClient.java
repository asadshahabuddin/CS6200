package com.ir.global;

/* Import list */
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import com.ir.index.Entry;
import java.io.IOException;
import java.util.ArrayList;
import com.ir.model.Formulae;

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
        ArrayList<ArrayList<Long>> offs = new ArrayList<ArrayList<Long>>();

        for(String doc : docs)
        {
            for(int i = 0; i < terms.length; i++)
            {
                HashMap<String, Entry> termEntryMap = SearchClient.queryTerm(terms[i], doc);
                if(termEntryMap.size() != 0)
                {
                    offs.add(termEntryMap.get(doc).getOffs());
                }
            }
            Formulae.proximitySearch(minProximity(offs), offs.size());
            offs.clear();
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
    public static long minProximity(ArrayList<ArrayList<Long>> offs)
    {
        long proximity = 0;
        long minProximity = Integer.MAX_VALUE;
        int[] idx = new int[offs.size()];
        TreeMap<Long, Integer> map = new TreeMap<Long, Integer>();
        boolean hasMoreElements = true;

        while(hasMoreElements)
        {
            hasMoreElements = false;
            /* Create a tree map of the offsets for various terms based
            on their current indices. */
            for(int i = 0; i < offs.size(); i++)
            {
                map.put(offs.get(i).get(idx[i]), i);
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
                if(idx[val] < offs.get(val).size() - 1)
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
        ArrayList<ArrayList<Long>> offs = new ArrayList<ArrayList<Long>>();
        ArrayList<Long> list1 = new ArrayList<Long>();
        ArrayList<Long> list2 = new ArrayList<Long>();
        ArrayList<Long> list3 = new ArrayList<Long>();
        offs.add(list1);
        offs.add(list2);
        offs.add(list3);

        list1.add(new Long(0));
        list1.add(new Long(5));
        list1.add(new Long(10));
        list1.add(new Long(15));

        list2.add(new Long(1));
        list2.add(new Long(3));
        list2.add(new Long(6));
        list2.add(new Long(9));

        list3.add(new Long(4));
        list3.add(new Long(8));
        list3.add(new Long(16));
        list3.add(new Long(21));

        Utils.cout(minProximity(offs));
    }
}
/* End of ProximitySearchClient.java */