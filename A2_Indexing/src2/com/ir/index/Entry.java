package com.ir.index;

/* Import list */
import java.util.ArrayList;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 8, 2015
 */

public class Entry
{
    private int tf;
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
    public Entry(int tf, ArrayList<Long> offs)
    {
        this.tf = tf;
        this.offs = offs;
    }

    public void setTf(int tf)
    {
        this.tf = tf;
    }

    public void addTf()
    {
        this.tf += 1;
    }

    public int getTf()
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

    public void addOffs(ArrayList<Long> newOffs)
    {
        offs.addAll(newOffs);
    }

    public ArrayList<Long> getOffs()
    {
        return offs;
    }
}
/* End of Entry.java */