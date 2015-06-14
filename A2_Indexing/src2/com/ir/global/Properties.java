package com.ir.global;

/* Import list */
import java.util.HashSet;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 5, 2015
 */

public class Properties
{
    /* String constants */
    public static final String XELEM_DOC_BEGIN   = "<DOC>";
    public static final String XELEM_DOC_END     = "</DOC>";
    public static final String XELEM_DOCNO_BEGIN = "<DOCNO>";
    public static final String XELEM_DOCNO_END   = "</DOCNO>";
    public static final String XELEM_TEXT_BEGIN  = "<TEXT>";
    public static final String XELEM_TEXT_END    = "</TEXT>";
    public static final String KEY_DOC_COUNT     = "DOC_COUNT";
    public static final String KEY_VOCAB_SIZE    = "VOCAB_SIZE";
    public static final String KEY_ALL_DOC_LEN   = "ALL_DOC_LEN";
    public static final String KEY_AVG_DOC_LEN   = "AVG_DOC_LEN";

    /* Mathematical constants */
    public static final long COUNT_DOC = 84678;
    public static final double K1      = 1.2;
    public static final double K2      = 1000;
    public static final double B       = 0.75;
    public static final double V       = 178050;
    public static final double L       = 0.22;
    public static final double LL      = 1 - L;
    public static final double C       = 1500;

    /* Keys */
    public static final int TYPE_DOC    = 0;
    public static final int TYPE_TERM   = 1;
    public static final int TYPE_DOCLEN = 2;

    /* Directories and files */
    public static final String FILE_STOPLIST    = "stoplist.txt";
    public static final String FILE_DOCLIST     = "doclist.txt";
    public static final String FILE_QUERY       = "query_desc.51-100.short.txt";
    public static final String FILE_DOC_IDX     = "doc.idx";
    public static final String FILE_DOC_TXT     = "docs.txt";
    public static final String FILE_TERM_IDX    = "term.idx";
    public static final String FILE_TERM_TXT    = "terms.txt";
    public static final String FILE_DOCLEN_OBJ  = "doclen.map";
    public static final String FILE_DOCLEN_TXT  = "doclen.txt";
    public static final String FILE_STATS       = "stats.txt";
    public static final String FILE_TUPLES_OBJ  = "tuple.idx";
    public static final String FILE_TUPLES_TEXT = "tuples.txt";
    public static final String FILE_CATALOG_OBJ = "sets.catalog";
    public static final String DIR_OBJ          = "E:/Home/Repository/Java/IdeaProjects/A2_Indexing/obj";
    public static final String DIR_IDX          = "E:/Home/Repository/Java/IdeaProjects/A2_Indexing/index";
    public static final String DIR_CATALOG      = "E:/Home/Repository/Java/IdeaProjects/A2_Indexing/catalog";
    public static final String DIR_MODEL        = "E:/Home/Repository/Java/IdeaProjects/A2_Indexing/model";
    public static final String FILE_OKAPITF     = "okapitf.txt";
    public static final String FILE_TFIDF       = "tfidf.txt";
    public static final String FILE_OKAPIBM25   = "okapibm25.txt";
    public static final String FILE_LMLAPLACE   = "lmlaplace.txt";
    public static final String FILE_LMJM        = "lmjm.txt";
    public static final String FILE_PROX_SEARCH = "proximity-search.txt";

    /* Regular expression patterns */
    public static final String REGEX_TOKEN = "\\w+(\\.?\\w+)*";

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
        PUNCTUATIONS.add('_');
    }
}
/* End of Properties.java */