package com.ir.global;

/**
 * Author : Asad Shahabuddin
 * Created: Jul 5, 2015
 */

public class Properties
{
    /* ElasticSearch properties. */
    public static final String CLUSTER_NAME = "leoscluster";
    public static final String INDEX_NAME   = "ras_dataset";
    public static final String INDEX_TYPE   = "document";

    /* Directories and files. */
    public static final String DIR_GRAPH1     = "C:/Users/Asad Shahabuddin/Google Drive/Information Retrieval";
    public static final String DIR_GRAPH2     = "E:/Home/Repository/Java/IdeaProjects/A4_Pagerank";
    public static final String FILE_GRAPH     = "graph.txt";
    public static final String FILE_DOCLIST   = "doclist.txt";
    public static final String FILE_DOCLEN    = "doclen.txt";
    public static final String FILE_BM25      = "bm25.txt";
    public static final String FILE_BASESET   = "baseset.txt";
    public static final String FILE_IDX_OBJ   = "index.map";
    public static final String FILE_AMIN_OBJ  = "adjacency-matrix-in.map";
    public static final String FILE_AMOUT_OBJ = "adjacency-matrix-out.map";

    /* Numerical constants. */
    public static final int COUNT_DOC = 45608;
    public static final double LAMBDA = 0.85;
    public static final double K1     = 1.2;
    public static final double K2     = 1000;  /* 0 - 1000 */
    public static final double B      = 0.75;

    /* String constants. */
    public static final String QUERY_TOPICAL = "APPLE TECHNOLOGY";
}
/* End of Properties.java */