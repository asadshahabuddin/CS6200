package com.ir.sc;

/* Import list */
import java.util.Map;
import java.util.HashMap;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import com.ir.global.Utils;
import java.io.BufferedReader;
import java.util.LinkedHashMap;
import com.ir.global.Properties;
import org.elasticsearch.node.Node;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * Author : Asad Shahabuddin
 * Created: Aug 11, 2015
 */

public class ManualSpamFeatureMatrix
{
    private LinkedHashMap<String, String> wordMap;
    private HashMap<String, String> trainMap;
    private HashMap<String, String> testMap;
    private HashMap<String, StringBuilder> trainDataMap;
    private HashMap<String, StringBuilder> testDataMap;

    /**
     * Constructor
     */
    public ManualSpamFeatureMatrix()
    {
        wordMap      = new LinkedHashMap<String, String>();
        trainDataMap = new HashMap<String, StringBuilder>();
        testDataMap  = new HashMap<String, StringBuilder>();
    }

    /**
     * Initialize the data structures.
     * @throws ClassNotFoundException, IOException
     */
    public void initialize()
        throws ClassNotFoundException, IOException
    {
        trainMap = Indexer.getTrainMap();
        testMap  = Indexer.getTestMap();

        for(String key : trainMap.keySet())
        {
            StringBuilder sb = new StringBuilder(key.substring(key.indexOf('.') + 1));
            if(trainMap.get(key).equals("spam"))
            {
                sb.append(" 1");
            }
            else
            {
                sb.append(" 0");
            }
            trainDataMap.put(key, sb);
        }
        for(String key : testMap.keySet())
        {
            StringBuilder sb = new StringBuilder(key.substring(key.indexOf('.') + 1));
            if(testMap.get(key).equals("spam"))
            {
                sb.append(" 1");
            }
            else
            {
                sb.append(" 0");
            }
            testDataMap.put(key, sb);
        }
    }

    /**
     * Create a map of spam words and their IDs.
     * @throws IOException
     */
    public void createWordMap()
        throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_SPAMLIST));
        String line;
        while((line = br.readLine()) != null)
        {
            wordMap.put(line.split(" ")[0], line.split(" ")[1]);
        }
    }

    /**
     * Output a feature matrix to the file system.
     * @param dataKey
     *            Train/test classification key.
     * @throws IOException
     */
    public void writeFeatureMatrix(int dataKey)
        throws IOException
    {
        /* Determine the data classification. */
        FileWriter fw;
        HashMap<String, StringBuilder> map;
        if(dataKey == Properties.KEY_TRAIN)
        {
            fw  = new FileWriter(Properties.FILE_MS_TRAIN);
            map = trainDataMap;
        }
        else
        {
            fw  = new FileWriter(Properties.FILE_MS_TEST);
            map = testDataMap;
        }

        /* Output data to the file system. */
        for(String key : map.keySet())
        {
            String val = map.get(key).toString();
            if(!val.substring(val.length() - 2).equals(" 0") &&
               !val.substring(val.length() - 2).equals(" 1"))
            {
                fw.write(val + "\n");
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
        for(String word : wordMap.keySet())
        {
            Utils.echo("Processing documents for word - " + word);
            Map<String, Integer> tfMap = Utils.queryTF(client,
                                                       QueryBuilders.matchQuery("body", word),
                                                       Properties.INDEX_NAME,
                                                       Properties.INDEX_TYPE);
            for(String file : tfMap.keySet())
            {
                if(trainDataMap.containsKey(file))
                {
                    trainDataMap.get(file).append(" " + wordMap.get(word) + ":1");
                }
                else
                {
                    testDataMap.get(file).append(" " + wordMap.get(word) + ":1");
                }
            }
        }

        /* Output feature matrices to the file system. */
        writeFeatureMatrix(Properties.KEY_TRAIN);
        writeFeatureMatrix(Properties.KEY_TEST);
        Utils.splitFeatureMatrix(Properties.FILE_MS_TRAIN,
                                 Properties.FILE_MS_TRAIN_LABEL,
                                 Properties.FILE_MS_TRAIN_DATA);
        Utils.splitFeatureMatrix(Properties.FILE_MS_TEST,
                                 Properties.FILE_MS_TEST_LABEL,
                                 Properties.FILE_MS_TEST_DATA);
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
        Utils.cout("\n========================");
        Utils.cout("\n| MANUAL SPAM FEATURES |");
        Utils.cout("\n========================");
        Utils.cout("\n");

        Node node = null;
        try
        {
            ManualSpamFeatureMatrix fm = new ManualSpamFeatureMatrix();
            fm.initialize();
            fm.createWordMap();
            node = NodeBuilder.nodeBuilder().client(true).clusterName(Properties.CLUSTER_NAME).node();
            Client client = node.client();
            Utils.cout("\n>Creating the feature matrices\n");
            fm.createFeatureMatrices(client);
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
/* End of ManualSpamFeatureMatrix.java */