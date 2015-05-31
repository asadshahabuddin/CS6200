package com.ir.es;

/* Import list */
import java.io.*;
import java.util.*;
import org.elasticsearch.node.Node;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.common.xcontent.*;
import org.tartarus.snowball.ext.PorterStemmer;
import java.util.concurrent.ExecutionException;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.action.search.SearchResponse;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import org.elasticsearch.action.termvector.TermVectorResponse;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.facet.statistical.StatisticalFacet;
import org.elasticsearch.action.termvector.TermVectorRequestBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.MetricsAggregationBuilder;

public class Query
{
    private static Node node;
    private static Client client;
    private static BufferedReader br;
    private static double avgDocLength;
    private static double allDocLength;
    private static PorterStemmer stemmer;
    
    /* File writers */
    private static FileWriter fw1;
    private static FileWriter fw2;
    private static FileWriter fw3;
    private static FileWriter fw4;
    private static FileWriter fw5;
    
    /* Statistic maps */
    private static HashMap<String, Double> docLenMap;
    private static HashMap<String, Double> okapiTfMap;
    private static HashMap<String, Double> tfIdfMap;
    private static HashMap<String, Double> okapibm25Map;
    private static HashMap<String, Double> lmLaplaceMap;
    private static HashMap<String, Double> lmJmMap;
    private static Map<String, Integer> termFreqMap;
    private static HashMap<String, Integer> tfwqMap;
    
    /* Queues */
    private static Queue okapitfq;
    private static Queue tfidfq;
    private static Queue okapibm25q;
    private static Queue lmlaplaceq;
    private static Queue lmjmq;
    
    static
    {
        stemmer = new PorterStemmer();
    }
    
    /* Get a map of all documents with an initial term frequency of 0 (zero) */
    public static void initializeStatMaps()
        throws FileNotFoundException, IOException
    {
        docLenMap    = new HashMap<String, Double>();
        tfwqMap      = new HashMap<String, Integer>();
        okapiTfMap   = new HashMap<String, Double>();
        tfIdfMap     = new HashMap<String, Double>();
        okapibm25Map = new HashMap<String, Double>();
        lmLaplaceMap = new HashMap<String, Double>();
        lmJmMap      = new HashMap<String, Double>();
        br = new BufferedReader(new FileReader(Properties.FILE_DOCLIST));
        String line = br.readLine();
        
        while((line = br.readLine()) != null)
        {
            String docno = line.split("\\s")[1];
            docLenMap.put(docno, getStatsOnTextTerms(client, "ap_dataset",
                                                     "document", "docno",
                                                     docno).getTotal());
            okapiTfMap.put(docno, 0D);
            tfIdfMap.put(docno, 0D);
            okapibm25Map.put(docno, 0D);
            lmLaplaceMap.put(docno, 0D);
            lmJmMap.put(docno, 0D);
        }
    }
    
    /* Reset all maps */
    public static void resetMaps()
    {
        for(Map.Entry<String, Double> entry : docLenMap.entrySet())
        {
            okapiTfMap.put(entry.getKey(), 0D);
            tfIdfMap.put(entry.getKey(), 0D);
            okapibm25Map.put(entry.getKey(), 0D);
            lmLaplaceMap.put(entry.getKey(), 0D);
            lmJmMap.put(entry.getKey(), 0D);
        }
        tfwqMap = new HashMap<String, Integer>();
    }
    
    /* Create a set of all stop list words */
    public static HashSet<String> createStopSet()
        throws FileNotFoundException, IOException
    {
        HashSet<String> stopSet = new HashSet<String>();
        br = new BufferedReader(new FileReader(Properties.FILE_STOPLIST));
        String line = "";
        
        while((line = br.readLine()) != null)
        {
            stopSet.add(line);
        }
        return stopSet;
    }
    
    /* Add the first 1000 entries by score from various maps to their 
    respective queues in descending order */
    public static void sortAndFilterMaps()
    {
        okapitfq   = new Queue();
        tfidfq     = new Queue();
        okapibm25q = new Queue();
        lmlaplaceq = new Queue();
        lmjmq      = new Queue();
        
        for(Map.Entry<String, Double> entry : okapiTfMap.entrySet())
        {
            okapitfq.add(new DocScorePair(entry.getKey(), entry.getValue()));
        }
        for(Map.Entry<String, Double> entry : tfIdfMap.entrySet())
        {
            tfidfq.add(new DocScorePair(entry.getKey(), entry.getValue()));
        }
        for(Map.Entry<String, Double> entry : okapibm25Map.entrySet())
        {
            okapibm25q.add(new DocScorePair(entry.getKey(), entry.getValue()));
        }
        for(Map.Entry<String, Double> entry : lmLaplaceMap.entrySet())
        {
            lmlaplaceq.add(new DocScorePair(entry.getKey(), entry.getValue()));
        }
        for(Map.Entry<String, Double> entry : lmJmMap.entrySet())
        {
            lmjmq.add(new DocScorePair(entry.getKey(), entry.getValue()));
        }
        okapitfq.reverse();
        tfidfq.reverse();
        okapibm25q.reverse();
        lmlaplaceq.reverse();
        lmjmq.reverse();
    }
    
