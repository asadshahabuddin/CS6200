package com.ir.global;

/* Import list */
import java.util.HashSet;

/**
 * Author : Asad Shahabuddin
 * Created: Jul 31, 2015
 */

public class Properties
{
    /* Directories and files */
    public static final String FILE_QUERY       = "input/query.txt";
    public static final String FILE_QRELS       = "input/qrels.txt";
    public static final String FILE_OKAPITF     = "input/okapitf.txt";
    public static final String FILE_TFIDF       = "input/tfidf.txt";
    public static final String FILE_OKAPIBM25   = "input/okapibm25.txt";
    public static final String FILE_LMLAPLACE   = "input/lmlaplace.txt";
    public static final String FILE_LMJM        = "input/lmjm.txt";
    public static final String FILE_DOCLIST     = "output/doclist.txt";
    public static final String FILE_TRAIN       = "output/train.txt";
    public static final String FILE_TEST        = "output/test.txt";
    public static final String FILE_RESULT      = "output/result.txt";
    public static final String FILE_WEKA_TRAIN1 = "output/weka-train1.txt";
    public static final String FILE_WEKA_TRAIN2 = "output/weka-train2.txt";
    public static final String FILE_WEKA_TEST1  = "output/weka-test1.txt";
    public static final String FILE_WEKA_TEST2  = "output/weka-test2.txt";
    public static final String FILE_RANK        = "output/rank.txt";

    /* Objects */
    public static HashSet<String> trainingQueries = new HashSet<String>();
    public static HashSet<String> testingQueries  = new HashSet<String>();

    static
    {
        /* Populate the training queries. */
        trainingQueries.add("85");
        trainingQueries.add("59");
        trainingQueries.add("56");
        trainingQueries.add("71");
        trainingQueries.add("64");
        trainingQueries.add("62");
        trainingQueries.add("93");
        trainingQueries.add("58");
        trainingQueries.add("77");
        trainingQueries.add("54");
        trainingQueries.add("87");
        trainingQueries.add("89");
        trainingQueries.add("61");
        trainingQueries.add("68");
        trainingQueries.add("57");
        trainingQueries.add("98");
        trainingQueries.add("60");
        trainingQueries.add("80");
        trainingQueries.add("63");
        trainingQueries.add("91");

        /* Populate the testing queries. */
        testingQueries.add("94");
        testingQueries.add("95");
        testingQueries.add("97");
        testingQueries.add("99");
        testingQueries.add("100");
    }
}
/* End of Properties.java */