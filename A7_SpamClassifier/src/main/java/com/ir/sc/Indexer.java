package com.ir.sc;

/* Import list */
import java.io.*;
import java.util.List;
import org.jsoup.Jsoup;
import java.util.HashMap;
import java.util.ArrayList;
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
import org.elasticsearch.common.xcontent.XContentBuilder;

/**
 * Author : Asad Shahabuddin
 * Created: Aug 9, 2015
 */

public class Indexer
{
    private int trainDocCount;
    private int spamDocCount;
    private int hamDocCount;
    private int trainSpamDocCount;

    /**
     * Constructor
     */
    public Indexer()
    {
        spamDocCount      = 0;
        hamDocCount       = 0;
        trainDocCount     = 0;
        trainSpamDocCount = 0;
    }

    /**
     * Create a map of document names with spam/ham classification.
     * @param file
     *            The input file.
     * @return
     *            The map of document names with said classification.
     * @throws IOException
     */
    public HashMap<String, Boolean> createDocMap(String file)
        throws IOException
    {
        HashMap<String, Boolean> map = new HashMap<String, Boolean>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while((line = br.readLine()) != null)
        {
            if(line.contains("spam"))
            {
                map.put(line.substring(line.indexOf("inmail")), true);
                spamDocCount++;
            }
            else
            {
                map.put(line.substring(line.indexOf("inmail")), false);
                hamDocCount++;
            }
        }
        return map;
    }

    /**
     * Calculate corpus statistics.
     */
    public void calcStatistics()
    {
        int totalDocCount = spamDocCount + hamDocCount;
        trainDocCount = (totalDocCount * 4) / 5;
        trainSpamDocCount = (spamDocCount * 4) / 5;
    }

    /**
     * Output corpus statistics to the console.
     */
    public void outputStatistics()
    {
        Utils.cout("\n>Statistics\n");
        Utils.cout("Total      - " + (spamDocCount + hamDocCount) + "\n");
        Utils.cout("Spam       - " + spamDocCount + "\n");
        Utils.cout("Ham        - " + hamDocCount + "\n");
        Utils.cout("Train      - " + trainDocCount + "\n");
        Utils.cout("Test       - " + (spamDocCount + hamDocCount - trainDocCount) + "\n");
        Utils.cout("Train spam - " + trainSpamDocCount + "\n");
        Utils.cout("Test spam  - " + (spamDocCount - trainSpamDocCount) + "\n");
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
     * @param content
     *            The formatted text.
     * @return
     *            The plain text.
     * @throws IOException
     */
    public String plainText(String content)
        throws IOException
    {
        Document document = Jsoup.parse(content);
        String s = document.body().text();
        return s.substring(s.indexOf("][ ") + 3)
                .replaceAll(" ]]", "");
    }

    /**
     * Main method for unit testing.
     * @param args
     *            Program arguments.
     */
    public static void main(String[] args)
        throws IOException
    {
        /* Calculate start time. */
        long startTime = System.nanoTime();
        Utils.cout("\n>Indexer\n");
        Utils.echo("Cluster name   - " + Properties.ES_CLUSTER);
        Utils.echo("Data directory - " + Properties.DIR_INPUT);

        Node node = NodeBuilder.nodeBuilder()
                               .client(true)
                               .clusterName(Properties.ES_CLUSTER)
                               .node();
        Client client = node.client();
        File[] files = new File(Properties.DIR_INPUT).listFiles();
        for(File file : files)
        {
            // TODO
        }
    }
}
/* End of Indexer.java */