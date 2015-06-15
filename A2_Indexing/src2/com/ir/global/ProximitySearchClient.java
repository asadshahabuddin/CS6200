package com.ir.global;

/* Import list */
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.TreeMap;
import java.io.FileReader;
import java.io.FileWriter;
import com.ir.index.Entry;
import com.ir.model.Queue;
import java.util.ArrayList;
import java.io.IOException;
import com.ir.model.Formulae;
import java.io.BufferedReader;
import com.ir.token.Tokenizer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.ir.model.DocScorePair;
import org.tartarus.snowball.ext.PorterStemmer;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 13, 2015
 */

public class ProximitySearchClient
{
    /* Static data members */
    private static PorterStemmer stemmer;
    private static Pattern pattern;
    private static HashSet<String> stopSet;
    private static HashMap<String, Integer> docLenMap;
    private static HashMap<String, Entry> termFreqMap;
    private static HashMap<String, ArrayList<ArrayList<Long>>> proxSearchMap;
    private static Queue proxsearchq;

    static
    {
        try
        {
            stemmer       = new PorterStemmer();
            pattern       = Pattern.compile(Properties.REGEX_TOKEN);
            stopSet       = Utils.createStopSet();
            docLenMap     = Tokenizer.getDocLenMap();
            termFreqMap   = new HashMap<String, Entry>();
            proxSearchMap = new HashMap<String, ArrayList<ArrayList<Long>>>();
        }
        catch(ClassNotFoundException cnfe)
        {
            cnfe.printStackTrace();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    public static void search()
        throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_QUERY));
        String line = "";
        int queryCount = 0;

        while((line = br.readLine()) != null &&
               line.length() > 0)
        {
            Utils.echo("Start of processing for query #" + ++queryCount);
            int idx = -1;
            int wordIdx = 0;
            String queryNum = "";

            /* Derive query number */
            while(line.charAt(++idx) != ' ')
            {
                queryNum += line.charAt(idx);
            }
            queryNum = queryNum.substring(0, queryNum.length() - 1);

            /* Derive query contents. */
            while(line.charAt(++idx) == ' ');
            line = line.substring(idx);

            Matcher matcher = pattern.matcher(line);
            while(matcher.find())
            {
                String term = Utils.filterText(matcher.group(0).toLowerCase());
                if(++wordIdx < 4 ||
                   stopSet.contains(term))
                {
                    continue;
                }

                // stemmer.setCurrent(term);
                // stemmer.stem();
                execQuery(term);
            }
            sortAndFilterResults();
            writeQueuesToFS(queryNum);
            proxSearchMap.clear();
            Utils.echo("End of processing for query #" + queryCount);
        }
    }

    /* Execute a query */
    public static void execQuery(String term)
        throws IOException
    {
        termFreqMap = SearchClient.queryTerm(term);
        for(Map.Entry<String, Entry> entry : termFreqMap.entrySet())
        {
            if(!proxSearchMap.containsKey(entry.getKey()))
            {
                proxSearchMap.put(entry.getKey(), new ArrayList<ArrayList<Long>>());
            }
            proxSearchMap.get(entry.getKey()).add(entry.getValue().getOffs());
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
        if(offs.size() < 2)
        {
            return Long.MAX_VALUE;
        }

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

    public static void sortAndFilterResults()
    {
        proxsearchq = new Queue();
        for(Map.Entry<String, ArrayList<ArrayList<Long>>> entry : proxSearchMap.entrySet())
        {
            if(entry.getValue().size() == 0)
            {
                proxsearchq.add(new DocScorePair(entry.getKey(), 0D));
            }
            else
            {
                proxsearchq.add(new DocScorePair(entry.getKey(),
                                                 Formulae.proximitySearch(
                                                     minProximity(entry.getValue()),
                                                 entry.getValue().size(),
                                                 docLenMap.get(entry.getKey()))));
            }
        }
        proxsearchq.reverse();
    }

    public static void writeQueuesToFS(String queryNum)
        throws IOException
    {
        Utils.echo("Start of FS ops for query " + queryNum);
        StringBuilder buffer = new StringBuilder();
        FileWriter fw = new FileWriter(
            Properties.DIR_MODEL + "/" + Properties.FILE_PROX_SEARCH, true);
        DocScorePair dsp = null;
        int rank = 0;

        while((dsp = proxsearchq.remove()) != null)
        {
            buffer.append(queryNum + " Q0 " + dsp.getDocNo() + " " +
                          ++rank   + " "    + dsp.getScore() + " Exp\n");
        }
        fw.write(buffer.toString());
        fw.close();
        Utils.echo("End of FS ops for query " + queryNum);
    }

    /**
     * Main method for unit testing.
     * @param args
     *            Program arguments.
     */
    public static void main(String[] args)
    {
        try
        {
            search();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}
/* End of ProximitySearchClient.java */