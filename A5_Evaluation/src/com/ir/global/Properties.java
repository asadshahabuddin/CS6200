package com.ir.global;

/**
 * Author : Asad Shahabuddin
 * Created: Jul 19, 2015
 */

public class Properties
{
    /* Numerical constants. */
    public static final int KEY_TRECEVAL = 0;
    public static final int KEY_IREVAL   = 1;
    public static final int COUNT_DOC    = 45608;
    public static final double K1        = 1.2;
    public static final double K2        = 1000;  /* 0 - 1000 */
    public static final double B         = 0.75;

    /* Directories and files. */
    public static final String FILE_DOCLEN   = "doclen.txt";

    /* ElasticSearch properties. */
    public static final String CLUSTER_NAME = "leoscluster";
    public static final String INDEX_NAME   = "ras_dataset";
    public static final String INDEX_TYPE   = "document";
}
/* End of Properties.java */