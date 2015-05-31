package com.ir.es;

/* Import list */
import java.util.Comparator;
import java.util.PriorityQueue;

public class Queue
{
    private PriorityQueue<DocScorePair> queue;
    
    /* Comparator classes for the priority queue */
    /* Comparator class 1 */
    static class PriorityQueueSort
        implements Comparator<DocScorePair>
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
    
    /* Main method for unit testing */
    public static void main(String[] args)
    {
        DocScorePair dfp1 = new DocScorePair("A", 0.5);
        DocScorePair dfp2 = new DocScorePair("B", 0.3);
        DocScorePair dfp3 = new DocScorePair("C", 0.2);
        DocScorePair dfp4 = new DocScorePair("A", 0.44);
        
        DocScorePair dfp = null;
        Queue queue = new Queue();
        queue.add(dfp1);
        queue.add(dfp2);
        queue.add(dfp3);
        queue.add(dfp4);
        /*
        while((dfp = queue.remove()) != null)
        {
            System.out.println("Document " + dfp.getDocNo() +
                               " with frequency " + dfp.getScore());
        }
        */
        
        queue.reverse();
        while((dfp = queue.remove()) != null)
        {
            System.out.println("Document " + dfp.getDocNo() +
                               " with frequency " + dfp.getScore());
        }
    }
}
/* End of Queue.java */