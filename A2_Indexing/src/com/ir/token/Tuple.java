package com.ir.token;

/* Import list */
import java.io.Serializable;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 5, 2015
 */

public class Tuple implements Serializable
{
    private long termId;
    private long docId;
    private long pos;

    /**
     * Constructor 1
     */
    public Tuple()
    {
        termId = 0;
        docId  = 0;
        pos    = 0;
    }

    /**
     * Constructor 2
     */
    public Tuple(long termId, long docId, long pos)
    {
        this.termId = termId;
        this.docId  = docId;
        this.pos    = pos;
    }

    public void setTermId(long termId)
    {
        this.termId = termId;
    }

    public long getTermId()
    {
        return termId;
    }

    public void setDocId(long docId)
    {
        this.docId = docId;
    }

    public long getDocId()
    {
        return docId;
    }

    public void setPos(long pos)
    {
        this.pos = pos;
    }

    public long getPos()
    {
        return pos;
    }
}
/* End of Tuple.java */