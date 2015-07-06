package com.ir.pagerank;

/**
 * Author : Asad Shahabuddin
 * Created: Jul 6, 2015
 */

public class NodeScorePair
{
    private String node;
    private double score;

    /**
     * Constructor 1.
     */
    public NodeScorePair()
    {
        node  = "";
        score = -1;
    }

    /**
     * Constructor 2.
     * @param node
     *            The node.
     * @param score
     *            The PageRank score.
     */
    public NodeScorePair(String node, double score)
    {
        this.node  = node;
        this.score = score;
    }

    /**
     * Set the node.
     * @param node
     *            The node.
     */
    public void setNode(String node)
    {
        this.node = node;
    }

    /**
     * Get the node.
     * @return
     *            The node.
     */
    public String getNode()
    {
        return node;
    }

    /**
     * Set the score.
     * @param score
     *            The PageRank score.
     */
    public void setScore(double score)
    {
        this.score = score;
    }

    /**
     * Get the score.
     * @return
     *            The PageRank score.
     */
    public double getScore()
    {
        return score;
    }
}
/* End of NodeScorePair.java */