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
    public static final String DIR_INPUT         = "E:/Home/ir_data/sc_input";
    public static final String FILE_STOPLIST     = "input/stoplist.txt";
    public static final String FILE_SPAMLIST     = "input/spamlist.txt";
    public static final String FILE_TRAIN_MAP    = "output/train.map";
    public static final String FILE_TEST_MAP     = "output/test.map";
    public static final String FILE_TRAIN_MATRIX = "output/train.txt";
    public static final String FILE_TEST_MATRIX  = "output/test.txt";

    /* Numerical constants */
    public static final int KEY_TRAIN = 0;
    public static final int KEY_TEST  = 1;

    /* String constants */
    public static final String CLUSTER_NAME = "leoscluster";
    public static final String INDEX_NAME   = "a7_dataset";
    public static final String INDEX_TYPE   = "document";

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