package com.ir.model;

/* Import list */
import com.ir.global.Properties;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 13, 2015
 */

public class Formulae
{
    /* Okapi TF */
    protected static double okapitf(int tfwd,
                                    double docLength,
                                    double avgDocLength)
    {
        return tfwd / (tfwd + 0.5 + (1.5 * (docLength / avgDocLength)));
    }

    /* TF-IDF */
    protected static double tfidf(double okapitf, int dfw)
    {
        return okapitf * Math.log(Properties.COUNT_DOC / dfw);
    }

    /* Okapi BM25 */
    protected static double okapibm25(double dfw,
                                      double tfwd,
                                      double docLength,
                                      double avgDocLength,
                                      double tfwq)
    {
        return Math.log((Properties.COUNT_DOC + 0.5) / (dfw + 0.5)) *
                ((tfwd * tfwq * (1 + Properties.K1) * (1 + Properties.K2)) /
                 ((tfwd + Properties.K1 * ((1 - Properties.B) + (Properties.B * docLength / avgDocLength))) * (tfwq + Properties.K2)));
    }

    /* Unigram LM with Laplace smoothing */
    public static double plaplace(double tfwd, double docLength)
    {
        return Math.log((tfwd + 1) / (docLength + Properties.V));
    }

    /* Unigram LM with Jelinek-Mercer smoothing */
    public static double pjm(double tfwd, double docLength, double bgModel)
    {
        if(docLength != 0)
        {
            return Math.log((Properties.L * tfwd / docLength) +
                            (Properties.LL * bgModel));
        }
        return Math.log(Properties.LL * bgModel);
    }
}
/* End of Formulae.java */