    /* Write the content of queues to respective files */
    public static void writeQueuesToFS(String queryNum)
        throws IOException
    {
        System.out.println("   [echo] Start of FS ops for query " + queryNum);
        StringBuilder buffer1 = new StringBuilder();
        StringBuilder buffer2 = new StringBuilder();
        StringBuilder buffer3 = new StringBuilder();
        StringBuilder buffer4 = new StringBuilder();
        StringBuilder buffer5 = new StringBuilder();
        fw1 = new FileWriter(Properties.FILE_OKAPITF, true);
        fw2 = new FileWriter(Properties.FILE_TFIDF, true);
        fw3 = new FileWriter(Properties.FILE_OKAPIBM25, true);
        fw4 = new FileWriter(Properties.FILE_LMLAPLACE, true);
        fw5 = new FileWriter(Properties.FILE_LMJM, true);
        DocScorePair dfp1 = null;
        DocScorePair dfp2 = null;
        DocScorePair dfp3 = null;
        DocScorePair dfp4 = null;
        DocScorePair dfp5 = null;
        int rank = 0;
        
        while((dfp1 = okapitfq.remove())   != null &&
              (dfp2 = tfidfq.remove())     != null &&
              (dfp3 = okapibm25q.remove()) != null &&
              (dfp4 = lmlaplaceq.remove()) != null &&
              (dfp5 = lmjmq.remove()) != null)
        {
            buffer1.append(queryNum + " Q0 " + dfp1.getDocNo() + " " +
                           ++rank   + " "    + dfp1.getScore() + " Exp\n");
            buffer2.append(queryNum + " Q0 " + dfp2.getDocNo() + " " +
                           rank     + " "    + dfp2.getScore() + " Exp\n");
            buffer3.append(queryNum + " Q0 " + dfp3.getDocNo() + " " +
                           rank     + " "    + dfp3.getScore() + " Exp\n");
            buffer4.append(queryNum + " Q0 " + dfp4.getDocNo() + " " +
                           rank     + " "    + dfp4.getScore() + " Exp\n");
            buffer5.append(queryNum + " Q0 " + dfp5.getDocNo() + " " +
                           rank     + " "    + dfp5.getScore() + " Exp\n");
        }
        
        /*
        (1) Write to file
        (2) Flush and close the file    
        */
        fw1.write(buffer1.toString());
        fw2.write(buffer2.toString());
        fw3.write(buffer3.toString());
        fw4.write(buffer4.toString());
        fw5.write(buffer5.toString());
        fw1.flush();
        fw2.flush();
        fw3.flush();
        fw4.flush();
        fw5.flush();
        fw1.close();
        fw2.close();
        fw3.close();
        fw4.close();
        fw5.close();
        System.out.println("   [echo] End of FS ops for query " + queryNum);
    }
    
    /* Create a map to store frequencies of various terms in a query */
    public static void populateTfwqMap(String q)
    {
        String[] terms = q.split("\\s+|-");
        for(String term : terms)
        {
            if(!term.equalsIgnoreCase("u.s."))
            {
                term = filterText(term).toLowerCase();
            }
            stemmer.setCurrent(term);
            stemmer.stem();
            if(!tfwqMap.containsKey(stemmer.getCurrent()))
            {
                tfwqMap.put(stemmer.getCurrent(), 0);
            }
            tfwqMap.put(stemmer.getCurrent(),
                        tfwqMap.get(stemmer.getCurrent()) + 1);
        }
    }
    
    /* Filter punctuation and unnecessary characters */
    public static String filterText(String s)
    {
        while(Properties.PUNCTUATIONS.contains(s.charAt(s.length() - 1)))
        {
            s = s.substring(0, s.length() - 1);
        }
        while(Properties.PUNCTUATIONS.contains(s.charAt(0)))
        {
            s = s.substring(1, s.length());
        }
        return s;
    }
    
