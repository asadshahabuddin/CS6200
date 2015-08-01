package com.ir.ml;

/* Import list */
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.vectorizer.encoders.ConstantValueEncoder;

/**
 * Author : Asad Shahabuddin
 * Created: Jul 31, 2015
 */

public class Observation
{
    private DenseVector vector = new DenseVector(6);
    private int actual;

    /**
     * Constructor
     * @param values
     *            An entry from the feature matrix.
     */
    public Observation(String[] values)
    {
        ConstantValueEncoder interceptEncoder = new ConstantValueEncoder("intercept");
        interceptEncoder.addToVector("1", vector);
        vector.set(0, Double.valueOf(values[1]));  /* Okapi TF */
        vector.set(1, Double.valueOf(values[2]));  /* TF-IDF */
        vector.set(2, Double.valueOf(values[3]));  /* Okapi BM25 */
        vector.set(3, Double.valueOf(values[4]));  /* Unigram LM (w/ Laplace smoothing) */
	    vector.set(4, Double.valueOf(values[5]));  /* Unigram LM (w/ JM smoothing) */
        this.actual = Integer.valueOf(values[6].split("\\.")[0]);
    }

    /**
     * Get the vector object.
     * @return
     *            The vector object.
     */
    public Vector getVector()
    {
        return vector;
    }

    /**
     * Get the actual value.
     * @return
     *            The actual value.
     */
    public int getActual()
    {
        return actual;
    }
}
/* End of Observation.java */