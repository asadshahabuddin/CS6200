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
    public static final String DIR_INPUT           = "E:/Home/ir_data/sc_input";
    public static final String FILE_SPAMLIST       = "input/spamlist.txt";
    public static final String FILE_TRAIN_MAP      = "output/train.map";
    public static final String FILE_TEST_MAP       = "output/test.map";
    public static final String FILE_MS_TRAIN       = "output/ms-train.txt";
    public static final String FILE_MS_TRAIN_LABEL = "output/ms-train-labels.txt";
    public static final String FILE_MS_TRAIN_DATA  = "output/ms-train-data.txt";
    public static final String FILE_MS_TEST        = "output/ms-test.txt";
    public static final String FILE_MS_TEST_LABEL  = "output/ms-test-labels.txt";
    public static final String FILE_MS_TEST_DATA   = "output/ms-test-data.txt";
    public static final String FILE_UNIGRAM_MAP    = "output/unigram.map";
    public static final String FILE_AU_TRAIN       = "output/au-train.txt";
    public static final String FILE_AU_TRAIN_LABEL = "output/au-train-labels.txt";
    public static final String FILE_AU_TRAIN_DATA  = "output/au-train-data.txt";
    public static final String FILE_AU_TEST        = "output/au-test.txt";
    public static final String FILE_AU_TEST_LABEL  = "output/au-test-labels.txt";
    public static final String FILE_AU_TEST_DATA   = "output/au-test-data.txt";

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