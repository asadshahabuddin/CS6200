package com.ir.sc;

/* Import list */
import java.io.*;
import org.jsoup.Jsoup;
import java.util.Random;
import java.util.HashMap;
import com.ir.global.Utils;
import com.ir.global.Properties;
import org.jsoup.nodes.Document;
import org.elasticsearch.node.Node;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.NodeBuilder;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.stream.EntityState;
import org.apache.james.mime4j.stream.MimeTokenStream;
import org.elasticsearch.common.xcontent.XContentFactory;

/**
 * Author : Asad Shahabuddin
 * Created: Aug 9, 2015
 */

public class Indexer
{
    private int totalDocCount;
    private int spamDocCount;
    private int hamDocCount;
    private int trainDocCount;
    private int trainSpamDocCount;
    private int trainHamDocCount;
    private int curDocCount;
    private int curSpamDocCount;
    private int curHamDocCount;
    private int curTrainSpamDocCount;
    private int curTrainHamDocCount;

    private HashMap<String, String> docMap;
    private HashMap<String, String> trainMap;
    private HashMap<String, String> testMap;

    /**
     * Constructor
     */
    public Indexer()
    {
        spamDocCount         = 0;
        hamDocCount          = 0;
        trainDocCount        = 0;
        trainSpamDocCount    = 0;
        trainHamDocCount     = 0;
        curDocCount          = 0;
        curSpamDocCount      = 0;
        curHamDocCount       = 0;
        curTrainSpamDocCount = 0;
        curTrainHamDocCount  = 0;

        docMap   = new HashMap<String, String>();
        trainMap = new HashMap<String, String>();
        testMap  = new HashMap<String, String>();
    }

