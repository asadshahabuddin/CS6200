package com.ir.es;

/* Import list */
import java.util.HashSet;

public class Properties
{
    /* Files */
    public static final String FILE_STOPLIST  = "stoplist.txt";
    public static final String FILE_DOCLIST   = "doclist.txt";
    public static final String FILE_QUERY     = "query_desc.51-100.short.txt";
    public static final String FILE_OKAPITF   = "okapitf.txt";
    public static final String FILE_TFIDF     = "tfidf.txt";
    public static final String FILE_OKAPIBM25 = "okapibm25.txt";
    public static final String FILE_LMLAPLACE = "lmlaplace.txt";
    public static final String FILE_LMJM      = "lmjm.txt";
    
    /* Punctuations */
    public static HashSet<Character> PUNCTUATIONS = new HashSet<Character>();
    
    static
    {
        PUNCTUATIONS.add('.');
        PUNCTUATIONS.add(',');
        PUNCTUATIONS.add(';');
        PUNCTUATIONS.add(':');
        PUNCTUATIONS.add('"');
        PUNCTUATIONS.add('(');
        PUNCTUATIONS.add(')');
        PUNCTUATIONS.add('!');
    }
    
    /* Collection statistics */
    public static final long COUNT_DOC = 84678;
    
    /* Constants */
    public static final double K1 = 1.2;
    public static final double K2 = 1000;  /* 0 - 1000 */
    public static final double B  = 0.75;
    public static final double V  = 178050;
    public static final double L  = 0.22;
    public static final double LL = 1 - L;
}
/* End of Properties.java */