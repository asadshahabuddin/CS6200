package com.ir.es;

public class Model
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
        return Math.log((Properties.L * tfwd / docLength) +
                        (Properties.LL * bgModel));
    }
    
    /* Main method for unit testing */
    public static void main(String[] args)
    {
        System.out.println(okapibm25(1, 2, 5, 2.5, 3));
    }
}
/* End of Model.java */