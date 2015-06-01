package com.ir.es;

/* Import list */
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;
import org.json.JSONObject;
import java.io.IOException;
import java.io.BufferedReader;
import org.json.JSONException;
import org.elasticsearch.node.Node;
import org.elasticsearch.client.Client;
import java.util.concurrent.ExecutionException;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import org.elasticsearch.action.termvector.TermVectorResponse;

public class Utils
{
	private static Node node;
    private static Client client;
	private static BufferedReader br;
	private static FileWriter fw;
	
	/* Calculate the length of each document and write to file system */
    public static void writeDocLengthsToFS()
    {
    	try
    	{
	    	br = new BufferedReader(new FileReader(Properties.FILE_DOCLIST));
	    	fw = new FileWriter(Properties.FILE_DOCLEN, true);
	        String line = br.readLine();
	        
	        while((line = br.readLine()) != null)
	        {
	        	String docno = line.split("\\s")[1];
	        	fw.write(docno + " " + calcDocLength(docno) + "\n");
	        }
    	}
    	catch(IOException ioe)
    	{
    		ioe.printStackTrace();
    	}
    	catch(ExecutionException exe)
    	{
    		exe.printStackTrace();
    	}
    	catch(InterruptedException intre)
    	{
    		intre.printStackTrace();
    	}
    	catch(JSONException jsone)
    	{
    		jsone.printStackTrace();
    	}
    	finally
    	{
    		/* Close reader and writer objects */
    		if(br != null)
    		{
    			try
    			{
    				br.close();
    			}
    			catch(IOException ioe)
    			{
    				ioe.printStackTrace();
    			}
    		}
    		if(fw != null)
    		{
    			try
    			{
    				fw.close();
    			}
    			catch(IOException ioe)
    			{
    				ioe.printStackTrace();
    			}
    		}
    	}
    }
    
    /* Convert document number to ID */
    public static String docNumToId(String docno)
        throws ExecutionException, InterruptedException,
        	   IOException, JSONException
    {
        SearchResponse res = client.prepareSearch("ap_dataset")
        						   .setTypes("document")
                                   .setSource(Query.getStatsTermsByMatchFieldBuilder("docno", docno))
                                   .execute()
                                   .actionGet();
        return new JSONObject(res.toString()).getJSONObject("hits").getJSONArray("hits")
        	   .getJSONObject(0).getString("_id");
    }
    
    /* Calculate the document length */
    public static long calcDocLength(String docno)
    	throws IOException, JSONException,
    	       InterruptedException, ExecutionException
    {
    	long docLength = 0;
    	
    	/* Execute the query and get the resultant builder */
    	TermVectorResponse res = client.prepareTermVector()
    								   .setIndex("ap_dataset")
    								   .setType("document")
    								   .setId(docNumToId(docno))
    								   .execute()
    								   .actionGet();
    	XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON).prettyPrint();
		builder.startObject();
		res.toXContent(builder, ToXContent.EMPTY_PARAMS);
		builder.endObject();
		
		try
		{
			/* Get the term vector response as a JSON object */
			JSONObject jsonObj = new JSONObject(XContentHelper.convertToJson(builder.bytes(), false))
								 .getJSONObject("term_vectors")
		    					 .getJSONObject("text")
		    					 .getJSONObject("terms");
		    Iterator<?> keys = jsonObj.keys();
		    
		    /* Iterate over all terms and add their respective frequencies */
		    while(keys.hasNext())
		    {
		    	docLength += jsonObj.getJSONObject(String.valueOf(keys.next())).getInt("term_freq");
		    }
		}
		catch(JSONException jsone)
		{
			return 0;
		}
	    
	    return docLength;
    }
    
    /* Main method */
    public static void main(String[] args)
    {
    	/* Starts client */
        node = nodeBuilder().client(true).clusterName("leoscluster").node();
        client = node.client();
        
        /* Calculate document lengths and write to file */
        writeDocLengthsToFS();
        node.close();
    }
}
/* End of Utils.java */