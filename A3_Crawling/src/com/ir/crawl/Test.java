package com.ir.crawl;

import com.ir.global.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 24, 2015
 */

public class Test
{
    /**
     * Main method for unit testing.
     * @param args
     *            Program arguments.
     */
    public static void main(String[] args)
        throws IOException
    {
        Document doc = Jsoup.connect("http://en.wikipedia.org/wiki/Telecommunications_in_the_Philippines").get();
        for(Element e : doc.select("a[href]"))
        {
            Utils.cout(e.attr("href") + "\n");
        }
    }
}
/* End of Test.java */