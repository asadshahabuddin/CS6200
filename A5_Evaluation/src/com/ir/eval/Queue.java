package com.ir.eval;

/* Import list */
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Author : Asad Shahabuddin
 * Created: Jul 17, 2015
 */

public class Queue
{
    private PriorityQueue<NodeScorePair> queue;

    /**
     * Comparator class for the priority queue.
     */
    static class NSPComparator implements Comparator<NodeScorePair>
    {
        @Override
        public int compare(NodeScorePair nsp1, NodeScorePair nsp2)
        {
            int res = Double.valueOf(nsp2.getScore()).compareTo(Double.valueOf(nsp1.getScore()));
            if(res == 0)
            {
                res = nsp2.getNode().compareTo(nsp1.getNode());
            }
            return res;
        }
    }

    /**
     * Constructor.
     */
    public Queue()
    {
        queue = new PriorityQueue<>(1000, new NSPComparator());
    }

    /**
     * Add an object to the priority queue.
     * @param nsp
     *            The NodeScorePair object.
     * @return
     *            true if the object was added successfully.
     */
    public boolean add(NodeScorePair nsp)
    {
        if(nsp == null)
        {
            return false;
        }
        return queue.offer(nsp);
    }

    /**
     * Remove and return an object from the priority queue.
     * @return
     *            A NodeScorePair object.
     */
    public NodeScorePair remove()
    {
        if(queue.size() == 0)
        {
            return null;
        }
        return queue.remove();
    }
}
/* End of Queue.java */