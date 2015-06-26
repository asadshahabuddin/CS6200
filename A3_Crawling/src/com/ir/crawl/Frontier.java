package com.ir.crawl;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 22, 2015
 */

public class Frontier
{
    private String url;
    private int inLinkCount;

    /**
     * Constructor 1.
     */
    public Frontier()
    {
        url         = "";
        inLinkCount = 0;
    }

    /**
     * Constructor 2.
     * @param url
     *            The URL.
     * @param inLinkCount
     *            The in-link count.
     */
    public Frontier(String url, int inLinkCount)
    {
        this.url         = url;
        this.inLinkCount = inLinkCount;
    }

    /**
     * Set the URL
     * @param url
     *            The URL.
     */
    public void setUrl(String url)
    {
        this.url = url;
    }

    /**
     * Get the URL.
     * @return
     *            The URL.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Set the in-link count.
     * @param inLinkCount
     *            The in-link count.
     */
    public void setInLinkCount(int inLinkCount)
    {
        this.inLinkCount = inLinkCount;
    }

    /**
     * Increment the in-link count by 1.
     */
    public void incrInLinkCount()
    {
        this.inLinkCount++;
    }

    /**
     * Get the in-link count.
     * @return
     *            The in-link count.
     */
    public int getInLinkCount()
    {
        return inLinkCount;
    }
}
/* End of Frontier.java */