package com.ir.global;

/* Import list */
import java.util.Map;
import java.util.HashMap;
import com.ir.index.Entry;
import java.io.IOException;
import com.ir.index.Indexer;
import com.ir.token.Tokenizer;
import java.io.RandomAccessFile;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 12, 2015
 */

public class IndexClient
{
    private static HashMap<String, String> docMap;
    private static HashMap<String, Long> invDocMap;
    private static HashMap<String, Long> termMap;
    private static HashMap<Long, Long> catalog;
    private static RandomAccessFile file;

    static
    {
        try
        {
            docMap    = new HashMap<String, String>();
            invDocMap = Tokenizer.getDocMap();
            termMap   = Tokenizer.getTermMap();
            catalog   = Indexer.getCatalog(0);
            file      = new RandomAccessFile(Properties.DIR_IDX + "/part0.idx", "r");

            /* Create mappings from document numbers to their corresponding IDs */
            for(Map.Entry<String, Long> entry : invDocMap.entrySet())
            {
                docMap.put(String.valueOf(entry.getValue()), entry.getKey());
            }
        }
        catch(ClassNotFoundException cnfe)
        {
            cnfe.printStackTrace();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * Create a map of Document IDs and their corresponding 'Entry' objects
     * from a line.
     * @param term
     *            The line to parse.
     * @return
     *            The resulting map of Document IDs and the corresponding
     *            'Entry' object.
     */
    public static HashMap<String, Entry> queryTerm(String term)
        throws ClassNotFoundException, IOException
    {
        HashMap<String, Entry> idxEntryMap = new HashMap<String, Entry>();
        Long termId = termMap.get(term);
        if(termId == null)
        {
            return idxEntryMap;
        }

        /* Read the index entry for the specified term */
        file.seek(catalog.get(termId));
        String[] idxEntry = file.readLine().split(" ");

        /* Populate the index entry map */
        for(int i = 1; i < idxEntry.length; i++)
        {
            Entry entry = new Entry();
            int j;
            for(j = i + 2;
                j < idxEntry.length && j < (i + 2 + Integer.valueOf(idxEntry[i + 1]));
                j++)
            {
                entry.addTf();
                entry.addOff(Long.valueOf(idxEntry[j]));
            }
            idxEntryMap.put(docMap.get(idxEntry[i]), entry);
            i = j - 1;
        }

        return idxEntryMap;
    }

    /**
     * Main method for unit testing.
     * @param args
     *            Program arguments.
     */
    public static void main(String[] args)
    {
        try
        {
            HashMap<String, Entry> idxEntryMap = queryTerm("new");
            for(Map.Entry<String, Entry> entry : idxEntryMap.entrySet())
            {
                Utils.cout(entry.getKey() + " (TF=" + entry.getValue().getTf() + ") - ");
                for(Long off : entry.getValue().getOffs())
                {
                    Utils.cout(off + " ");
                }
                Utils.cout("\n");
            }
        }
        catch(ClassNotFoundException cnfe)
        {
            cnfe.printStackTrace();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}
/* End of IndexClient.java */