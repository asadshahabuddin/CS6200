package com.ir.index;

/* Import list */
import java.io.*;
import java.util.*;

import com.ir.global.Utils;
import com.ir.global.Properties;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.node.Node;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.json.JSONException;
import org.json.JSONObject;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 28, 2015
 */

public class Indexer
{
    private static Node node;
    private static int id;

    /**
     * Constructor.
     */
    public Indexer()
    {
        node = null;
        id   = 0;
    }

    /**
     * Write the set of indexed URLs to the file system.
     * @param set
     *           The set of indexed URLs.
     * @return
     *           'true' if the write is successful.
     * @throws IOException
     */
    public boolean writeIndexedSet(HashSet<String> set)
        throws IOException
    {
        if(set == null || set.size() == 0)
        {
            return false;
        }
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(Properties.FILE_INDEXED_OBJ));
        out.writeObject(set);
        out.close();
        return true;
    }

    /**
     * Get the set of indexed URLs.
     * @return
     *           The set of indexed URLs.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public HashSet<String> createIndexedSet()
        throws IOException, ClassNotFoundException
    {
        HashSet<String> set = new HashSet<>();
        if(new File(Properties.FILE_INDEXED_OBJ).exists())
        {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(Properties.FILE_INDEXED_OBJ));
            set = (HashSet<String>) in.readObject();
            in.close();
        }
        return set;
    }

    /**
     * Create mappings for various fields in the data set.
     * @return
     *            The resulting content builder.
     * @throws IOException
     */
    public XContentBuilder getMappingBuilder()
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

    /**
     * Configure Elasticsearch settings.
     * @return
     *            The resulting content builder.
     * @throws IOException
     */
    public XContentBuilder getSettingsBuilder()
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

    /**
     * List all the files in the specified directory.
     * @param dirName
     *            The directory name.
     * @return
     *            The list of files in the directory.
     */
    public File[] listFiles(String dirName)
    {
        return new File(dirName).listFiles();
    }

    /**
     * Create a map of all the in-link entries from the graph file.
     * @return
     *            The resulting map.
     * @throws IOException
     */
    public HashMap<String, ArrayList<String>> createInLinkMap(String file)
        throws IOException
    {
        HashMap<String, ArrayList<String>> map = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        while((line = br.readLine()) != null)
        {
            String[] words = line.split(" ");
            ArrayList<String> list = new ArrayList<>();

            if(map.containsKey(words[0]))
            {
                Utils.warning(words[0] + " is a repeated document");
            }
            for(int i = 1; i < words.length; i++)
            {
                list.add(words[i]);
            }
            map.put(words[0], list);
        }

        return map;
    }

    /**
     * Parse the contents of a file.
     * @param file
     *            The file.
     * @return
     *            The resulting content builder.
     * @throws IOException
     */
    public List<XContentBuilder> createBuilders(File file, HashMap<String, ArrayList<String>> graph)
        throws IOException
    {
        ArrayList<XContentBuilder> builderList = new ArrayList<XContentBuilder>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        String docNo    = "";
        String outLinks = "";
        StringBuilder text    = new StringBuilder();
        StringBuilder html    = new StringBuilder();
        StringBuilder inLinks = new StringBuilder();

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
                    else if(line.contains("<OUTLINKS>"))
                    {
                        outLinks = line.substring(line.indexOf(">") + 1,
                                                  line.indexOf("</")).trim();
                    }
                    else if(line.contains("<TEXT>"))
                    {
                        while(!(line = br.readLine()).contains("</TEXT>"))
                        {
                            text.append(line + " ");
                        }
                    }
                    else if(line.contains("<HTML>"))
                    {
                        while(!(line = br.readLine()).contains("</HTML>"))
                        {
                            html.append(line + " ");
                        }
                    }
                }
                for(String s : graph.get(docNo))
                {
                    inLinks.append(s + " ");
                }
                graph.remove(docNo);

                builderList.add(XContentFactory.jsonBuilder()
                           .startObject()
                           .field("docno", docNo)
                           .field("text", text.toString())
                           .field("inlinks", inLinks.toString().trim())
                           .field("outlinks", outLinks)
                           .field("html", html)
                           .endObject());
                text    = new StringBuilder();
                html    = new StringBuilder();
                inLinks = new StringBuilder();
            }
        }

        br.close();
        return builderList;
    }

    /**
     * Main method for unit testing.
     * @param args
     *            Program arguments.
     * @throws Exception
     */
    public static void main(String[] args)
    {
    	/* Calculate start time. */
        long startTime = System.nanoTime();
        Utils.cout("\n");
        Utils.cout("=======\n");
        Utils.cout("INDEXER\n");
        Utils.cout("=======\n");

        if (args.length < 3)
        {
            Utils.cout("USAGE  : Indexer <cluster_name> <graph_file> <data_folder>\n");
            Utils.cout("EXAMPLE: Indexer es-cluster ~/graph.txt ~/es-data\n");
        }

        Utils.echo("Cluster name   - " + args[0]);
        Utils.echo("Graph          - " + args[1]);
        Utils.echo("Data directory - " + args[2]);

        try
        {
            Indexer i = new Indexer();
            node = nodeBuilder().client(true).clusterName(args[0]).node();
            Client client = node.client();

            /* Create the in-link map. */
            Utils.cout("\n>Loading the graph into memory\n");
            HashMap<String, ArrayList<String>> graph = i.createInLinkMap(args[1]);
            /* Index files to documents. */
            File[] files = i.listFiles(args[2]);

            /* Index, starting from 0. */
            Utils.cout("\n>Creating the index\n");
            for (File file : files)
            {
                /* Parse the file and return a list of JSON documents. */
                List<XContentBuilder> builders = i.createBuilders(file, graph);

                /* Iterate through the list of documents and index each one. */
                for (XContentBuilder builder : builders)
                {
                    if(id % 1000 == 0)
                    {
                        Utils.echo("Processed the data for " + id + " documents");
                    }
                    String docNo = String.valueOf(new JSONObject(XContentHelper.convertToJson(
                        builder.bytes(), false)).get("docno"));
                    SearchResponse res = client.prepareSearch("ras_dataset")
                                               .setTypes("document")
                                               .setQuery(QueryBuilders.matchQuery("docno", docNo))
                                               .setExplain(true)
                                               .execute()
                                               .actionGet();
                    if(!String.valueOf(res.getHits().getHits()[0].getSource().get("docno")).equals(docNo))
                    {
                        client.prepareIndex("ras_dataset", "document", "" + docNo)
                              .setSource(builder)
                              .execute()
                              .actionGet();
                    }
                    else
                    {
                        HashSet<String> inLinkSet = new HashSet<>(
                            Arrays.asList(res.getHits().getHits()[0].getSource().get("inlinks").toString().split(" ")));
                        Collections.addAll(inLinkSet, String.valueOf(
                            new JSONObject(XContentHelper.convertToJson(builder.bytes(), false)).get("inlinks")).split(" "));
                        StringBuilder inLinks = new StringBuilder();
                        for(String s : inLinkSet)
                        {
                            inLinks.append(s + " ");
                        }
                        client.update(new UpdateRequest("ras_dataset", "document", "" + docNo)
                                      .doc(XContentFactory.jsonBuilder()
                                           .startObject()
                                           .field("inlinks", "http://www.google.com/")
                                           .endObject())).get();
                    }
                    ++id;
                }
            }
            Utils.echo("Processed the data for " + id + " documents");
        }
        catch(Exception e)
        {
            Utils.error("Exception in main(...)");
            Utils.cout(">Stack trace\n");
            e.printStackTrace();
            Utils.cout("\n");
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