    /**
     * Create a map of document names with spam/ham classification.
     * @param file
     *            The input file.
     * @throws IOException
     */
    public void createDocMap(String file)
        throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while((line = br.readLine()) != null)
        {
            if(line.contains("spam"))
            {
                docMap.put(line.substring(line.indexOf("inmail")), "spam");
                spamDocCount++;
            }
            else
            {
                docMap.put(line.substring(line.indexOf("inmail")), "ham");
                hamDocCount++;
            }
        }
    }

    /**
     * Serialize the maps.
     * @throws IOException
     */
    public void serializeMaps()
        throws IOException
    {
        /* Serialize train map. */
        FileOutputStream fout = new FileOutputStream(Properties.FILE_TRAIN_MAP);
        ObjectOutputStream out = new ObjectOutputStream(fout);
        out.writeObject(trainMap);
        out.close();
        fout.close();

        /* Serialize test map. */
        fout = new FileOutputStream(Properties.FILE_TEST_MAP);
        out = new ObjectOutputStream(fout);
        out.writeObject(testMap);
        out.close();
        fout.close();
    }

    /**
     * Get the map of training documents and their spam classification.
     * @return
     *            The aforementioned map.
     * @throws ClassNotFoundException, IOException
     */
    public HashMap<String, String> getTrainMap()
        throws ClassNotFoundException, IOException
    {
        FileInputStream fin = new FileInputStream(Properties.FILE_TRAIN_MAP);
        ObjectInputStream in = new ObjectInputStream(fin);
        HashMap<String, String> map = (HashMap<String, String>) in.readObject();
        in.close();
        fin.close();
        return map;
    }

    /**
     * Get the map of testing documents and their spam classification.
     * @return
     *            The aforementioned map.
     * @throws ClassNotFoundException, IOException
     */
    public HashMap<String, String> getTestMap()
        throws ClassNotFoundException, IOException
    {
        FileInputStream fin = new FileInputStream(Properties.FILE_TEST_MAP);
        ObjectInputStream in = new ObjectInputStream(fin);
        HashMap<String, String> map = (HashMap<String, String>) in.readObject();
        in.close();
        fin.close();
        return map;
    }

    /**
     * Calculate corpus statistics.
     */
    public void calcStatistics()
    {
        totalDocCount     = spamDocCount + hamDocCount;
        trainDocCount     = (totalDocCount * 4) / 5;
        trainSpamDocCount = (spamDocCount  * 4) / 5;
        trainHamDocCount  = (hamDocCount   * 4) / 5;
    }

    /**
     * Output corpus statistics to the console.
     */
    public void outputStatistics()
    {
        Utils.cout("\n>Statistics\n");
        Utils.cout("Total                - " + (spamDocCount + hamDocCount) + "\n");
        Utils.cout("Spam                 - " + spamDocCount + "\n");
        Utils.cout("Ham                  - " + hamDocCount + "\n");
        Utils.cout("Train                - " + trainDocCount + "\n");
        Utils.cout("Test                 - " + (spamDocCount + hamDocCount - trainDocCount) + "\n");
        Utils.cout("Train spam           - " + trainSpamDocCount + "\n");
        Utils.cout("Train ham            - " + trainHamDocCount + "\n");
        Utils.cout("Train spam (current) - " + curTrainSpamDocCount + "\n");
        Utils.cout("Train ham (current)  - " + curTrainHamDocCount + "\n\n");
    }

    /**
     * Retrieve contents of the specified file.
     * @param file
     *            The file.
     * @return
     *            Contents of the file.
     * @throws IOException, MimeException
     */
    public String content(File file)
        throws IOException, MimeException
    {
        StringBuilder sb = new StringBuilder();
        MimeTokenStream mimeStream = new MimeTokenStream();
        mimeStream.parse(new FileInputStream(file));

        for(EntityState state = mimeStream.getState();
            state != EntityState.T_END_OF_STREAM;
            state = mimeStream.next())
        {
            if(state == EntityState.T_BODY)
            {
                sb.append(mimeStream.getInputStream());
            }
        }

        return sb.toString();
    }

    /**
     * Convert formatted text to plain text.
     * @param file
     *            The input file.
     * @return
     *            The plain text.
     * @throws IOException
     */
    public String plainText(File file)
        throws IOException
    {
        /* Read the contents to file into memory. */
        FileInputStream finStream = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        finStream.read(data);
        finStream.close();

        /* Convert formatted text to plain text. */
        Document document = Jsoup.parse(new String(data));
        String s = document.body().text();
        return s.substring(s.indexOf("][ ") + 3)
                .replaceAll(" ]]", "");
    }

    /**
     * Generate random 0s and 1s.
     * @return
     *           0 or 1, chosen randomly.
     */
    public int random()
    {
        return new Random().nextInt(2);
    }

    /**
     * Partition spam documents into train and test data.
     * @param file
     *            The input file.
     * @return
     *            Train/test classification.
     */
    public String partitionSpam(String file)
    {
        int key = 0;
        String val;
        /* Determine partition. */
        if(trainSpamDocCount - curTrainSpamDocCount < spamDocCount - curSpamDocCount)
        {
            key = (curTrainSpamDocCount == trainSpamDocCount) ? 1 : random();
        }

        /* Update the state based on partition. */
        if(key == 0)
        {
            val = "train";
            curTrainSpamDocCount++;
            trainMap.put(file, "spam");
        }
        else
        {
            val = "test";
            testMap.put(file, "spam");
        }
        return val;
    }

    /**
     * Partition ham documents into train and test data.
     * @param file
     *            The input file.
     * @return
     *            Train/test classification.
     */
    public String partitionHam(String file)
    {
        int key = 0;
        String val;
        /* Determine partition. */
        if(trainHamDocCount - curTrainHamDocCount < hamDocCount - curHamDocCount)
        {
            key = (curTrainHamDocCount == trainHamDocCount) ? 1 : random();
        }

        /* Update the state based on partition. */
        if(key == 0)
        {
            val = "train";
            curTrainHamDocCount++;
            trainMap.put(file, "ham");
        }
        else
        {
            val = "test";
            testMap.put(file, "ham");
        }
        return val;
    }

    /**
     * Index a file.
     * @param client
     *            The Elasticsearch client.
     * @param file
     *            The file.
     * @throws IOException
     */
    public void indexFile(Client client, File file)
        throws IOException
    {
        String split;
        if(docMap.get(file.getName()).equals("spam"))
        {
            split = partitionSpam(file.getName());
            curSpamDocCount++;
        }
        else
        {
            split = partitionHam(file.getName());
            curHamDocCount++;
        }

        /* Update the index. */
        client.prepareIndex(Properties.ES_INDEX, "document", "" + file.getName())
              .setSource(XContentFactory.jsonBuilder()
                                        .startObject()
                                        .field("file_name", file.getName())
                                        .field("label"    , docMap.get(file.getName()))
                                        .field("body"     , plainText(file))
                                        .field("split"    , split)
                                        .endObject())
              .execute()
              .actionGet();
    }

    /**
     * Index a document corpus.
     * @param client
     *            The Elasticsearch client.
     * @throws IOException
     */
    public void index(Client client)
        throws IOException
    {
        File[] files = new File(Properties.DIR_INPUT + "/data").listFiles();
        for(File file : files)
        {
            /* Index individual documents. */
            indexFile(client, file);
            if(++curDocCount % 100 == 0)
            {
                Utils.echo("Processed the data for " + curDocCount + " documents");
            }
        }
        /* Serialize maps and output statistics. */
        serializeMaps();
        outputStatistics();
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
        Utils.cout("\n===========");
        Utils.cout("\n| INDEXER |");
        Utils.cout("\n===========");
        Utils.cout("\n");
        Utils.echo("Cluster name   - " + Properties.ES_CLUSTER);
        Utils.echo("Data directory - " + Properties.DIR_INPUT);

        Node node = null;
        try
        {
            Indexer i = new Indexer();
            node = NodeBuilder.nodeBuilder().client(true).clusterName(Properties.ES_CLUSTER).node();
            Client client = node.client();
            i.createDocMap(Properties.DIR_INPUT + "/full/index");
            i.calcStatistics();
            i.index(client);
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
            Utils.elapsedTime(startTime, "\nCreation of index completed");
        }
    }
}
/* End of Indexer.java */