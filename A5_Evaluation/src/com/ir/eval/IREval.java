package com.ir.eval;

/* Import list */
import java.util.HashMap;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import com.ir.global.Utils;
import java.io.BufferedReader;
import com.ir.global.Properties;
import org.elasticsearch.node.Node;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * Author : Asad Shahabuddin
 * Created: Jul 19, 2015
 */

public class IREval
{
    private HashMap<String, Integer> docLenMap;
    private HashMap<String, Double> bm25Map;
    private Queue bm25q;
    private double avgDocLength;

    /**
     * Constructor.
     */
    public IREval()
    {
        docLenMap    = new HashMap<>();
        bm25Map      = new HashMap<>();
        bm25q        = new Queue(Properties.KEY_IREVAL);
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
        br.close();
        avgDocLength /= (double) Properties.COUNT_DOC;
    }

    /**
     * Add the first 1000 entries by score to a priority queue.
     */
    public void sortAndFilterMap()
    {
        for(String key : bm25Map.keySet())
        {
            bm25q.add(new NodeScorePair(key, bm25Map.get(key)));
        }
        bm25Map.clear();
        bm25q.reverse();
    }

    /**
     * Output the queue to the file system.
     * @param q
     *            The query.
     * @throws IOException
     */
    public void writeQueue(String qid, String q)
        throws IOException
    {
        Utils.echo("Start of file system write for query '" + q + "'");
        FileWriter fw = new FileWriter("bm25-" + qid + ".txt");
        NodeScorePair nsp;
        int rank = 0;

        /* Output results to the file system. */
        while((nsp = bm25q.remove()) != null)
        {
            fw.write(qid + " Q0 " + nsp.getNode() + " " + ++rank + " " + nsp.getScore() + " Exp\n");
        }
        fw.close();
        Utils.echo("End of file system operations for query '" + q + "'");
    }

    /**
     * Create a rank list.
     * @param client
     *            The Elasticsearch client object.
     * @param q
     *            The query.
     * @throws IOException
     */
    public void createRankList(Client client, String qid, String q)
        throws IOException
    {
        /* Initialize the data structures and start processing the query. */
        initialize();
        Utils.echo("Start of processing for query '" + q + "'");

        for(String term : q.split(" "))
        {
            Utils.echo("Going through term frequencies for " + term);
            int count = 0;
            HashMap<String, Integer> tfMap = Utils.queryTF(client,
                                                           QueryBuilders.matchQuery("text", term.toLowerCase()),
                                                           Properties.INDEX_NAME,
                                                           Properties.INDEX_TYPE);

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
                /* Output current status to the console. */
                if(++count % 100 == 0)
                {
                    Utils.echo("Processed " + count + " out of " + tfMap.size() + " documents");
                }
            }
            Utils.cout("\n");
        }

        sortAndFilterMap();
        writeQueue(qid, q);
        Utils.echo("End of processing for query '" + q + "'");
    }

    /**
     * Main method for unit testing.
     * @param args
     *            Program arguments.
     */
    public static void main(String[] args)
    {
        /* Calculate start time. */
        long startTime = System.nanoTime();
        IREval eval = new IREval();
        Node node = NodeBuilder.nodeBuilder().client(true).clusterName(Properties.CLUSTER_NAME).node();
        Client client = node.client();

        try
        {
            /* Create the rank list. */
            Utils.cout("\n>Creating the rank list\n");
            eval.createRankList(client, "150403", "retina display");
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        finally
        {
            node.close();
            Utils.elapsedTime(startTime, "\nCreation of rank list completed.");
        }
    }
}
/* End of IREval.java */