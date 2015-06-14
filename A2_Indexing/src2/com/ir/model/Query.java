package com.ir.model;

/* Import list */
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.io.FileReader;
import java.io.FileWriter;
import com.ir.index.Entry;
import java.io.IOException;
import com.ir.global.Utils;
import java.io.BufferedReader;
import com.ir.global.Properties;
import com.ir.global.SearchClient;
import org.tartarus.snowball.ext.PorterStemmer;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 13, 2015
 */

public class Query
{
    private static boolean stopSwitch;
    private static boolean stemSwitch;
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
    private static Map<String, Entry> termFreqMap;
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

    /*
    (1) Create a map of all documents and respective lengths.
    (2) Calculate the total and average document length for all
        documents in the corpus.
    */
    public static void initializeStatMaps()
        throws IOException
    {
        docLenMap    = new HashMap<String, Double>();
        tfwqMap      = new HashMap<String, Integer>();
        okapiTfMap   = new HashMap<String, Double>();
        tfIdfMap     = new HashMap<String, Double>();
        okapibm25Map = new HashMap<String, Double>();
        lmLaplaceMap = new HashMap<String, Double>();
        lmJmMap      = new HashMap<String, Double>();
        br = new BufferedReader(new FileReader(
            Properties.DIR_OBJ + "/" + Properties.FILE_DOCLEN_TXT));
        String line = "";

        while((line = br.readLine()) != null)
        {
            String[] docStats = line.split("\\s");
            docLenMap.put(docStats[0], Double.valueOf(docStats[1]));
            okapiTfMap.put(docStats[0], 0D);
            tfIdfMap.put(docStats[0], 0D);
            okapibm25Map.put(docStats[0], 0D);
            lmLaplaceMap.put(docStats[0], 0D);
            lmJmMap.put(docStats[0], 0D);
            allDocLength += Double.valueOf(docStats[1]);
        }
        avgDocLength = allDocLength / Properties.COUNT_DOC;
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
            if(!entry.getValue().isNaN())
            {
                lmjmq.add(new DocScorePair(entry.getKey(), entry.getValue()));
            }
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
        fw1 = new FileWriter(Properties.DIR_MODEL + "/" + Properties.FILE_OKAPITF, true);
        fw2 = new FileWriter(Properties.DIR_MODEL + "/" + Properties.FILE_TFIDF, true);
        fw3 = new FileWriter(Properties.DIR_MODEL + "/" + Properties.FILE_OKAPIBM25, true);
        fw4 = new FileWriter(Properties.DIR_MODEL + "/" + Properties.FILE_LMLAPLACE, true);
        fw5 = new FileWriter(Properties.DIR_MODEL + "/" + Properties.FILE_LMJM, true);
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
                term = Utils.filterText(term).toLowerCase();
            }
            if(stemSwitch)
            {
                stemmer.setCurrent(term);
                stemmer.stem();
                term = stemmer.getCurrent();
            }
            if(!tfwqMap.containsKey(term))
            {
                tfwqMap.put(term, 1);
            }
            tfwqMap.put(term, tfwqMap.get(term) + 1);
        }
    }

    /* Execute queries */
    public static void parseAndExecQueries(HashSet<String> stopSet)
        throws ClassNotFoundException, IOException
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
                    term = Utils.filterText(term).trim().toLowerCase();
                }
                if(++wordIdx < 4 ||
                   stopSet.contains(term))
                {
                    continue;
                }
                if(stemSwitch)
                {
                    stemmer.setCurrent(term);
                    stemmer.stem();
                    term = stemmer.getCurrent();
                }
                execQuery(term);
                termSet.add(term);
            }
            sortAndFilterMaps();
            writeQueuesToFS(queryNum);
            resetMaps();
            System.out.println("   [echo] End of processing for query #" + queryCount);
        }
    }

    /* Execute a query */
    public static void execQuery(String term)
        throws IOException
    {
        termFreqMap = SearchClient.queryTerm(term);
        String key = "";
        double sumTfwd = 0;

        for(Map.Entry<String, Entry> termFreqEntry : termFreqMap.entrySet())
        {
            key = termFreqEntry.getKey();
            double okapiTf = Formulae.okapitf(termFreqEntry.getValue().getTf(),
                                              docLenMap.get(key),
                                              avgDocLength);
            sumTfwd += termFreqEntry.getValue().getTf();

            okapiTfMap.put(key, okapiTfMap.get(key) + okapiTf);

            tfIdfMap.put(key,
                         tfIdfMap.get(key) +
                         Formulae.tfidf(okapiTf, termFreqMap.size()));

            okapibm25Map.put(key,
                            okapibm25Map.get(key) +
                            Formulae.okapibm25((double) termFreqMap.size(),
                                               (double) termFreqEntry.getValue().getTf(),
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
                tfwd = termFreqMap.get(key).getTf();
            }
            lmLaplaceMap.put(key, lmLaplaceMap.get(key) + Formulae.plaplace(tfwd, docLenMap.get(key)));
            lmJmMap.put(key, lmJmMap.get(key) + Formulae.pjm(tfwd, docLenMap.get(key), (sumTfwd / allDocLength)));
        }
    }

    /* Main method for unit testing */
    public static void main(String[] args)
    {
        if(args.length < 2)
        {
            Utils.error("A minimum of 2 arguments are required.");
            Utils.echo("-stop=true/false -stem=true/false");
            System.exit(-1);
        }

        stopSwitch = args[0].equalsIgnoreCase("-stop=true");
        stemSwitch = args[1].equalsIgnoreCase("-stem=true");
        Utils.echo("Stop word removal has been set to " + stopSwitch);
        Utils.echo("Stemming has been set to " + stemSwitch + "\n");

        try
        {
            /* Calculate document statistics and execute queries */
            HashSet<String> stopSet = stopSwitch ? Utils.createStopSet() : new HashSet<String>();
            initializeStatMaps();
            parseAndExecQueries(stopSet);
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        catch(ClassNotFoundException cnfe)
        {
            cnfe.printStackTrace();
        }
    }
}
/* End of Query.java */