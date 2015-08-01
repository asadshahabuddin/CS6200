package com.ir.ml;

/* Import list */
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import com.ir.global.Properties;
import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;

/**
 * Author : Asad Shahabuddin
 * Created: Jul 31, 2015
 */

public class Train
{
    private OnlineLogisticRegression olr;

    /**
     * Constructor
     */
    public Train()
    {
        olr = new OnlineLogisticRegression(2, 6, new L1());
    }

    /**
     * Train a model.
     * @param file
     *            The feature matrix file.
     * @throws IOException
     */
    public void train(String file)
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
     * Main method for unit testing.
     * @param args
     *            Program arguments.
     */
    public static void main(String[] args)
    {
        Train t = new Train();
        try
        {
            t.train(Properties.FILE_FEATUREMATRIX);
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}
/* End of Train.java */