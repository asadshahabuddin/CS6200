package com.ir.es;

/* Import list */
import java.io.*;
import java.util.List;
import java.util.ArrayList;

import org.elasticsearch.node.Node;
import org.elasticsearch.client.Client;
// import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

// import org.elasticsearch.client.transport.TransportClient;
// import org.elasticsearch.common.settings.ImmutableSettings;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
// import org.elasticsearch.common.transport.InetSocketTransportAddress;

/**
 * Created by Rainicy on 1/13/15.
 */

/** 
 * Edited by Vanessa Murdock on 2/5/15:  
 * removed code for parsing the documents, 
 * added a few comments
 */

/* 
 * Depending on your setup, you may need to add two jar files to your BuildPath:
 * elasticsearch-1.4.2.jar
 * lucene-core-4.10.2.jar
 * They can be found in the lib directory of your elasticsearch installation.
 */

public class BuildIndex
{
    public static void main(String[] args) throws Exception
    {
    	/* Calculate start time */
        long startTime = System.nanoTime();
        
        if (args.length != 1)
        {
            throw new IllegalArgumentException("Only Need config file.");
        }

        Config config = new Config(args[0]);
        String folder = config.getString("input.folder");
        String clusterName = config.getString("cluster.name");

        System.out.println("   [echo] Input folder: " + folder);
        System.out.println("   [echo] Cluster name: " + clusterName);

        // Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", clusterName).build();
        // Client client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
        Node node = nodeBuilder().client(true).clusterName(clusterName).node();
        Client client = node.client();

        // Settings
        // 1) Set settings and analysis
        /*
        XContentBuilder settingsBuilder = getSettingsBuilder();
        client.admin().indices().prepareCreate("ap_dataset")
              .setSettings(ImmutableSettings.settingsBuilder().loadFromSource(settingsBuilder.string()))
              .execute()
              .actionGet();
        
        // 2) Set mapping
        XContentBuilder mappingBuilder = getMappingBuilder();
        client.admin().indices().preparePutMapping("ap_dataset")
              .setType("document")
              .setSource(mappingBuilder)
              .execute()
              .actionGet();
        */
        // 3) Index files to documents
        File[] files = listFiles(folder);
        // Index, starting from 0
        int id = 0;
        for (File file : files)
        {
            // Parse the file and return a list of JSON documents
            List<XContentBuilder> builders = createBuilders(file);
            
            // Iterate through the list of documents and index each one
            for (XContentBuilder builder : builders)
            {
                // System.out.println("ID: " + id);
                @SuppressWarnings("unused")
				IndexResponse response = client.prepareIndex("ap_dataset", "document", "" + id)
                                               .setSource(builder)
                                               .execute()
                                               .actionGet();
                ++id;
            }
            // System.out.println("Final ID: " + id);
        }

        node.close();
        elapsedTime(startTime, "Creation of index completed");
    }
    
    /*
    (1) Output time elapsed since last checkpoint
    (2) Return start time for this checkpoint
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

    public static XContentBuilder getMappingBuilder()
        throws IOException
    {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject()
               .startObject("document")
               .startObject("properties")
               .startObject("docno")
               .field("type", "string")
               .field("store", true)
               .field("index", "not_analyzed")
               .endObject()
               .startObject("text")
               .field("type", "string")
               .field("store", true)
               .field("index", "analyzed")
               .field("term_vector", "with_positions")
               .field("analyzer", "my_english")
               .endObject()
               .endObject()
               .endObject()
               .endObject();
        return builder;
    }

    public static XContentBuilder getSettingsBuilder()
        throws IOException
    {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject()
               .startObject("settings")
               .startObject("index")
               .startObject("score")
               .field("type", "default")
               .endObject()
               .field("number_of_shards", 1)
               .field("number_of_replicas", 1)
               .endObject()
               .endObject()
               .startObject("analysis")
               .startObject("analyzer")
               .startObject("my_english")
               .field("type", "english")
               .field("stopwords_path", "E:/InformationRetrieval_A1/stoplist.txt")
               .endObject()
               .endObject()
               .endObject()
               .endObject();
        return builder;
    }
    
    /* List files in the directory */
    public static File[] listFiles(String dirName)
    {
        return new File(dirName).listFiles();
    }
    
    /* Create content builders for each file */
    public static List<XContentBuilder> createBuilders(File file)
        throws IOException
    {
        ArrayList<XContentBuilder> builderList = new ArrayList<XContentBuilder>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line  = "";
        String docNo = "";
        StringBuilder text  = new StringBuilder();
        
        while((line = br.readLine()) != null)
        {
            if(line.equals("<DOC>"))
            {
                while(!(line = br.readLine()).contains("</DOC>"))
                {
                    if(line.contains("<DOCNO>"))
                    {
                        docNo = line.substring(line.indexOf(">") + 1,
                                               line.indexOf("</")).trim();
                    }
                    if(line.contains("<TEXT>"))
                    {
                        while(!(line = br.readLine()).contains("</TEXT>"))
                        {
                            text.append(line + " ");
                        }
                    }
                }
                
                builderList.add(XContentFactory.jsonBuilder()
                                               .startObject()
                                               .field("docno", docNo)
                                               .field("text" , text)
                                               .endObject());
                text  = new StringBuilder();
            }
        }
        
        br.close();
        return builderList;
    }
}
/* End of BuildIndex.java */