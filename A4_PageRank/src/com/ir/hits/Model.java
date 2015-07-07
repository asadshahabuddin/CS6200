package com.ir.hits;

/* Import list */
import com.ir.global.Properties;

/**
 * Author : Asad Shahabuddin
 * Created: Jul 7, 2015
 */

public class Model
{
    /**
     * Calculate the Okapi BM25 value.
     * @param dfw
     *            The number of documents which contain term w.
     * @param tfwd
     *            The term frequency of term w in document d.
     * @param dlen
     *            The length of document d.
     * @param avgDocLength
     *            The average document length for the entire corpus.
     * @param tfwq
     *            The term frequency of term w in query q.
     * @return
     *            The Okapi BM25 value.
     */
    protected static double okapibm25(double dfw,
                                      double tfwd,
                                      double dlen,
                                      double avgDocLength,
                                      double tfwq)
    {
        return Math.log((Properties.COUNT_DOC + 0.5) / (dfw + 0.5)) *
               ((tfwd * tfwq * (1 + Properties.K1) * (1 + Properties.K2)) /
                ((tfwd + Properties.K1 * ((1 - Properties.B) + (Properties.B * dlen / avgDocLength))) *
                 (tfwq + Properties.K2)));
    }
}
/* End of Model.java */