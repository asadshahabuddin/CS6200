package com.ir.global;

/* Import list */
import java.util.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.BufferedReader;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.termvector.TermVectorResponse;

/**
 * Author : Asad Shahabuddin
 * Created: Aug 9, 2015
 */

public class Utils
{
    private static JSONObject jsonObj;

    static
    {
        jsonObj = null;
    }

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

    /**
     * Create a term vector iterator for the specified key.
     * @param client
     *            The Elasticsearch client.
     * @param key
     *            The key.
     * @return
     *            An iterator for the input key.
     * @throws IOException
     */
    public static Iterator<?> termVectorIterator(Client client, String key)
        throws IOException
    {
        /* Execute the query and get the resultant builder. */
        TermVectorResponse response = client.prepareTermVector()
                                            .setIndex(Properties.INDEX_NAME)
                                            .setType(Properties.INDEX_TYPE)
                                            .setId(key)
                                            .execute()
                                            .actionGet();
        XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON).prettyPrint();
        builder.startObject();
        response.toXContent(builder, ToXContent.EMPTY_PARAMS);
        builder.endObject();

        try
        {
            /* Get the term vector response as a JSON object. */
            jsonObj = new JSONObject(XContentHelper.convertToJson(builder.bytes(), false))
                      .getJSONObject("term_vectors")
                      .getJSONObject("body")
                      .getJSONObject("terms");
        }
        catch(JSONException jsone)
        {
            Utils.cout("\n");
            Utils.warning("JSON Exception>");
            Utils.warning("Inside termVectorIterator(...) for file - " + key + "\n");
        }
        return jsonObj.keys();
    }

    /**
     * Get the frequency for a key from the term vector response.
     * @param key
     *            The key.
     * @return
     *            The term frequency.
     */
    public static int getTermFrequency(String key)
    {
        int res = -1;
        if(jsonObj == null)
        {
            return res;
        }
        try
        {
            res = jsonObj.getJSONObject(key).getInt("term_freq");
        }
        catch(JSONException jsone) {}
        return res;
    }

    /**
     * Split a feature matrix into labels and data.
     * @param inputFile
     *            The input file.
     * @param labelFile
     *            The label file.
     * @param dataFile
     *            The data file.
     * @throws IOException
     */
    public static void splitFeatureMatrix(String inputFile, String labelFile, String dataFile)
        throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        FileWriter fw1 = new FileWriter(labelFile);
        FileWriter fw2 = new FileWriter(dataFile);
        String line;

        /* Split labels and data between files. */
        while((line = br.readLine()) != null)
        {
            fw1.write(line.substring(0, line.indexOf(' ')) + "\n");
            fw2.write(line.substring(line.indexOf(' ') + 1) + "\n");
        }

        /* Close file reader and writer objects. */
        fw2.close();
        fw1.close();
        br.close();
    }
}
/* End of Utils.java */