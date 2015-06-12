package com.ir.global;

/* Import list */
import java.util.HashSet;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 5, 2015
 */

public class Properties
{
    /* Constants */
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

    /* Regular expression patterns */
    public static final String REGEX_TOKEN = "\\w+(\\.?\\w+)*";

    /* Keys */
    public static final int TYPE_DOC  = 0;
    public static final int TYPE_TERM = 1;

    /* Directories and files */
    public static final String FILE_STOPLIST    = "stoplist.txt";
    public static final String FILE_DOC_IDX     = "doc.idx";
    public static final String FILE_DOC_TXT     = "docs.txt";
    public static final String FILE_TERM_IDX    = "term.idx";
    public static final String FILE_TERM_TXT    = "terms.txt";
    public static final String FILE_STATS       = "stats.txt";
    public static final String FILE_TUPLES_OBJ  = "tuple.idx";
    public static final String FILE_TUPLES_TEXT = "tuples.txt";
    public static final String DIR_IDX          = "E:/Home/Repository/Java/IdeaProjects/A2_Indexing/index";
    public static final String DIR_CATALOG      = "E:/Home/Repository/Java/IdeaProjects/A2_Indexing/catalog";

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