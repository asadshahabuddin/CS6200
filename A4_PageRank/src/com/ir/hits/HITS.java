package com.ir.hits;

/* Import list */
import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import com.ir.global.Utils;
import com.ir.global.Queue;
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
    private BufferedReader br;
    private FileWriter fw;

    /**
     * Constructor.
     */
    public HITS()
    {
        docLenMap    = new HashMap<>();
        bm25Map      = new HashMap<>();
        bm25q        = new Queue();
        avgDocLength = 0;
        br           = null;
    }

    /**
     * Initialize the data structures.
     * @throws IOException
     */
    public void initialize()
        throws IOException
    {
        br = new BufferedReader(new FileReader(Properties.FILE_DOCLEN));
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
        bm25Map.clear();
        bm25q.reverse();
    }

    /**
     * Write the content of Okapi BM25 queue to the file system.
     * @throws IOException
     */
    public HashSet<String> writeQueue(String q)
        throws IOException
    {
        Utils.echo("Start of file system write for query '" + q + "'");
        HashSet<String> set = new HashSet<String>();
        fw = new FileWriter(Properties.FILE_BM25);
        NodeScorePair nsp;
        int rank = 0;

        /* Output the Okapi BM25 results to the file system and populate the set
        of root nodes. */
        while((nsp = bm25q.remove()) != null)
        {
            set.add(nsp.getNode());
            fw.write(nsp.getNode() + " " + ++rank + " " + nsp.getScore() + "\n");
        }
        fw.close();
        Utils.echo("End of file system operations for query '" + q + "'");
        Utils.echo("End of processing for query '" + q + "'");

        return set;
    }

    /**
     * Create the root set.
     * @param client
     *            The Elasticsearch client object.
     * @param q
     *            The query.
     * @throws IOException
     */
    public HashSet<String> createRootSet(Client client, String q)
        throws IOException
    {
        /* If the root set already exists, read it from the file system. */
        HashSet<String> baseSet = new HashSet<>();
        File file = new File(Properties.FILE_BM25);
        if(file.exists())
        {
            br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null)
            {
                baseSet.add(line.split(" ")[0]);
            }
            br.close();
            return baseSet;
        }

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
        return writeQueue(q);
    }

    /**
     * Create the base set.
     * @param client
     *            The Elasticsearch client object.
     * @param rootSet
     *            The root set.
     * @return
     *            The equivalent base set.
     * @throws IOException
     */
    public HashSet<String> createBaseSet(Client client, HashSet<String> rootSet)
        throws IOException
    {
        /* If the base set already exists, read it from the file system. */
        HashSet<String> baseSet = new HashSet<>();
        File file = new File(Properties.FILE_BASESET);
        if(file.exists())
        {
            br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null)
            {
                baseSet.add(line);
            }
            br.close();
            return baseSet;
        }

        /*
        Create the base set as follows:
        (1) For each page in the root set, add all the pages that the page points to.
        (2) For each page in the root set, add a set of pages with size <= |d| that
            point to the page.
        */
        HashSet<String> docs = Utils.createDocSet();
        int count = 0;
        for(String docId : rootSet)
        {
            /* Add out-links to the base set. */
            String[] ids = Utils.queryAttr(client, docId, "outlinks").split(" ");
            for(String id : ids)
            {
                if(docs.contains(id))
                {
                    baseSet.add(id);
                }
            }
            /* Add in-links to the base set. */
            ids = Utils.queryAttr(client, docId, "inlinks").split(" ");
            for(int i = 0; i < ids.length && i < 50; i++)
            {
                if(docs.contains(ids[i]))
                {
                    baseSet.add(ids[i]);
                }
            }
            /* Output current status to the console. */
            if(++count % 100 == 0)
            {
                Utils.echo("Processed " + count + " documents");
            }
        }
        /* Combine the root and base sets to create the final sub-graph. */
        baseSet.addAll(rootSet);

        /* Write the sub-graph to the file system and return it. */
        FileWriter fw = new FileWriter(file);
        for(String s : baseSet)
        {
            if(!s.equals(""))
            {
                fw.write(s + "\n");
            }
            else
            {
                Utils.warning("Encountered a blank URL");
                Utils.echo("Ignoring...");
            }
        }
        fw.close();
        return baseSet;
    }

    /**
     * Create an index for URLs in the base set.
     * @param baseSet
     *            The base set.
     * @return
     *            The equivalent index.
     */
    public HashMap<String, Integer> createIndex(HashSet<String> baseSet)
        throws IOException, ClassNotFoundException
    {
        /* If the index already exists, read it from the file system. */
        HashMap<String, Integer> index = new HashMap<>();
        File file = new File(Properties.FILE_IDX_OBJ);
        if(file.exists())
        {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            index = (HashMap<String, Integer>) in.readObject();
            in.close();
            return index;
        }

        /* Create the index. */
        int id = 0;
        for(String docId : baseSet)
        {
            index.put(docId, id++);
        }
        /* Free the memory of data duplication. */
        baseSet.clear();
        /* Serialize the index and return it. */
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
        out.writeObject(index);
        out.close();
        return index;
    }

    /**
     * Create an adjacency matrix.
     * @param client
     *            The Elasticsearch client object.
     * @param index
     *            An index of the (former) base set.
     * @param n
     *            Size of the index.
     * @return
     *            The equivalent adjacency matrix.
     */
    public HashMap[] createAdjacencyMatrix(Client client, HashMap<String, Integer> index, int n)
        throws IOException, ClassNotFoundException
    {
        /* If the adjacency matrix already exists, read it from the file system. */
        HashMap<Integer, HashSet<Integer>> inM  = new HashMap<>();
        HashMap<Integer, HashSet<Integer>> outM = new HashMap<>();
        File file1 = new File(Properties.FILE_AMIN_OBJ);
        File file2 = new File(Properties.FILE_AMOUT_OBJ);
        if(file1.exists() && file2.exists())
        {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file1));
            inM = (HashMap<Integer, HashSet<Integer>>) in.readObject();
            in.close();
            in = new ObjectInputStream(new FileInputStream(file2));
            outM = (HashMap<Integer, HashSet<Integer>>) in.readObject();
            in.close();
            return new HashMap[] {inM, outM};
        }

        /* Create the adjacency matrix. */
        int count = 0;
        for(String docId : index.keySet())
        {
            HashSet<String> inIds = new HashSet<>(Arrays.asList(
                Utils.queryAttr(client, docId, "inlinks").split(" ")));
            for(String inId : inIds)
            {
                if(index.containsKey(inId))
                {
                    if(!inM.containsKey(index.get(docId)))
                    {
                        inM.put(index.get(docId), new HashSet<Integer>());
                    }
                    if(!outM.containsKey(index.get(inId)))
                    {
                        outM.put(index.get(inId), new HashSet<Integer>());
                    }
                    inM.get(index.get(docId)).add(index.get(inId));
                    outM.get(index.get(inId)).add(index.get(docId));
                }
            }
            /* Output current status to the console. */
            if(++count % 100 == 0)
            {
                Utils.echo("Processed " + count + " documents");
            }
        }

        /* Serialize the adjacency matrix and return it. */
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file1));
        out.writeObject(inM);
        out.close();
        out = new ObjectOutputStream(new FileOutputStream(file2));
        out.writeObject(outM);
        out.close();
        return new HashMap[] {inM, outM};
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
            /* Create the root set. */
            Utils.cout("\n>Creating the root set\n");
            HashSet<String> set = h.createRootSet(client, Properties.QUERY_TOPICAL);
            Utils.echo("Size of the root set is " + set.size());

            /* Create the base set. */
            Utils.cout("\n>Creating the base set by expanding roots\n");
            set = h.createBaseSet(client, set);
            Utils.echo("Size of the base set is " + set.size());

            /* Create an index for the base set. */
            HashMap<String, Integer> index = h.createIndex(set);

            /* Create an adjacency matrix for the base set. */
            Utils.cout("\n>Creating the adjacency matrix\n");
            HashMap[] adjM = h.createAdjacencyMatrix(client, index, index.size());
            node.close();

            /* Calculate the Hub and Authority scores. */
            // TODO
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        catch(ClassNotFoundException cnfe)
        {
            cnfe.printStackTrace();
        }
        finally
        {
            if(node != null)
            {
                node.close();
            }
            Utils.elapsedTime(startTime, "\nHITS algorithm completed.");
        }
    }
}
/* End of HITS.java */