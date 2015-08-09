package com.ir.ml;

/* Import list */
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Author : Asad Shahabuddin
 * Created: Aug 3, 2015
 */

public class Queue
{
    private PriorityQueue<NodeScorePair> queue;

    /**
     * Comparator classes for the priority queue.
     */
    static class QueueAsc implements Comparator<NodeScorePair>
    {
        public int compare(NodeScorePair nsp1, NodeScorePair nsp2)
        {
            return Double.valueOf(nsp1.getScore()).compareTo(Double.valueOf(nsp2.getScore()));
        }
    }
    static class QueueDesc implements Comparator<NodeScorePair>
    {
        public int compare(NodeScorePair nsp1, NodeScorePair nsp2)
        {
            return Double.valueOf(nsp2.getScore()).compareTo(Double.valueOf(nsp1.getScore()));
        }
    }

    /**
     * Constructor
     */
    public Queue()
    {
        queue = new PriorityQueue<NodeScorePair>(1000, new QueueAsc());
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
            new PriorityQueue<NodeScorePair>(queue.size(), new QueueDesc());
        revQueue.addAll(queue);
        queue = revQueue;
    }
}
/* End of Queue.java */