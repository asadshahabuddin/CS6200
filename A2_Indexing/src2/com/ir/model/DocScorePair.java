package com.ir.model;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 13, 2015
 */

public class DocScorePair
{
    private String docNo;
    private Double score;

    public DocScorePair(String docNo, Double score)
    {
        this.docNo = docNo;
        this.score = score;
    }

    public void setDocNo(String docNo)
    {
        this.docNo = docNo;
    }

    public String getDocNo()
    {
        return docNo;
    }

    public void setScore(Double score)
    {
        this.score = score;
    }

    public Double getScore()
    {
        return score;
    }
}
/* End of DocScorePair.java */