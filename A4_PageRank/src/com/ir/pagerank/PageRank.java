package com.ir.pagerank;

/* Import list */
import java.io.*;
import java.util.Map;
import com.ir.global.*;
import java.util.HashSet;
import java.util.HashMap;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
// import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.action.search.SearchResponse;

/**
 * Author : Asad Shahabuddin
 * Created: Jul 4, 2015
 */

public class PageRank
{
    private HashMap<String, Double> pr;
    private HashMap<String, HashSet<String>> inlinks;
    private HashMap<String, HashSet<String>> outlinks;
    private HashSet<String> sink;
    private int convCount;

    /**
     * Constructor.
     */
    public PageRank()
    {
        pr        = new HashMap<>();
        inlinks   = new HashMap<>();
        outlinks  = new HashMap<>();
        sink      = new HashSet<>();
        convCount = 1;
    }

    /**
     * Create a connectivity (in-links) graph from the unified index.
     * @param client
     *            ElasticSearch API client.
     * @throws IOException
     */
    public void createGraph(Client client)
        throws IOException
    {
        FileWriter fw = new FileWriter(Properties.FILE_GRAPH, true);
        SearchResponse res = client.prepareSearch(Properties.INDEX_NAME)
                                   .setTypes(Properties.INDEX_TYPE)
                                   .setScroll(new TimeValue(1000))
                                   .setQuery(QueryBuilders.matchAllQuery())
                                   .setExplain(true)
                                   .setSize(100)
                                   .execute()
                                   .actionGet();
        int count = 0;

        while(res.getHits().getHits().length != 0)
        {
            for (SearchHit entry : res.getHits().getHits())
            {
                fw.write(String.valueOf(entry.getId()) + " " +
                         String.valueOf(entry.getSource().get("inlinks")) + "\n");
            }

            res = client.prepareSearchScroll(res.getScrollId()).setScroll(
                new TimeValue(1000)).execute().actionGet();
            if(++count % 100 == 0)
            {
                Utils.echo("Processed " + count + " index entries");
            }
        }
        fw.close();
    }

    /**
     * Populate all the data structures from the (in-links) graph.
     * @throws IOException
     */
    public void createDataset()
        throws IOException
    {
        BufferedReader br;
        String line;
        int count = 0;

        for(File file : new File(Properties.DIR_GRAPH).listFiles())
        {
            if(!file.getName().contains("graph_"))
            {
                continue;
            }

            br = new BufferedReader(new FileReader(file));
            while((line = br.readLine()) != null)
            {
                String[] urls = line.split(" ");
                if(!inlinks.containsKey(urls[0]))
                {
                    inlinks.put(urls[0], new HashSet<String>());
                }

                for(int i = 1; i < urls.length; i++)
                {
                    if(!outlinks.containsKey(urls[i]))
                    {
                        outlinks.put(urls[i], new HashSet<String>());
                    }
                    inlinks.get(urls[0]).add(urls[i]);
                    outlinks.get(urls[i]).add(urls[0]);
                }

                if(++count % 1000 == 0)
                {
                    Utils.echo("Processed " + count + " documents");
                }
            }
            br.close();
        }

        /* Purge unnecessary/external links the out-link map. */
        HashSet<String> extNodes = new HashSet<>();
        for(String key : outlinks.keySet())
        {
            if(!inlinks.containsKey(key))
            {
                extNodes.add(key);
            }
        }
        for(String key : extNodes)
        {
            outlinks.remove(key);
        }

        /* Populate the set of sink nodes. */
        sink = new HashSet<>(inlinks.keySet());
        sink.removeAll(outlinks.keySet());

        /* Output to console. */
        Utils.cout("\n>Graph statistics\n");
        Utils.echo("Size of the in-links map  - " + inlinks.size());
        Utils.echo("Size of the out-links map - " + outlinks.size());
        Utils.echo("Number of sink nodes      - " + sink.size());
    }

