package com.ir.global;

/* Import list */
import java.util.HashMap;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.action.search.SearchResponse;

/**
 * Author : Asad Shahabuddin
 * Created: Jul 16, 2015
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
     * Ouput an echo to the console.
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
     * Query frequency of the specified term in the entire corpus.
     * @param client
     *            The Elasticsearch client object.
     * @param qb
     *            The query.
     * @param index
     *            The index name.
     * @param type
     *            The index data type.
     * @return
     *            A map of document IDs and frequencies for the specified term.
     */
    public static HashMap<String, Integer> queryTF(Client client, QueryBuilder qb,
                                                   String index , String type)
    {
        SearchResponse resp = client.prepareSearch(index)
                                    .setTypes(type)
                                    .setScroll(new TimeValue(6000))
                                    .setQuery(qb)
                                    .setExplain(true)
                                    .setSize(50)
                                    .execute()
                                    .actionGet();
        /* No results to scroll through. */
        if(resp.getHits().getTotalHits() == 0)
        {
            return new HashMap<>();
        }

        HashMap<String, Integer> res = new HashMap<>();
        while(true)
        {
            for(SearchHit hit : resp.getHits().getHits())
            {
                res.put((String) hit.getSource().get("docno"),
                        (int) hit.getExplanation().getDetails()[0].getDetails()[0].getDetails()[0].getValue());
            }
            resp = client.prepareSearchScroll(resp.getScrollId()).setScroll(
                new TimeValue(6000)).execute().actionGet();
            if(resp.getHits().getHits().length == 0)
            {
                break;
            }
        }
        return res;
    }
}
/* End of Utils.java */