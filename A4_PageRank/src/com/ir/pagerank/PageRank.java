package com.ir.pagerank;

/* Import list */
import java.io.*;
import java.util.Map;
import com.ir.global.*;
import java.util.HashSet;
import java.util.HashMap;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
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
    private HashMap<String, Integer> outlinks;
    private HashSet<String> sink;
    private Queue queue;
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
        queue     = new Queue();
        convCount = 1;
    }

    /**
     * Create a connectivity (in-links) graph from the unified index.
     * @param client
     *            The ElasticSearch client object.
     * @throws IOException
     */
    @SuppressWarnings("unused")
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
     * Populate all data structures from the (in-links) graph.
     * @throws IOException
     */
    public void createDataSet()
        throws IOException
    {
        BufferedReader br;
        String line;
        int count = 0;

        for(File file : new File(Properties.DIR_GRAPH2).listFiles())
        {
            if(file.getName().contains("graph-"))
            {
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
                            outlinks.put(urls[i], 0);
                        }
                        inlinks.get(urls[0]).add(urls[i]);
                        outlinks.put(urls[i], outlinks.get(urls[i]) + 1);
                    }

                    /* Output status at regular intervals. */
                    if(++count % 1000 == 0)
                    {
                        Utils.echo("Processed " + count + " documents");
                    }
                }
                br.close();
            }
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
     * Populate all data structures using test data.
     * @throws IOException
     */
    @SuppressWarnings("unused")
    public void createTestDataSet()
        throws IOException
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
        outlinks.put("B", 1);
        outlinks.put("C", 1);
        outlinks.put("D", 2);
        outlinks.put("E", 3);
        outlinks.put("F", 2);
        outlinks.put("G", 2);
        outlinks.put("H", 2);
        outlinks.put("I", 2);
        outlinks.put("J", 1);
        outlinks.put("K", 1);
        /* Out-links : END */

        /* Sink nodes. */
        sink.add("A");
    }

    /**
     * Check if all the nodes are valid in-link keys.
     */
    @SuppressWarnings("unused")
    public void check()
    {
        for(String k1 : inlinks.keySet())
        {
            for(String k2 : inlinks.get(k1))
            {
                if(!inlinks.containsKey(k2))
                {
                    Utils.echo(k2 + " is not an in-link key");
                }
            }
        }
    }

    /**
     * Check if the scores have converged.
     * @param map
     *           A map of nodes and their page ranks.
     * @return
     *           true iff the scores have converged.
     */
    public boolean hasConverged(HashMap<String, Double> map)
    {
        boolean status = true;
        for(String node : pr.keySet())
        {
            if(Math.floor(pr.get(node) * 100000) != Math.floor(map.get(node) * 100000))
            {
                status = false;
                break;
            }
        }
        convCount = status ? (convCount + 1) : 1;
        return convCount == 4;
    }

    /**
     * Rank all the pages iteratively.
     * @return
     *            true iff the scores converge.
     */
    public boolean rank()
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
            pr.put(key, 1 / (double) N);
        }

        while(iterCount <= 100)
        {
            Utils.echo("Completed iteration " + iterCount);
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
                for(String k2 : inlinks.get(k1))
                {
                    if(inlinks.containsKey(k2))
                    {
                        v += (Properties.LAMBDA * pr.get(k2)) / outlinks.get(k2);
                    }
                }
                map.put(k1, v);
            }

            /* Test for convergence. */
            if(hasConverged(map))
            {
                Utils.echo("PageRank calculations converged after " + (iterCount - 3) + " iterations");
                return true;
            }
            pr.clear();
            pr.putAll(map);
            iterCount++;
        }

        return false;
    }

    /**
     * Output the converged page ranks to console.
     */
    @SuppressWarnings("unused")
    public void outputMap()
    {
        Utils.cout("\n>PageRank values\n");
        for(Map.Entry<String, Double> entry : pr.entrySet())
        {
            Utils.echo(entry.getKey() + " - " + entry.getValue());
        }
        Utils.cout("\n");
    }

    /**
     * Populate the queue with nodes and their page ranks.
     */
    public void createQueue()
    {
        for(String key : pr.keySet())
        {
            queue.add(new NodeScorePair(key, pr.get(key)));
        }
        queue.reverse();
    }

    /**
     * Output the top 500 nodes by PageRank score.
     */
    public void outputQueue()
    {
        NodeScorePair nsp;
        double sum = 0;
        while((nsp = queue.remove()) != null)
        {
            Utils.cout(nsp.getNode() + " has a score of " + nsp.getScore() * 100 + "\n");
            sum += nsp.getScore();
        }
        Utils.cout("\nTotal PR distribution is " + sum + "\n");
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
            Utils.cout(">Creating in-link, out-link and sink data structures\n");
            pr.createDataSet();
            Utils.cout("\n>Calculating page ranks for all the pages\n");
            if(pr.rank())
            {
                Utils.cout("\n>Creating the priority queue\n");
                pr.createQueue();
                Utils.cout("\n>Top 500 by PageRank score\n");
                pr.outputQueue();
            }
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