    /* Execute queries */
    public static void parseAndExecQueries(HashSet<String> stopSet)
        throws FileNotFoundException, IOException
    {
        br = new BufferedReader(new FileReader(Properties.FILE_QUERY));
        String line = "";
        int queryCount = 0;
        
        while((line = br.readLine()) != null &&
              line.length() > 0)
        {
            System.out.println("   [echo] Start of processing for query #" + ++queryCount);
            int idx = -1;
            int wordIdx = 0;
            String queryNum = "";
            HashSet<String> termSet = new HashSet<String>();
            
            /* Derive query number */
            while(line.charAt(++idx) != ' ')
            {
                queryNum += line.charAt(idx);
            }
            queryNum = queryNum.substring(0, queryNum.length() - 1);
            
            /* Derive query contents and create 'tfwq' map */
            while(line.charAt(++idx) == ' ');
            line = line.substring(idx);
            populateTfwqMap(line);
            
            for(String term : line.split("\\s+|-"))
            {
                if(!term.equalsIgnoreCase("u.s."))
                {
                    term = filterText(term).trim().toLowerCase();
                }
                if(++wordIdx < 4 ||
                   stopSet.contains(term))
                {
                    continue;
                }
                stemmer.setCurrent(term);
                stemmer.stem();
                execQuery(stemmer.getCurrent(), line);
                termSet.add(term);
            }
            sortAndFilterMaps();
            writeQueuesToFS(queryNum);
            resetMaps();
            System.out.println("   [echo] End of processing for query #" + queryCount);
        }
    }
    
    /* Execute a query */
    public static void execQuery(String term, String query)
        throws IOException
    {
        termFreqMap = queryTF(client, matchQuery("text", term), "ap_dataset", "document");
        String key = "";
        double sumTfwd = 0; 
        
        for(Map.Entry<String, Integer> termFreqEntry : termFreqMap.entrySet())
        {
            key = termFreqEntry.getKey();
            double okapiTf = Model.okapitf(termFreqEntry.getValue(),
                                           docLenMap.get(key),
                                           avgDocLength);
            sumTfwd += termFreqEntry.getValue();
            
            okapiTfMap.put(key,
                           okapiTfMap.get(key) +
                           okapiTf);
            
            tfIdfMap.put(key,
                         tfIdfMap.get(key) +
                         Model.tfidf(okapiTf, termFreqMap.size()));
            
            okapibm25Map.put(key,
                             okapibm25Map.get(key) +
                             Model.okapibm25((double) termFreqMap.size(),
                                             (double) termFreqEntry.getValue(),
                                             docLenMap.get(key),
                                             avgDocLength,
                                             tfwqMap.get(term)));
        }
        
        for(Map.Entry<String, Double> entry : docLenMap.entrySet())
        {
            double tfwd = 0;
            key = entry.getKey();
            
            if(termFreqMap.containsKey(key))
            {
                tfwd = termFreqMap.get(key);
            }
            lmLaplaceMap.put(key, lmLaplaceMap.get(key) + Model.plaplace(tfwd, entry.getValue()));
            lmJmMap.put(key, lmJmMap.get(key) + Model.pjm(tfwd, entry.getValue(), (sumTfwd / allDocLength)));
        }
    }

    /**
     * V is the vocabulary size – the total number of unique terms in the collection.
     * @param client
     * @param index
     * @param type
     * @param field
     * @return
     */
    public static long getVocabularySize(Client client, String index,
                                          String type  , String field)
    {
        @SuppressWarnings("rawtypes")
        MetricsAggregationBuilder aggregation = AggregationBuilders.cardinality("agg").field(field);
        SearchResponse sr = client.prepareSearch(index).setTypes(type).addAggregation(aggregation).execute().actionGet();
        Cardinality agg = sr.getAggregations().get("agg");
        long value = agg.getValue();
        return value;
    }

    /**
     * return Pairs of <"decno", tf value> by given term query.
     * @param client
     * @param qb
     * @param index
     * @param type
     * @return
     */
    public static Map<String, Integer> queryTF(Client client, QueryBuilder qb,
                                               String index, String type)
    {
        SearchResponse scrollResp = client.prepareSearch(index)
                                          .setTypes(type)
                                          .setScroll(new TimeValue(6000))
                                          .setQuery(qb)
                                          .setExplain(true)
                                          .setSize(1000).execute().actionGet();

        /* No query matched */
        if (scrollResp.getHits().getTotalHits() == 0)
        {
            return new HashMap<String, Integer>();
        }
        Map<String, Integer> results = new HashMap<>();
        while (true)
        {
            for (SearchHit hit : scrollResp.getHits().getHits())
            {
                String docno = (String) hit.getSource().get("docno");
                int tf =  (int) hit.getExplanation().getDetails()[0].getDetails()[0].getDetails()[0].getValue();
                results.put(docno, tf);
            }
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(
                                                    new TimeValue(6000)).execute().actionGet();
            if (scrollResp.getHits().getHits().length == 0)
            {
                break;
            }
        }
        return results;
    }
    
