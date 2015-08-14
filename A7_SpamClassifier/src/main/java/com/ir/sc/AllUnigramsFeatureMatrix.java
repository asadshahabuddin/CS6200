package com.ir.sc;

/* Import list */
import java.io.*;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Iterator;
import com.ir.global.Utils;
import java.util.LinkedHashMap;
import com.ir.global.Properties;
import org.elasticsearch.node.Node;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.NodeBuilder;

/**
 * Author : Asad Shahabuddin
 * Created: Aug 12, 2015
 */

public class AllUnigramsFeatureMatrix
{
    private static LinkedHashMap<String, Integer> wordMap;
    private static HashMap<String, String> trainMap;
    private static HashMap<String, String> testMap;
    private int unigramIdx;

    /**
     * Constructor
     */
    public AllUnigramsFeatureMatrix()
    {
        wordMap = new LinkedHashMap<String, Integer>();
        unigramIdx = 0;
    }

    /**
     * Initialize data structures.
     * @throws ClassNotFoundException, IOException
     */
    public void initialize()
        throws ClassNotFoundException, IOException
    {
        trainMap = Indexer.getTrainMap();
        testMap  = Indexer.getTestMap();
    }

    /**
     * Create a map of all words in training corpus to the file system.
     * @param client
     *            The Elasticsearch client.
     * @throws IOException
     */
    public void createWordMap(Client client)
        throws ClassNotFoundException, IOException
    {
        int count = 0;
        for(String key : trainMap.keySet())
        {
            Iterator<?> keys = Utils.termVectorIterator(client, key);
            while(keys.hasNext())
            {
                String word = String.valueOf(keys.next());
                if(!wordMap.containsKey(word))
                {
                    wordMap.put(word, ++unigramIdx);
                }
            }
            if(++count % 100 == 0)
            {
                Utils.echo("Processed " + count + " of " + trainMap.size() + " documents");
            }
        }
    }

    /**
     * Serialize the map of all words.
     */
    public void serializeWordMap()
        throws ClassNotFoundException, IOException
    {
        FileOutputStream fout = new FileOutputStream(Properties.FILE_UNIGRAM_MAP);
        ObjectOutputStream out = new ObjectOutputStream(fout);
        out.writeObject(wordMap);
        out.close();
        fout.close();
    }

    /**
     * Get the map of all words and their IDs.
     * @return
     *            The aforementioned map.
     */
    public static LinkedHashMap<String, Integer> getWordMap()
        throws ClassNotFoundException, IOException
    {
        FileInputStream fin = new FileInputStream(Properties.FILE_UNIGRAM_MAP);
        ObjectInputStream in = new ObjectInputStream(fin);
        LinkedHashMap<String, Integer> map = (LinkedHashMap<String, Integer>) in.readObject();
        in.close();
        fin.close();
        return map;
    }

    /**
     * Create a feature matrix of the specified type.
     * @param client
     *            The Elasticsearch client.
     * @param dataType
     *            Train/test classification key.
     * @throws ClassNotFoundException, IOException
     */
    public void createFeatureMatrix(Client client, int dataType)
        throws ClassNotFoundException, IOException
    {
        HashMap<String, String> map;
        FileWriter fw;
        if(dataType == Properties.KEY_TRAIN)
        {
            map = trainMap;
            fw  = new FileWriter(Properties.FILE_AU_TRAIN);
        }
        else
        {
            map = testMap;
            fw  = new FileWriter(Properties.FILE_AU_TEST);
        }

        int count = 0;
        for(String key : map.keySet())
        {
            /* Create the label for this document entry. */
            StringBuilder sb = new StringBuilder(key.substring(key.indexOf('.') + 1));
            if(map.get(key).equals("spam"))
            {
                sb.append(" 1");
            }
            else
            {
                sb.append(" 0");
            }

            /* Iterate over all terms and add their respective frequencies. */
            Iterator<?> keys = Utils.termVectorIterator(client, key);
            TreeMap<Integer, Integer> wordTree = new TreeMap<Integer, Integer>();
            while(keys.hasNext())
            {
                String word = String.valueOf(keys.next());
                if(dataType == Properties.KEY_TRAIN ||
                   (dataType == Properties.KEY_TEST &&
                    wordMap.containsKey(word)))
                {
                    wordTree.put(wordMap.get(word), Utils.getTermFrequency(word));
                }
            }
            for(Integer k : wordTree.keySet())
            {
                sb.append(" " + k + ":" + wordTree.get(k));
            }
            fw.write(sb.toString() + "\n");

            if(++count % 100 == 0)
            {
                Utils.echo("Processed " + count + " of " + map.size() + " documents");
            }
        }
        fw.close();
    }

    /**
     * Create the train and test feature matrices.
     * @param client
     *            The Elasticsearch client.
     * @throws ClassNotFoundException, IOException
     */
    public void createFeatureMatrices(Client client)
        throws ClassNotFoundException, IOException
    {
        /* Create the train and test feature matrices. */
        Utils.cout("\n>Creating the feature matrix for train data\n");
        createFeatureMatrix(client, Properties.KEY_TRAIN);
        Utils.cout("\n>Creating the feature matrix for test data\n");
        createFeatureMatrix(client, Properties.KEY_TEST);

        /* Split feature matrices and output to the file system. */
        Utils.splitFeatureMatrix(Properties.FILE_AU_TRAIN,
                                 Properties.FILE_AU_TRAIN_LABEL,
                                 Properties.FILE_AU_TRAIN_DATA);
        Utils.splitFeatureMatrix(Properties.FILE_AU_TEST,
                                 Properties.FILE_AU_TEST_LABEL,
                                 Properties.FILE_AU_TEST_DATA);
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
        Utils.cout("\n=========================");
        Utils.cout("\n| ALL UNIGRAMS FEATURES |");
        Utils.cout("\n=========================");
        Utils.cout("\n");

        Node node = null;
        try
        {
            AllUnigramsFeatureMatrix fm = new AllUnigramsFeatureMatrix();
            node = NodeBuilder.nodeBuilder().client(true).clusterName(Properties.CLUSTER_NAME).node();
            Client client = node.client();
            fm.initialize();
            fm.createWordMap(client);
            fm.serializeWordMap();
            // wordMap = getWordMap();
            Utils.cout("\n>Statistics\n");
            Utils.cout("Total unigrams - " + wordMap.size() + "\n");
            fm.createFeatureMatrices(client);
            // Utils.createRankList(Properties.KEY_ALL);
        }
        catch(ClassNotFoundException cnfe)
        {
            cnfe.printStackTrace();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        finally
        {
            if(node != null)
            {
                node.close();
            }
            Utils.elapsedTime(startTime, "\nCreation of feature matrix completed");
        }
    }
}
/* End of AllUnigramsFeatureMatrix.java */