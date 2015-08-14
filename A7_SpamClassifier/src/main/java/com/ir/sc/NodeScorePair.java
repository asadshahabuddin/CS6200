package com.ir.sc;

/**
 * Author : Asad Shahabuddin
 * Created: Aug 14, 2015
 */

public class NodeScorePair
{
    private String node;
    private double score;

    /**
     * Constructor 1
     */
    public NodeScorePair()
    {
        node  = "";
        score = -1;
    }

    /**
     * Constructor 2
     * @param node
     *            The node.
     * @param score
     *            The evaluation score.
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
     *            The evaluation score.
     */
    public void setScore(double score)
    {
        this.score = score;
    }

    /**
     * Get the score.
     * @return
     *            The evaluation score.
     */
    public double getScore()
    {
        return score;
    }
}
/* End of NodeScorePair.java */