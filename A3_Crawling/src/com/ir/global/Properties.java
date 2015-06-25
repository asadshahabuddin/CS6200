package com.ir.global;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 23, 2015
 */

public class Properties
{
    /* Constants */
    public static final int FLAG_REMOVED = -1;

    /* Regular expressions */
    public static final String REGEX_URL     = "href=\"(?=(?:([^\"]+)))";
    public static final String REGEX_DOMAIN  = "http\\w{0,1}://[^/]*/";
    public static final String REGEX_SECTION = "#\\S*";

    /* Directories and files */
    public static final String DIR_CRAWL   = "E:/Home/Repository/Java/IdeaProjects/A3_Crawling/crawl_data";
    public static final String FILE_ROBOTS = "robots.txt";
}
/* End of Properties.java */