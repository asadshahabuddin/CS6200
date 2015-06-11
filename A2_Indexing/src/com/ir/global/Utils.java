package com.ir.global;

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
}
/* End of Utils.java */