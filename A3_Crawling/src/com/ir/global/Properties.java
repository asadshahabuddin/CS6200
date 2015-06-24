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
    public static final String REGEX_DOMAIN    = "([^/]*/){3}";
    public static final String REGEX_CANONICAL = "href=\"(?=(?:([^\"]+)))";
    public static final String REGEX_SECTION   = "#\\S*";

    /* Directories and files */
    public static final String FILE_ROBOTS = "robots.txt";
}
/* End of Properties.java */