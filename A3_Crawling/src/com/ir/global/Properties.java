package com.ir.global;

import java.util.ArrayList;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 23, 2015
 */

public class Properties
{
    /* Constants */
    public static final String AGENT_MOZILLA = "Mozilla 5.0";

    /* Regular expressions */
    public static final String REGEX_URL     = "href=\"(?=(?:([^\"]+)))";
    public static final String REGEX_DOMAIN  = "http\\w{0,1}://[^/]*/";
    public static final String REGEX_SECTION = "#\\S*";
    public static final String REGEX_MINURL  = "http\\w{0,1}://(w{3}\\.|)";

    /* Directories and files */
    public static final String DIR_CRAWL   = "E:/Home/Repository/Java/IdeaProjects/A3_Crawling/crawl";
    public static final String FILE_ROBOTS = "robots.txt";
    public static final String FILE_GRAPH  = "graph.txt";

    /* Restricted domains */
    public static ArrayList<String> restrictedDomains = new ArrayList<>();

    static
    {
        restrictedDomains.add("facebook.com");
        restrictedDomains.add("twitter.com");
        restrictedDomains.add("linkedin.com");
        restrictedDomains.add("youtube.com");
    }
}
/* End of Properties.java */