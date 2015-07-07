package com.ir.hits;

/* Import list */
import java.io.FileWriter;
import java.util.HashMap;
import java.io.FileReader;
import java.io.IOException;
import com.ir.global.Utils;
import com.ir.global.Queue;
import java.io.BufferedReader;
import com.ir.global.Properties;
import com.ir.global.NodeScorePair;
import org.elasticsearch.node.Node;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * Author : Asad Shahabuddin
 * Created: Jul 7, 2015
 */

public class HITS
{
    private HashMap<String, Integer> docLenMap;
    private HashMap<String, Double> bm25Map;
    private Queue bm25q;
    private double avgDocLength;

    /**
     * Constructor.
     */
    public HITS()
    {
        docLenMap    = new HashMap<>();
        bm25Map      = new HashMap<>();
        bm25q        = new Queue();
        avgDocLength = 0;
    }

    /**
     * Initialize the data structures.
     * @throws IOException
     */
    public void initialize()
        throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_DOCLEN));
        String line;
        while((line = br.readLine()) != null)
        {
            String[] words = line.split(" ");
            docLenMap.put(words[0], Integer.valueOf(words[1]));
            bm25Map.put(words[0], 0D);
            avgDocLength += Integer.valueOf(words[1]);
        }
        avgDocLength /= (double) Properties.COUNT_DOC;
    }

    /**
     * Reset the data structure(s).
     */
    public void reset()
    {
        for(String key : docLenMap.keySet())
        {
            bm25Map.put(key, 0D);
        }
    }

    /**
     * Add the first 1000 entries by score from the Okapi BM25 map to its
     * queue in descending order.
     */
    public void sortAndFilterMap()
    {
        for(String key : bm25Map.keySet())
        {
            bm25q.add(new NodeScorePair(key, bm25Map.get(key)));
        }
        bm25q.reverse();
    }

    /**
     * Write the content of Okapi BM25 queue to the file system.
     * @throws IOException
     */
    public void writeQueue(String q)
        throws IOException
    {
        Utils.echo("Start of file system write for query '" + q + "'");
        FileWriter fw = new FileWriter(Properties.FILE_BM25);
        StringBuilder sb = new StringBuilder();
        NodeScorePair nsp;
        int rank = 0;

        while((nsp = bm25q.remove()) != null)
        {
            sb.append(nsp.getNode() + " " + ++rank   + " " + nsp.getScore() + "\n");
        }
        fw.write(sb.toString());
        fw.close();
        Utils.echo("End of file system operations for query '" + q + "'");
    }

    /**
     * Create the root set.
     * @param client
     *            The Elasticsearch client object.
     * @param q
     *            The query.
     * @throws IOException
     */
    public void createRootSet(Client client, String q)
        throws IOException
    {
        Utils.echo("Start of processing for query '" + q + "'");
        for(String term : q.split(" "))
        {
            Utils.echo("Going through term frequencies for " + term);
            HashMap<String, Integer> tfMap = Utils.queryTF(client,
                                                           QueryBuilders.matchQuery("text", term.toLowerCase()),
                                                           Properties.INDEX_NAME,
                                                           Properties.INDEX_TYPE);
            int count = 0;

            for(String docNo : tfMap.keySet())
            {
                if(!bm25Map.containsKey(docNo))
                {
                    Utils.warning("Ignoring " + docNo);
                }
                else
                {
                    bm25Map.put(docNo, bm25Map.get(docNo) +
                                       Model.okapibm25((double) tfMap.size(),
                                                       (double) tfMap.get(docNo),
                                                       docLenMap.get(docNo),
                                                       avgDocLength,
                                                       1));
                }
                if(++count % 100 == 0)
                {
                    Utils.echo("Processed " + count + " out of " + tfMap.size() + " documents");
                }
            }
            Utils.cout("\n");
        }
        sortAndFilterMap();
        writeQueue(q);
        Utils.echo("End of processing for query '" + q + "'");
    }

    /**
     * Main method for unit testing.
     * @param args
     *            Program arguments.
     */
    public static void main(String[] args)
    {
        /* Calculate start time */
        long startTime = System.nanoTime();
        Utils.cout("\n==============");
        Utils.cout("\nHITS ALGORITHM");
        Utils.cout("\n==============");
        Utils.cout("\n");

        HITS h = new HITS();
        Node node = NodeBuilder.nodeBuilder().client(true).clusterName(Properties.CLUSTER_NAME).node();
        Client client = node.client();

        try
        {
            h.initialize();
            h.createRootSet(client, Properties.QUERY_TOPICAL);
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        finally
        {
            node.close();
            Utils.elapsedTime(startTime, "\nHITS algorithm completed.");
        }
    }
}
/* End of HITS.java */