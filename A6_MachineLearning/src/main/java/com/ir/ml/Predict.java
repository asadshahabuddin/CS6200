package com.ir.ml;

/* Import list */
import java.util.HashMap;
import java.io.FileReader;
import java.io.FileWriter;
import weka.core.Instance;
import weka.core.Instances;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.LinkedHashMap;
import com.ir.global.Properties;
import org.apache.mahout.math.Vector;
import weka.classifiers.functions.LinearRegression;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;

/**
 * Author : Asad Shahabuddin
 * Created: Jul 31, 2015
 */

public class Predict
{
    private HashMap<String, LinkedHashMap<String, Double>> mlMap;

    /**
     * Constructor
     */
    public Predict()
    {
        mlMap = new HashMap<String, LinkedHashMap<String, Double>>();
    }

    /**
     * Write the ranked entries to file system.
     * @throws IOException
     */
    public void write()
        throws IOException
    {
        FileWriter fw = new FileWriter(Properties.FILE_RANK);
        for(String qid : mlMap.keySet())
        {
            /* Create a priority queue for this query ID. */
            Queue mlq = new Queue();
            for(String docid : mlMap.get(qid).keySet())
            {
                mlq.add(new NodeScorePair(docid, mlMap.get(qid).get(docid)));
            }
            mlq.reverse();

            /* Output the queue as a ranked list to the file system. */
            int rank = 0;
            NodeScorePair nsp;
            StringBuilder sb = new StringBuilder();
            while((nsp = mlq.remove()) != null)
            {
                sb.append(qid + " Q0 " + nsp.getNode() + " " +
                          ++rank + " " + nsp.getScore() + " Exp\n");
            }
            fw.write(sb.toString());
        }
        fw.close();
    }

    /**
     * Predict using the online logistic regression model.
     * @param model
     *            The trained model.
     * @throws IOException
     */
    public void onlineLogisticRegression(OnlineLogisticRegression model)
        throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_TRAIN));
        String line;

        while((line = br.readLine()) != null)
        {
            String[] fields = line.split(" ");
            String qid = fields[0].split("-")[0];
            String docid = fields[0].substring(fields[0].indexOf("-") + 1);
            Observation obs = new Observation(fields);
            Vector result = model.classifyFull(obs.getVector());

            if(!mlMap.containsKey(qid))
            {
                mlMap.put(qid, new LinkedHashMap<String, Double>());
            }
            mlMap.get(qid).put(docid, result.get(1));
        }

        /*
        (1) Close the reader object.
        (2) Output ranked list to the file system.
        */
        br.close();
        write();
    }

    /**
     * Predict using the linear regression model.
     * @param model
     *            The trained model.
     * @throws Exception
     */
    public void linearRegression(LinearRegression model)
        throws Exception
    {
        BufferedReader br1 = new BufferedReader(new FileReader(Properties.FILE_WEKA_TRAIN1));
        BufferedReader br2 = new BufferedReader(new FileReader(Properties.FILE_WEKA_TRAIN2));
        Instances instances = new Instances(br2);
        String line;
        int idx = 0;

        while((line = br1.readLine()) != null)
        {
            String[] fields = line.split(" ");
            String qid = fields[0].split("-")[0];
            String docid = fields[0].substring(fields[0].indexOf("-") + 1);
            Instance instance = instances.instance(idx++);
            if(!mlMap.containsKey(qid))
            {
                mlMap.put(qid, new LinkedHashMap<String, Double>());
            }
            mlMap.get(qid).put(docid, model.classifyInstance(instance));
        }

        /*
        (1) Close the reader object.
        (2) Output ranked list to the file system.
        */
        br2.close();
        br1.close();
        write();
    }
}
/* End of Predict.java */