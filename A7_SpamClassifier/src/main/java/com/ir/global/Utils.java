package com.ir.global;

/* Import list */
import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.action.search.SearchResponse;

/**
 * Author : Asad Shahabuddin
 * Created: Aug 9, 2015
 */

public class Utils
{
    /**
     * Output a message to the console.
     * @param o
     *            The message.
     */
    public static void cout(Object o)
    {
        System.out.print(o);
    }

    /**
     * Output an echo to the console.
     * @param o
     *            The message.
     */
    public static void echo(Object o)
    {
        System.out.println("   [echo] " + o);
    }

    /**
     * Output an error to the console.
     * @param o
     *            The error message.
     */
    public static void error(Object o)
    {
        System.out.println("  [error] " + o);
    }

    /**
     * Output a warning to the console.
     * @param o
     *            The warning message.
     */
    public static void warning(Object o)
    {
        System.out.println("[warning] " + o);
    }

    /**
     * (1) Output time elapsed since last checkpoint.
     * (2) Return start time for this checkpoint.
     * @param startTime
     *            Start time for the current phase.
     * @param message
     *            User-friendly message.
     * @return
     *            Start time for the next phase.
     */
    public static long elapsedTime(long startTime, String message)
    {
        if(message != null)
        {
            System.out.println(message);
        }
        long elapsedTime = (System.nanoTime() - startTime) / 1000000000;
        System.out.println("Elapsed time: " + elapsedTime + " second(s)");
        return System.nanoTime();
    }

    /**
     * Generate random 0s and 1s.
     * @return
     *           0 or 1, chosen randomly.
     */
    public static int random()
    {
        return new Random().nextInt(2);
    }

    /**
     * Query frequency of specified term in the corpus.
     * @param client
     *            The Elasticsearch client.
     * @param qb
     *            The query.
     * @param index
     *            The index name.
     * @param type
     *            The index type.
     * @return
     *            A map of file names and frequencies for the specified term.
     */
    public static Map<String, Integer> queryTF(Client client, QueryBuilder qb,
                                               String index , String type)
    {
        SearchResponse scrollResp = client.prepareSearch(index)
                                          .setTypes(type)
                                          .setScroll(new TimeValue(6000))
                                          .setQuery(qb)
                                          .setExplain(true)
                                          .setSize(1000).execute().actionGet();
        /* No match */
        if (scrollResp.getHits().getTotalHits() == 0)
        {
            return new HashMap<String, Integer>();
        }

        Map<String, Integer> results = new HashMap<String, Integer>();
        while (true)
        {
            for (SearchHit hit : scrollResp.getHits().getHits())
            {
                String file = (String) hit.getSource().get("file_name");
                int tf = (int) hit.getExplanation().getDetails()[0].getDetails()[0].getDetails()[0].getValue();
                results.put(file, tf);
            }
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(
                new TimeValue(6000)).execute().actionGet();
            if (scrollResp.getHits().getHits().length == 0)
            {
                break;
            }
        }
        return results;
    }
}
/* End of Utils.java */