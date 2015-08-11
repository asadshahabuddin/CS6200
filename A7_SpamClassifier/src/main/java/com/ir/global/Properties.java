package com.ir.global;

/* Import list */
import java.util.HashSet;

/**
 * Author : Asad Shahabuddin
 * Created: Aug 9, 2015
 */

public class Properties
{
    /* Directories and files */
    public static final String DIR_INPUT     = "E:/Home/ir_data/sc_input";
    public static final String FILE_STOPLIST = "input/stoplist.txt";
    public static final String FILE_DOCLIST  = "input/doclist.txt";

    /* Constants */
    public static final String ES_CLUSTER = "leoscluster";

    /* Objects */
    public static HashSet<Character> PUNCTUATIONS = new HashSet<Character>();

    static
    {
        PUNCTUATIONS.add('.');
        PUNCTUATIONS.add(',');
        PUNCTUATIONS.add(':');
        PUNCTUATIONS.add(';');
        PUNCTUATIONS.add('"');
        PUNCTUATIONS.add('(');
        PUNCTUATIONS.add(')');
        PUNCTUATIONS.add('!');
        PUNCTUATIONS.add('_');
    }
}
/* End of Properties.java */