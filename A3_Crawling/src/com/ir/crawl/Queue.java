package com.ir.crawl;

/* Import list */

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 22, 2015
 */

public class Queue
{
    private PriorityQueue<Frontier> queue;

    /**
     * Comparator classes for the priority queue.
     */
    static class QueueComparator implements Comparator<Frontier>
    {
        @Override
        public int compare(Frontier f1, Frontier f2)
        {
            int res = f2.getInLinkCount() - f1.getInLinkCount();
            if(res == 0)
            {
                res = f1.getId() - f2.getId();
            }
            return res;
        }
    }

    /**
     * Constructor.
     */
    public Queue()
    {
        queue = new PriorityQueue<Frontier>(100, new QueueComparator());
    }

    /**
     * Add an element to the priority queue.
     * @param f
     *            The Frontier object.
     * @return
     *            'true' iff the Frontier object was added successfully to the
     *            queue.
     */
    public boolean add(Frontier f)
    {
        if(f == null)
        {
            return false;
        }
        queue.offer(f);
        return true;
    }

    /**
     * Poll the priority queue.
     * @return
     *            The polled Frontier object.
     */
    public Frontier remove()
    {
        if(queue.size() == 0)
        {
            return null;
        }
        return queue.remove();
    }

    /**
     * Main method for unit testing.
     * @param args
     *            Program arguments.
     */
    public static void main(String[] args)
    {
        Queue q = new Queue();

        /* Create Frontier objects. */
        Frontier f1 = new Frontier("a", 2);
        Frontier f2 = new Frontier("b", 4);
        Frontier f3 = new Frontier("c", 1);
        Frontier f4 = new Frontier("d", 2);
        Frontier f5 = new Frontier("e", 2);
        Frontier f6 = new Frontier("f", 0);

        /* Add objects to the queue. */
        q.add(f1);
        q.add(f2);
        q.add(f3);
        q.add(f4);
        q.add(f5);
        q.add(f6);

        Frontier f = null;
        while((f = q.remove()) != null)
        {
            System.out.println("URL: "           + f.getUrl());
            System.out.println("In-link count: " + f.getInLinkCount());
        }
    }
}
/* End of Queue.java */