package com.ir.global;

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

    /* Files */
    public static final String FILE_STOPLIST    = "stoplist.txt";
    public static final String FILE_DOC_IDX     = "doc.idx";
    public static final String FILE_TERM_IDX    = "term.idx";
    public static final String FILE_STATS       = "stats.txt";
    public static final String FILE_TUPLES_OBJ  = "tuple.idx";
    public static final String FILE_TUPLES_TEXT = "tuples.txt";
    public static final String FILE_INDEX       = "index.txt";
}
/* End of Properties.java */