    /**
     * Get statistical facet by given docno or whole documents
     * INFO including following:
     "facets": {
        "text": {
             "_type": "statistical",
             "count": 84678,
             "total": 18682561,
             "min": 0,
             "max": 802,
             "mean": 220.63063605659084,
             "sum_of_squares": 4940491417,
             "variance": 9666.573376838636,
             "std_deviation": 98.31873360066552
        }
     }
     * @param client
     * @param index
     * @param type
     * @param matchedField
     * @param matchedValue
     * @return
     * @throws IOException
     */
    public static StatisticalFacet getStatsOnTextTerms(Client client,
                                                       String index,
                                                       String type,
                                                       String matchedField,
                                                       String matchedValue)
        throws IOException
    {
        XContentBuilder facetsBuilder;
        if (matchedField == null && matchedValue == null)  /* Match_all docs */
        {
            facetsBuilder = getStatsTermsBuilder();
        }
        else
        {
            facetsBuilder = getStatsTermsByMatchFieldBuilder(matchedField, matchedValue);
        }
        SearchResponse response = client.prepareSearch(index).setTypes(type)
                                        .setSource(facetsBuilder)
                                        .execute()
                                        .actionGet();
        StatisticalFacet f = (StatisticalFacet) response.getFacets().facetsAsMap().get("text");
        return f;
    }

    /**
     * Builder for facets statistical terms length by given matched field, like docno.
     * In Sense:
     *
     POST ap_dataset/document/_search
     {
        "query": {
             "match": {
                 "docno": "AP891216-0142"
            }
        },
        "facets": {
            "text": {
                "statistical": {
                    "script": "doc['text'].values.size()"
                }
            }
        }
     }
     * @param matchField
     * @param matchValue
     * @return
     * @throws IOException
     */
    public static XContentBuilder getStatsTermsByMatchFieldBuilder(String matchField,
                                                                   String matchValue)
        throws IOException
    {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject()
               .startObject("query")
               .startObject("match")
               .field(matchField, matchValue)
               .endObject()
               .endObject()
               .startObject("facets")
               .startObject("text")
               .startObject("statistical")
               .field("script", "doc['text'].values.size()")
               .endObject()
               .endObject()
               .endObject()
               .endObject();
        return builder;
    }

    /**
     * Builder for the facets statistical terms length by whole documents.
     * In Sense:
     * POST /ap_dataset/document/_search
        {
         "query": {"match_all": {}},
            "facets": {
                "text": {
                    "statistical": {
                         "script": "doc['text'].values.size()"
                    }
                 }
             }
         }
     * @return
     * @throws IOException
     */
    public static XContentBuilder getStatsTermsBuilder()
        throws IOException
    {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject()
               .startObject("query")
               .startObject("match_all")
               .endObject()
               .endObject()
               .startObject("facets")
               .startObject("text")
               .startObject("statistical")
               .field("script", "doc['text'].values.size()")
               .endObject()
               .endObject()
               .endObject()
               .endObject();
        return builder;
    }
    
    /* Main method for unit testing */
    public static void main(String[] args)
        throws IOException, InterruptedException, ExecutionException
    {
        /* Starts client */
        node = nodeBuilder().client(true).clusterName("leoscluster").node();
        client = node.client();
        
        /* BLOCK 3 - Execute queries */
        allDocLength = getStatsOnTextTerms(client, "ap_dataset", "document", null, null).getTotal();
        avgDocLength = allDocLength / 84678;
        initializeStatMaps();
        parseAndExecQueries(createStopSet());
        node.close();
    }
    
    /* Under construction */
    public static void createDocLengthMap()
        throws ExecutionException, InterruptedException, IOException
    {
        TermVectorRequestBuilder builder = new TermVectorRequestBuilder(client, "ap_dataset", "document", "0");
        TermVectorResponse response = builder.setSelectedFields("text")
                                             .setTermStatistics(true)
                                             .setFieldStatistics(true)
                                             .execute()
                                             .actionGet();
        XContentBuilder xcbuilder = XContentFactory.contentBuilder(XContentType.JSON).prettyPrint();
        response.toXContent(xcbuilder, ToXContent.EMPTY_PARAMS);
        System.out.println(xcbuilder.toString());
    }
}

/* Usage information */
/* BLOCK 1 - Get vocabulary size */
/*
System.out.println("   [echo] Vocabulary size is " +
                   getVocabularySize(client, "ap_dataset", "document", "text"));

BufferedReader br = new BufferedReader(new FileReader("doclist.txt"));
String line = "";
StatisticalFacet facet = null;
long totalLength = 0;
*/

/* BLOCK 2 - Get document length */
/*
while((line = br.readLine()) != null)
{
    facet = getStatsOnTextTerms(client, "ap_dataset", "document", "docno", line.split(" ")[1]);
    // System.out.println("Document number " + line.split(" ")[1] + " has length " + facet.getTotal());
    totalLength += facet.getTotal();
}
System.out.println("   [echo] Total length (by summation) is " + totalLength);
facet = getStatsOnTextTerms(client, "ap_dataset", "document", null, null);
System.out.println("   [echo] Total length is " + facet.getTotal());
br.close();
*/
/* End of Query.java */