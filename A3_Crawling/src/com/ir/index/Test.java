package com.ir.index;

/* Import list */
import org.jsoup.Jsoup;
import com.ir.global.Utils;
import java.io.IOException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 29, 2015
 */

public class Test
{
    public static void main(String[] args)
        throws IOException
    {
        /*
        Node node = nodeBuilder().client(true).clusterName("leoscluster").node();
        Client client = node.client();

        SearchResponse res = client.prepareSearch("ak_dataset")
                                   .setTypes("document")
                                   .setQuery(QueryBuilders.matchQuery("docno", "http://en.wikipedia.org/wiki/Linux-libre"))
                                   .setExplain(true)
                                   .execute()
                                   .actionGet();

        Utils.cout(">In-links\n");
        Utils.cout(res.getHits().getHits()[0].getSource().get("inlinks"));
        */
        Document doc = Jsoup.connect("http://en.wikipedia.org/wiki/IPhone").get();
        for(Element e : doc.select("a[href]"))
        {
            Utils.cout(e.attr("abs:href") + "\n");
        }
    }
}
/* End of Test.java */