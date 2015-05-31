package com.ir.es;

public class DocScorePair
{
    private String docNo;
    private Double score;
    
    protected DocScorePair(String docNo, Double score)
    {
        this.docNo = docNo;
        this.score = score;
    }
    
    protected void setDocNo(String docNo)
    {
        this.docNo = docNo;
    }
    
    protected String getDocNo()
    {
        return docNo;
    }
    
    protected void setScore(Double score)
    {
        this.score = score;
    }
    
    protected Double getScore()
    {
        return score;
    }
}
/* End of DocFreqPair.java */