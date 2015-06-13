package com.ir.token;

/* Import list */
import java.util.Comparator;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 9, 2015
 */

public class TupleComparator implements Comparator<Tuple>
{
    @Override
    public int compare(Tuple t1, Tuple t2)
    {
        int res = Long.compare(t1.getTermId(), t2.getTermId());
        if(res == 0)
        {
            res = Long.compare(t1.getDocId(), t2.getDocId());
        }
        return res;
    }
}
/* End of TupleComparator.java */