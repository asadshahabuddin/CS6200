package com.ir.ml;

/* Import list */
import java.io.File;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.io.BufferedReader;
import com.ir.global.Properties;

/**
 * Author : Asad Shahabuddin
 * Created: Jul 31, 2015
 */

public class FeatureMatrix
{
    private HashMap<String, HashMap<String, double[]>> featureMatrix;

    /**
     * Constructor
     */
    public FeatureMatrix()
    {
        featureMatrix = new HashMap<String, HashMap<String, double[]>>();
    }

    /**
     * Create a map of query and document IDs.
     * @return
     *            The map of query and document IDs.
     * @throws IOException
     */
    public HashMap<String, HashSet<String>> createQueryDocList()
                                                                                                                                                            throws IOException
    {
        File file = new File(Properties.FILE_DOCLIST);
        if(!file.exists())
        {
            BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_QRELS));
            FileWriter fw = new FileWriter(file);
            StringBuilder sb = new StringBuilder();
            String line;

            while((line = br.readLine()) != null)
            {
                sb.append(line.split(" ")[0] + " " + line.split(" ")[2] + "\n");
            }

            /* Output the document list to file system and close the file handles. */
            fw.write(sb.toString());
            fw.close();
            br.close();
        }
        return getQueryDocMap();
    }

    /**
     * Retrieve the map of query and document IDs.
     * @return
     *            The map of query and document IDs.
     * @throws IOException
     */
    public HashMap<String, HashSet<String>> getQueryDocMap()
        throws IOException
    {
        HashMap<String, HashSet<String>> map = new HashMap<String, HashSet<String>>();
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_DOCLIST));
        String line;
        while((line = br.readLine()) != null)
        {
            String[] fields = line.split(" ");
            if(!map.containsKey(fields[0]))
            {
                map.put(fields[0], new HashSet<String>());
            }
            map.get(fields[0]).add(fields[1]);
        }
        return map;
    }

    /**
     * Retrieve the list of queries.
     * @return
     *            The list of queries.
     * @throws IOException
     */
    public ArrayList<String> getQueries()
        throws IOException
    {
        ArrayList<String> queries = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_QUERY));
        String line;
        while((line = br.readLine()) != null)
        {
            if(!line.equals(""))
            {
                queries.add(line.split("\\.")[0]);
            }
        }
        return queries;
    }

    /**
     * Populate Okapi TF values in the feature matrix.
     * @param qdMap
     *            The map of query and document IDs.
     * @throws IOException
     */
    public void populateOkapiTfValues(HashMap<String, HashSet<String>> qdMap)
        throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_OKAPITF));
        String line;

        while((line = br.readLine()) != null)
        {
            String[] fields = line.split(" ");
            if(qdMap.get(fields[0]).contains(fields[2]))
            {
                if(!featureMatrix.get(fields[0]).containsKey(fields[2]))
                {
                    featureMatrix.get(fields[0]).put(fields[2], new double[6]);
                }
                featureMatrix.get(fields[0]).get(fields[2])[0] = Double.valueOf(fields[4]);
            }
        }
    }

    /**
     * Populate TF-IDF values in the feature matrix.
     * @param qdMap
     *            The map of query and document IDs.
     * @throws IOException
     */
    public void populateTfIdfValues(HashMap<String, HashSet<String>> qdMap)
        throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_TFIDF));
        String line;

        while((line = br.readLine()) != null)
        {
            String[] fields = line.split(" ");
            if(qdMap.get(fields[0]).contains(fields[2]))
            {
                if(!featureMatrix.get(fields[0]).containsKey(fields[2]))
                {
                    featureMatrix.get(fields[0]).put(fields[2], new double[6]);
                }
                featureMatrix.get(fields[0]).get(fields[2])[1] = Double.valueOf(fields[4]);
            }
        }
    }

    /**
     * Populate Okapi BM25 values in the feature matrix.
     * @param qdMap
     *            The map of query and document IDs.
     * @throws IOException
     */
    public void populateOkapiBm25Values(HashMap<String, HashSet<String>> qdMap)
        throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_OKAPIBM25));
        String line;

        while((line = br.readLine()) != null)
        {
            String[] fields = line.split(" ");
            if(qdMap.get(fields[0]).contains(fields[2]))
            {
                if(!featureMatrix.get(fields[0]).containsKey(fields[2]))
                {
                    featureMatrix.get(fields[0]).put(fields[2], new double[6]);
                }
                featureMatrix.get(fields[0]).get(fields[2])[2] = Double.valueOf(fields[4]);
            }
        }
    }

    /**
     * Populate Unigram LM (w/ Laplace smoothing) values in the feature matrix.
     * @param qdMap
     *            The map of query and document IDs.
     * @throws IOException
     */
    public void populateLmLaplaceValues(HashMap<String, HashSet<String>> qdMap)
        throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_LMLAPLACE));
        String line;

        while((line = br.readLine()) != null)
        {
            String[] fields = line.split(" ");
            if(qdMap.get(fields[0]).contains(fields[2]))
            {
                if(!featureMatrix.get(fields[0]).containsKey(fields[2]))
                {
                    featureMatrix.get(fields[0]).put(fields[2], new double[6]);
                }
                featureMatrix.get(fields[0]).get(fields[2])[3] = Double.valueOf(fields[4]);
            }
        }
    }

    /**
     * Populate Unigram LM (w/ JM smoothing) values in the feature matrix.
     * @param qdMap
     *            The map of query and document IDs.
     * @throws IOException
     */
    public void populateLmJmValues(HashMap<String, HashSet<String>> qdMap)
        throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_LMJM));
        String line;

        while((line = br.readLine()) != null)
        {
            String[] fields = line.split(" ");
            if(qdMap.get(fields[0]).contains(fields[2]))
            {
                if(!featureMatrix.get(fields[0]).containsKey(fields[2]))
                {
                    featureMatrix.get(fields[0]).put(fields[2], new double[6]);
                }
                featureMatrix.get(fields[0]).get(fields[2])[4] = Double.valueOf(fields[4]);
            }
        }
    }

    /**
     * Populate QREL relevance values in the feature matrix.
     * @param qdMap
     *            The map of query and document IDs.
     * @throws IOException
     */
    public void populateLabels(HashMap<String, HashSet<String>> qdMap)
        throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_QRELS));
        String line;

        while((line = br.readLine()) != null)
        {
            String[] fields = line.split(" ");
            if(featureMatrix.containsKey(fields[0]) &&
               qdMap.get(fields[0]).contains(fields[2]))
            {
                if(!featureMatrix.get(fields[0]).containsKey(fields[2]))
                {
                    featureMatrix.get(fields[0]).put(fields[2], new double[6]);
                }
                featureMatrix.get(fields[0]).get(fields[2])[5] = Double.valueOf(fields[3]);
            }
        }
    }

    /**
     * Output feature matrix to the file system.
     * @throws IOException
     */
    public void writeFeatureMatrix()
        throws IOException
    {
        FileWriter trainFw  = new FileWriter(Properties.FILE_TRAIN);
        FileWriter testFw   = new FileWriter(Properties.FILE_TEST);
        FileWriter resultFw = new FileWriter(Properties.FILE_RESULT);

        for(String qid : featureMatrix.keySet())
        {
            StringBuilder sb = new StringBuilder();
            for(Map.Entry<String, double[]> e : featureMatrix.get(qid).entrySet())
            {
                sb.append(qid + "-" + e.getKey() + " " +
                          e.getValue()[0] + " " +
                          e.getValue()[1] + " " +
                          e.getValue()[2] + " " +
                          e.getValue()[3] + " " +
                          e.getValue()[4] + " " +
                          e.getValue()[5] + "\n");
            }
            if(Properties.trainingQueries.contains(qid))
            {
                trainFw.write(sb.toString());
            }
            else
            {
                testFw.write(sb.toString().replace("1.0\n", "0.0\n"));
                resultFw.write(sb.toString());
            }
        }

        /* Close the file write handles. */
        trainFw.close();
        testFw.close();
        resultFw.close();
    }

    /**
     * Create feature matrix and output it to the file system.
     * @param queries
     *            The list of queries.
     * @param qdMap
     *            The map of query and document IDs.
     * @throws IOException
     */
    public void createFeatureMatrix(ArrayList<String> queries,
                                    HashMap<String, HashSet<String>> qdMap)
        throws IOException
    {
        /* Populate keys for the feature matrix. */
        for(String query : queries)
        {
            featureMatrix.put(query, new HashMap<String, double[]>());
        }

        /* Populate features. */
        populateOkapiTfValues(qdMap);
        populateTfIdfValues(qdMap);
        populateOkapiBm25Values(qdMap);
        populateLmLaplaceValues(qdMap);
        populateLmJmValues(qdMap);
        populateLabels(qdMap);

        /* Output feature matrix to the file system. */
        writeFeatureMatrix();
    }

    /**
     * Main method for unit testing.
     * @param args
     *            Program arguments.
     */
    public static void main(String[] args)
    {
        FeatureMatrix fm = new FeatureMatrix();
        try
        {
            HashMap<String, HashSet<String>> queryDocMap = fm.createQueryDocList();
            ArrayList<String> queries = fm.getQueries();
            fm.createFeatureMatrix(queries, queryDocMap);
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}
/* End of FeatureMatrix.java */