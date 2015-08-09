package com.ir.ml;

/* Import list */
import java.io.FileReader;
import java.io.FileWriter;
import weka.core.Instances;
import java.io.IOException;
import java.io.BufferedReader;
import com.ir.global.Properties;
import org.apache.mahout.classifier.sgd.L1;
import weka.classifiers.functions.LinearRegression;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;

/**
 * Author : Asad Shahabuddin
 * Created: Jul 31, 2015
 */

public class Train
{
    private OnlineLogisticRegression olr;
    private LinearRegression lr;

    /**
     * Constructor
     */
    public Train()
    {
        olr = new OnlineLogisticRegression(2, 6, new L1());
        lr  = new LinearRegression();
    }

    /**
     * Get the Online Logistic Regression model.
     * @return
     *            The model.
     */
    public OnlineLogisticRegression getOlrModel()
    {
        return olr;
    }

    /**
     * Get the Linear Regression model.
     * @return
     *            The model.
     */
    public LinearRegression getLrModel()
    {
        return lr;
    }

    /**
     * Create an input file based on WEKA format.
     * @throws IOException
     */
    public void createWekaInput()
        throws IOException
    {
        BufferedReader br1 = new BufferedReader(new FileReader(Properties.FILE_TRAIN));
        BufferedReader br2  = new BufferedReader(new FileReader(Properties.FILE_TEST));
        FileWriter fw1 = new FileWriter(Properties.FILE_WEKA_TRAIN1);
        FileWriter fw2 = new FileWriter(Properties.FILE_WEKA_TRAIN2);
        FileWriter fw3 = new FileWriter(Properties.FILE_WEKA_TEST1);
        FileWriter fw4 = new FileWriter(Properties.FILE_WEKA_TEST2);
        String line;

        /* Append data to train files. */
        fw2.write("@RELATION train_set\n" +
                  "@ATTRIBUTE okapitf NUMERIC\n" +
                  "@ATTRIBUTE tfidf NUMERIC\n" +
                  "@ATTRIBUTE okapibm25 NUMERIC\n" +
                  "@ATTRIBUTE lmlaplace NUMERIC\n" +
                  "@ATTRIBUTE lmjm NUMERIC\n" +
                  "@ATTRIBUTE label NUMERIC (0, 1)\n\n" +
                  "@DATA\n");
        while((line = br1.readLine()) != null)
        {
            fw1.write(line.split(" ")[0] + "\n");
            fw2.write(line.substring(line.indexOf(" ") + 1) + "\n");
        }

        /* Append data to test files. */
        fw4.write("@RELATION train_set\n" +
                  "@ATTRIBUTE okapitf NUMERIC\n" +
                  "@ATTRIBUTE tfidf NUMERIC\n" +
                  "@ATTRIBUTE okapibm25 NUMERIC\n" +
                  "@ATTRIBUTE lmlaplace NUMERIC\n" +
                  "@ATTRIBUTE lmjm NUMERIC\n" +
                  "@ATTRIBUTE label NUMERIC (0, 1)\n\n" +
                  "@DATA\n");
        while((line = br2.readLine()) != null)
        {
            fw3.write(line.split(" ")[0] + "\n");
            fw4.write(line.substring(line.indexOf(" ") + 1) + "\n");
        }

        /* Close all the file reader and writer objects. */
        fw4.close();
        fw3.close();
        fw2.close();
        fw1.close();
        br2.close();
        br1.close();
    }

    /**
     * Train the online logistic regression model.
     * @param file
     *            The training file.
     * @throws IOException
     */
    public void onlineLogisticRegression(String file)
        throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while((line = br.readLine()) != null)
        {
            Observation obs = new Observation(line.split(" "));
            olr.train(obs.getActual(), obs.getVector());
        }
        br.close();
    }

    /**
     * Train the linear regression model.
     * @param file
     *            The training file.
     * @throws IOException
     */
    public void linearRegression(String file)
        throws Exception
    {
        BufferedReader br = new BufferedReader(new FileReader(file));
        Instances instances = new Instances(br);
        instances.setClassIndex(instances.numAttributes() - 1);
        lr.buildClassifier(instances);
        br.close();
    }

    /**
     * Main method for unit testing.
     * @param args
     *            Program arguments.
     */
    public static void main(String[] args)
        throws IOException
    {
        Train t = new Train();
        Predict p = new Predict();
        try
        {
            t.linearRegression(Properties.FILE_WEKA_TRAIN2);
            p.linearRegression(t.getLrModel());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
/* End of Train.java */