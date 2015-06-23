package com.ir.crawl;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 22, 2015
 */

public class Frontier
{
    /* Static data members */
    private static int _id;

    /* Non-static data members */
    private int id;
    private String url;
    private int inLinkCount;

    static
    {
        _id = -1;
    }

    /**
     * Constructor 1.
     */
    public Frontier()
    {
        id          = ++_id;
        url         = "";
        inLinkCount = 0;
    }

    /**
     * Constructor 2.
     * @param url
     *            The canonicalized page URL.
     * @param inLinkCount
     *            The in-link count.
     */
    public Frontier(String url, int inLinkCount)
    {
        id               = ++_id;
        this.url         = url;
        this.inLinkCount = inLinkCount;
    }

    public int getId()
    {
        return id;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getUrl()
    {
        return url;
    }

    public void setInLinkCount(int inLinkCount)
    {
        this.inLinkCount = inLinkCount;
    }

    public void incrInLinkCount()
    {
        this.inLinkCount++;
    }

    public int getInLinkCount()
    {
        return inLinkCount;
    }
}
/* End of Frontier.java */