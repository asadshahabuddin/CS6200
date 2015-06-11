package com.ir.index;

/* Import list */
import java.util.ArrayList;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 8, 2015
 */

public class Entry
{
    private long tf;
    private ArrayList<Long> offs;

    /**
     * Constructor 1.
     */
    public Entry()
    {
        tf   = 0;
        offs = new ArrayList<Long>();
    }

    /**
     * Constructor 2.
     * @param tf
     *            Term frequency in the document.
     * @param offs
     *            List of offsets for the term in the document.
     */
    public Entry(long tf, ArrayList<Long> offs)
    {
        this.tf = tf;
        this.offs = offs;
    }

    public void setTf(long tf)
    {
        this.tf = tf;
    }

    public void addTf()
    {
        this.tf += 1;
    }

    public long getTf()
    {
        return tf;
    }

    public void setOffs(ArrayList<Long> offs)
    {
        this.offs = offs;
    }

    public void addOff(long off)
    {
        offs.add(off);
    }

    public ArrayList<Long> getOffs()
    {
        return offs;
    }
}
/* End of Entry.java */