package com.ir.token;

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

    /* Regular expression patterns */
    public static final String REGEX_TOKEN = "\\w+(\\.?\\w+)*";

    /* Files */
    public static final String FILE_STOPLIST = "stoplist.txt";
}
/* End of Properties.java */