    /**
     * Populate all the data structures using test data.
     */
    public void createTestDataSet()
    {
        /* In-links : BEGIN */
        inlinks.put("A", new HashSet<String>());
        inlinks.get("A").add("D");
        inlinks.put("B", new HashSet<String>());
        inlinks.get("B").add("C");
        inlinks.get("B").add("D");
        inlinks.get("B").add("E");
        inlinks.get("B").add("F");
        inlinks.get("B").add("G");
        inlinks.get("B").add("H");
        inlinks.get("B").add("I");
        inlinks.put("C", new HashSet<String>());
        inlinks.get("C").add("B");
        inlinks.put("D", new HashSet<String>());
        inlinks.get("D").add("E");
        inlinks.put("E", new HashSet<String>());
        inlinks.get("E").add("F");
        inlinks.get("E").add("G");
        inlinks.get("E").add("H");
        inlinks.get("E").add("I");
        inlinks.get("E").add("J");
        inlinks.get("E").add("K");
        inlinks.put("F", new HashSet<String>());
        inlinks.get("F").add("E");
        inlinks.put("G", new HashSet<String>());
        inlinks.put("H", new HashSet<String>());
        inlinks.put("I", new HashSet<String>());
        inlinks.put("J", new HashSet<String>());
        inlinks.put("K", new HashSet<String>());
        /* In-links : END */

        /* Out-links : BEGIN */
        outlinks.put("B", new HashSet<String>());
        outlinks.get("B").add("C");
        outlinks.put("C", new HashSet<String>());
        outlinks.get("C").add("B");
        outlinks.put("D", new HashSet<String>());
        outlinks.get("D").add("A");
        outlinks.get("D").add("B");
        outlinks.put("E", new HashSet<String>());
        outlinks.get("E").add("B");
        outlinks.get("E").add("D");
        outlinks.get("E").add("F");
        outlinks.put("F", new HashSet<String>());
        outlinks.get("F").add("B");
        outlinks.get("F").add("E");
        outlinks.put("G", new HashSet<String>());
        outlinks.get("G").add("B");
        outlinks.get("G").add("E");
        outlinks.put("H", new HashSet<String>());
        outlinks.get("H").add("B");
        outlinks.get("H").add("E");
        outlinks.put("I", new HashSet<String>());
        outlinks.get("I").add("B");
        outlinks.get("I").add("E");
        outlinks.put("J", new HashSet<String>());
        outlinks.get("J").add("E");
        outlinks.put("K", new HashSet<String>());
        outlinks.get("K").add("E");
        /* Out-links : END */

        /* Sink nodes. */
        sink.add("A");
    }

    /**
     * Rank all the pages iteratively.
     */
    public void rank()
    {
        /*
        VOCABULARY:
        (1) 'inlinks.keySet()' is the set of all pages; |P| = N.
        (2) 'sink' is the set of sink nodes, i.e., pages that have no out-links.
        (3) 'inlinks.get(p)' is the set of pages that link to page p.
        (4) 'outlinks(q).size()' is the number of out-links from page q.
        (5) 'd' is the PageRank damping/teleportation factor; use d = 0.85 as is typical.
        */

        /* Number of nodes in the graph. */
        int N = inlinks.size();
        double sinkPR;
        int iterCount = 1;

        /* Initial value. */
        for(String key : inlinks.keySet())
        {
            pr.put(key, 100 / (double) N);
        }

        while(true)
        {
            HashMap<String, Double> map = new HashMap<>();
            sinkPR = 0;

            /* Calculate the total 'Sink PR'. */
            for(String node : sink)
            {
                sinkPR += pr.get(node);
            }

            for(String k1 : inlinks.keySet())
            {
                /*
                (1) (1 - LAMBDA) corresponds to the user model (direct jumps).
                (2) The rest corresponds to the citation model (links from other pages).
                */
                double v = (1 - Properties.LAMBDA + (Properties.LAMBDA * sinkPR)) / (double) N;
                /* Pages pointing to k1. */
                for(String k2 : outlinks.keySet())
                {
                    if(outlinks.get(k2).contains(k1))
                    {
                        v += (Properties.LAMBDA * pr.get(k2)) / outlinks.get(k2).size();
                    }
                }
                map.put(k1, v);
            }

            /* Test for convergence. */
            if(hasConverged(map))
            {
                Utils.echo("PageRank calculations converged after " + (iterCount - 3) + " iterations");
                output();
                return;
            }
            pr.clear();
            pr.putAll(map);
            iterCount++;
        }
    }

    /**
     * Check if the PageRank calculations have converged.
     * @param map
     *           A map of nodes and their page ranks.
     * @return
     *           true iff the page ranks have converged.
     */
    public boolean hasConverged(HashMap<String, Double> map)
    {
        boolean status = true;
        for(String node : pr.keySet())
        {
            if(Math.floor(pr.get(node) * 100) != Math.floor(map.get(node) * 100))
            {
                status    = false;
            }
        }
        convCount = status ? (convCount + 1) : 1;
        return (convCount == 4) ? true : false;
    }

    /**
     * Output the converged page ranks to console.
     */
    public void output()
    {
        Utils.cout("\n>PageRank values\n");
        for(Map.Entry<String, Double> entry : pr.entrySet())
        {
            Utils.echo(entry.getKey() + " - " + entry.getValue() * 100);
        }
        Utils.cout("\n");
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
        Utils.cout("\n============");
        Utils.cout("\nPAGE RANKING");
        Utils.cout("\n============");
        Utils.cout("\n");

        try
        {
            PageRank pr = new PageRank();
            // Client client = NodeBuilder.nodeBuilder().client(true).clusterName(Properties.CLUSTER_NAME).node().client();
            // pr.createGraph(client);
            Utils.cout(">Creating in-link, out-link and sink data structures\n");
            pr.createDataset();
            pr.rank();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        finally
        {
            Utils.elapsedTime(startTime, "\nPageRank calculation completed.");
        }
    }
}
/* End of PageRank.java */