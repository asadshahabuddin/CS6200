package com.ir.global;

/* Import list */
import java.io.*;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONObject;
import org.json.JSONException;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.common.xcontent.*;
import java.util.concurrent.ExecutionException;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.termvector.TermVectorResponse;

/**
 * Author : Asad Shahabuddin
 * Created: Jul 5, 2015
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
     * Output a list of all document IDs to the file system.
     * @param dir
     *            The directory containing in-link graphs.
     * @throws IOException
     */
    public static void createDocList(String dir)
        throws IOException
    {
        if(dir == null)
        {
            return;
        }

        /* Read the Document IDs into a set. */
        HashSet<String> set = new HashSet<>();
        for(File file : new File(dir).listFiles())
        {
            if(file.getName().contains("graph-"))
            {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while((line = br.readLine()) != null)
                {
                    set.add(line.split(" ")[0]);
                }
                br.close();
            }
        }

        /* Write the Document IDs to the file system. */
        FileWriter fw = new FileWriter(Properties.FILE_DOCLIST);
        StringBuilder sb = new StringBuilder();
        for(String s : set)
        {
            sb.append(s + "\n");
        }
        fw.write(sb.toString());
        fw.close();
    }

    /**
     * Create a set of all document IDs.
     * @return
     *            The set of all document IDs.
     * @throws IOException
     */
    public static HashSet<String> createDocSet()
        throws IOException
    {
        HashSet<String> set = new HashSet<>();
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_DOCLIST));
        String line;

        while((line = br.readLine()) != null)
        {
            set.add(line);
        }
        br.close();
        return set;
    }

    /**
     * Calculate the document length.
     * @param client
     *            The Elasticsearch client object.
     * @param docId
     *            The document ID.
     * @return
     *            The document length.
     * @throws IOException, JSONException, InterruptedException, ExecutionException
     */
    public static int calcDocLength(Client client, String docId)
        throws IOException, JSONException, InterruptedException, ExecutionException
    {
        int len = 0;

        /* Execute the query and get the resultant builder. */
        TermVectorResponse res = client.prepareTermVector()
                                       .setIndex(Properties.INDEX_NAME)
                                       .setType(Properties.INDEX_TYPE)
                                       .setId(docId)
                                       .execute()
                                       .actionGet();
        XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON).prettyPrint();
        builder.startObject();
        res.toXContent(builder, ToXContent.EMPTY_PARAMS);
        builder.endObject();

        try
        {
            /* Get the term vector response as a JSON object. */
            JSONObject jsonObj = new JSONObject(XContentHelper.convertToJson(builder.bytes(), false))
                                 .getJSONObject("term_vectors")
                                 .getJSONObject("text")
                                 .getJSONObject("terms");
            Iterator<?> keys = jsonObj.keys();

            /* Iterate over all terms and add their respective frequencies. */
            while(keys.hasNext())
            {
                len += jsonObj.getJSONObject(String.valueOf(keys.next())).getInt("term_freq");
            }
        }
        catch(JSONException jsone)
        {
            len = 0;
        }

        return len;
    }

    /**
     * Output a list of all document IDs and the corresponding document
     * lengths to the file system.
     * @throws IOException, JSONException, InterruptedException, ExecutionException
     */
    public static void createDocLenList(Client client)
        throws IOException, JSONException, InterruptedException, ExecutionException
    {
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_DOCLIST));
        FileWriter fw = new FileWriter(Properties.FILE_DOCLEN);
        StringBuilder sb = new StringBuilder();
        String line;
        int count = 0;

        while((line = br.readLine()) != null)
        {
            sb.append(line + " " + calcDocLength(client, line) + "\n");
            if(++count % 100 == 0)
            {
                Utils.echo("Processed " + count + " documents");
            }
        }
        fw.write(sb.toString());
        fw.close();
        br.close();
    }

    /**
     * Create a map of all document IDs and the corresponding document lengths.
     * @return
     *            A map of all document IDs and corresponding document lengths.
     * @throws IOException
     */
    public static HashMap<String, Integer> createDocLenMap()
        throws IOException
    {
        HashMap<String, Integer> map = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_DOCLEN));
        String line;

        while((line = br.readLine()) != null)
        {
            String[] words = line.split(" ");
            map.put(words[0], Integer.valueOf(words[1]));
        }
        return map;
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

    /**
     * Query Elasticsearch for an attribute in the specified document.
     * @param client
     *            The Elasticsearch client object.
     * @param docNo
     *            The document ID.
     * @param attr
     *            The attribute whose value is to be queried.
     * @return
     *            The value of this attribute.
     */
    public static String queryAttr(Client client, String docNo, String attr)
    {
        return client.prepareSearch(Properties.INDEX_NAME)
                     .setTypes(Properties.INDEX_TYPE)
                     .setQuery(QueryBuilders.matchQuery("docno", docNo))
                     .setExplain(true)
                     .execute()
                     .actionGet()
                     .getHits()
                     .getHits()[0]
                     .getSource()
                     .get(attr)
                     .toString();
    }
}
/* End of Utils.java */