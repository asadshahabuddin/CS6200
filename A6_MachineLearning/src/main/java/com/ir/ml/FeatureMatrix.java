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
     * Create a set of featured document IDs.
     * @return
     *            The set of relevant document IDs.
     * @throws IOException
     */
    @SuppressWarnings("unused")
    public HashSet<String> createDocList()
        throws IOException
    {
        File file = new File(Properties.FILE_QRELS);
        if(!file.exists())
        {
            BufferedReader br = new BufferedReader(new FileReader(file));
            FileWriter fw = new FileWriter(Properties.FILE_DOCLIST);
            StringBuilder sb = new StringBuilder();
            String line;

            while((line = br.readLine()) != null)
            {
                sb.append(line.split(" ")[2] + "\n");
            }

            /* Output the document list to file system and close the file handles. */
            fw.write(sb.toString());
            fw.close();
            br.close();
        }
        return getDocSet();
    }

    /**
     * Retrieve the set of relevant document IDs.
     * @return
     *            The set of relevant document IDs.
     * @throws IOException
     */
    public HashSet<String> getDocSet()
        throws IOException
    {
        HashSet<String> docs = new HashSet<String>();
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_DOCLIST));
        String line;
        while((line = br.readLine()) != null)
        {
            docs.add(line);
        }
        return docs;
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
     * @throws IOException
     */
    public void populateOkapiTfValues()
        throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_OKAPITF));
        String line;

        while((line = br.readLine()) != null)
        {
            String[] fields = line.split(" ");
            if(featureMatrix.containsKey(fields[0]))
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
     * @throws IOException
     */
    public void populateTfIdfValues()
        throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_TFIDF));
        String line;

        while((line = br.readLine()) != null)
        {
            String[] fields = line.split(" ");
            if(featureMatrix.containsKey(fields[0]))
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
     * @throws IOException
     */
    public void populateOkapiBm25Values()
        throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_OKAPIBM25));
        String line;

        while((line = br.readLine()) != null)
        {
            String[] fields = line.split(" ");
            if(featureMatrix.containsKey(fields[0]))
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
     * @throws IOException
     */
    public void populateLmLaplaceValues()
        throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_LMLAPLACE));
        String line;

        while((line = br.readLine()) != null)
        {
            String[] fields = line.split(" ");
            if(featureMatrix.containsKey(fields[0]))
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
     * @throws IOException
     */
    public void populateLmJmValues()
        throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_LMJM));
        String line;

        while((line = br.readLine()) != null)
        {
            String[] fields = line.split(" ");
            if(featureMatrix.containsKey(fields[0]))
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
     * @throws IOException
     */
    public void populateLabels()
        throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_QRELS));
        String line;

        while((line = br.readLine()) != null)
        {
            String[] fields = line.split(" ");
            if(featureMatrix.containsKey(fields[0]))
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
        FileWriter fw = new FileWriter(Properties.FILE_FEATUREMATRIX);
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
            fw.write(sb.toString());
        }
        fw.close();
    }

    /**
     * Create feature matrix and output it to the file system.
     * @param queries
     *            The list of queries.
     * @throws IOException
     */
    public void createFeatureMatrix(ArrayList<String> queries)
        throws IOException
    {
        /* Populate keys for the feature matrix. */
        int count = 0;
        for(String query : queries)
        {
            featureMatrix.put(query, new HashMap<String, double[]>());
            if(++count == 20)
            {
                break;
            }
        }

        /* Populate features. */
        populateOkapiTfValues();
        populateTfIdfValues();
        populateOkapiBm25Values();
        populateLmLaplaceValues();
        populateLmJmValues();
        populateLabels();

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
            ArrayList<String> queries = fm.getQueries();
            fm.createFeatureMatrix(queries);
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}
/* End of FeatureMatrix.java */