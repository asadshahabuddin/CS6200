package com.ir.model;

/* Import list */
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 13, 2015
 */

public class Queue
{
    private PriorityQueue<DocScorePair> queue;

    /* Comparator classes for the priority queue */
    /* Comparator class 1 */
    static class PriorityQueueSort implements Comparator<DocScorePair>
    {
        @Override
        public int compare(DocScorePair dfp1, DocScorePair dfp2)
        {
            return dfp1.getScore().compareTo(dfp2.getScore());
        }
    }

    /* Comparator class 2 */
    static class PriorityQueueReverse
            implements Comparator<DocScorePair>
    {
        @Override
        public int compare(DocScorePair dfp1, DocScorePair dfp2)
        {
            return dfp2.getScore().compareTo(dfp1.getScore());
        }
    }

    /* Constructor */
    protected Queue()
    {
        queue = new PriorityQueue<DocScorePair>(100, new PriorityQueueSort());
    }

    /* Add an element to the priority queue */
    protected boolean add(DocScorePair dfp)
    {
        if(dfp == null)
        {
            return false;
        }
        queue.offer(dfp);
        if(queue.size() == 1001)
        {
            queue.remove();
        }
        return true;
    }

    /* Poll the priority queue */
    protected DocScorePair remove()
    {
        if(queue.size() == 0)
        {
            return null;
        }
        return queue.remove();
    }

    /* Reverse the priority queue */
    protected void reverse()
    {
        PriorityQueue<DocScorePair> reverseQueue =
            new PriorityQueue<DocScorePair>(queue.size(), new PriorityQueueReverse());
        reverseQueue.addAll(queue);
        queue = reverseQueue;
    }
}
/* End of Queue.java */