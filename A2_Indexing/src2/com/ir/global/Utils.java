package com.ir.global;

/* Import list */
import java.util.HashSet;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 10, 2015
 */

public class Utils
{
    public static void cout(Object o)
    {
        System.out.print(o);
    }

    public static void echo(Object o)
    {
        System.out.println("   [echo] " + o);
    }

    public static void error(Object o)
    {
        System.out.println("  [error] " + o);
    }

    public static void warning(Object o)
    {
        System.out.println("[warning] " + o);
    }

    /**
     * (1) Output time elapsed since last checkpoint.
     * (2) Return start time for this checkpoint.
     * @param startTime
     *            Start time for the current phase.
     * @param message
     *            User-friendly message.
     * @return
     *            Start time for the next phase.
     */
    public static long elapsedTime(long startTime, String message)
    {
        if(message != null)
        {
            System.out.println(message);
        }
        long elapsedTime = (System.nanoTime() - startTime) / 1000000000;
        System.out.println("Elapsed time: " + elapsedTime + " second(s)");
        return System.nanoTime();
    }

    /**
     * Filter punctuation and unnecessary character
     * @param s
     *           The word to be filtered.
     * @return
     *           The filtered word.
     */
    public static String filterText(String s)
    {
        if(s.equalsIgnoreCase("u.s"))
        {
            return "U.S.";
        }
        while(s.length() > 0 &&
              Properties.PUNCTUATIONS.contains(s.charAt(s.length() - 1)))
        {
            s = s.substring(0, s.length() - 1);
        }
        while(s.length() > 0 &&
              Properties.PUNCTUATIONS.contains(s.charAt(0)))
        {
            s = s.substring(1, s.length());
        }
        return s;
    }

    /**
     * Create a set of all stop words.
     * @return
     *            A set of all stop words.
     * @throws IOException
     */
    public static HashSet<String> createStopSet()
            throws IOException
    {
        HashSet<String> stopSet = new HashSet<String>();
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_STOPLIST));
        String line = "";

        while((line = br.readLine()) != null)
        {
            stopSet.add(line);
        }

        br.close();
        return stopSet;
    }
}
/* End of Utils.java */