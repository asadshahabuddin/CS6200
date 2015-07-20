package com.ir.eval;

/* Import list */
import java.util.Comparator;
import java.util.PriorityQueue;
import com.ir.global.Properties;

/**
 * Author : Asad Shahabuddin
 * Created: Jul 17, 2015
 */

public class Queue
{
    private PriorityQueue<NodeScorePair> queue;

    /**
     * Secondary sort.
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
     * Ascending order.
     */
    static class AscComparator implements Comparator<NodeScorePair>
    {
        @Override
        public int compare(NodeScorePair nsp1, NodeScorePair nsp2)
        {
            return Double.valueOf(nsp1.getScore()).compareTo(Double.valueOf(nsp2.getScore()));
        }
    }

    /**
     * Descending order.
     */
    static class DescComparator implements Comparator<NodeScorePair>
    {
        @Override
        public int compare(NodeScorePair nsp1, NodeScorePair nsp2)
        {
            return Double.valueOf(nsp2.getScore()).compareTo(Double.valueOf(nsp1.getScore()));
        }
    }

    /**
     * Constructor.
     * @param evalType
     *            A key indicating the comparator type.
     */
    public Queue(int evalType)
    {
        if(evalType == Properties.KEY_TRECEVAL)
        {
            queue = new PriorityQueue<>(1000, new NSPComparator());
        }
        else if(evalType == Properties.KEY_IREVAL)
        {
            queue = new PriorityQueue<>(1000, new AscComparator());
        }
    }

    /**
     * Add an object to the priority queue.
     * @param nsp
     *            The NodeScorePair object.
     * @return
     *            true iff the object was added successfully.
     */
    public boolean add(NodeScorePair nsp)
    {
        if(nsp == null)
        {
            return false;
        }
        queue.offer(nsp);
        if(queue.size() == 1001)
        {
            queue.remove();
        }
        return true;
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

    /**
     * Reverse the priority queue.
     */
    public void reverse()
    {
        PriorityQueue<NodeScorePair> revQueue =
            new PriorityQueue<>(queue.size(), new DescComparator());
        revQueue.addAll(queue);
        queue = revQueue;
    }
}
/* End of Queue.java */