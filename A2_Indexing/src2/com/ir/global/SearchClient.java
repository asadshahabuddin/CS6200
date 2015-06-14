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

public class SearchClient
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
     * Create a map between document numbers and the corresponding 'Entry'
     * objects for the term.
     * @param term
     *            The line to parse.
     * @return
     *            The resulting map between document numbers and the
     *            corresponding 'Entry' objects.
     */
    public static HashMap<String, Entry> queryTerm(String term)
        throws IOException
    {
        HashMap<String, Entry> termEntryMap = new HashMap<String, Entry>();
        Long termId = termMap.get(term);
        if(termId == null)
        {
            return termEntryMap;
        }

        /* Read the index entry for the specified term */
        file.seek(catalog.get(termId));
        String[] termEntry = file.readLine().split(" ");

        /* Populate the index entry map */
        for(int i = 1; i < termEntry.length; i++)
        {
            Entry entry = new Entry();
            int j;
            for(j = i + 2;
                j < termEntry.length && j < (i + 2 + Integer.valueOf(termEntry[i + 1]));
                j++)
            {
                entry.addTf();
                entry.addOff(Long.valueOf(termEntry[j]));
            }
            termEntryMap.put(docMap.get(termEntry[i]), entry);
            i = j - 1;
        }

        return termEntryMap;
    }

    /**
     * Create a map between the document number and the corresponding 'Entry'
     * object for the term.
     * @param term
     *            The line to parse.
     * @return
     *            The resulting map between the document number and the
     *            corresponding 'Entry' object.
     */
    public static HashMap<String, Entry> queryTerm(String term, String docNum)
        throws IOException
    {
        HashMap<String, Entry> termEntryMap = new HashMap<String, Entry>();
        Long termId = termMap.get(term);
        if(termId == null)
        {
            return termEntryMap;
        }

        /* Read the index entry for the specified term */
        file.seek(catalog.get(termId));
        String[] termEntry = file.readLine().split(" ");

        /* Populate the index entry map */
        for(int i = 1; i < termEntry.length; i++)
        {
            int j;
            if(termEntry[i].equals(
                    String.valueOf(invDocMap.get(docNum))))
            {
                Entry entry = new Entry();
                for(j = i + 2;
                    j < termEntry.length && j < (i + 2 + Integer.valueOf(termEntry[i + 1]));
                    j++)
                {
                    entry.addTf();
                    entry.addOff(Long.valueOf(termEntry[j]));
                }
                termEntryMap.put(docNum, entry);
                break;
            }
            i += Integer.valueOf(termEntry[i + 1]) + 1;
        }

        return termEntryMap;
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
            int ttf = 0;
            HashMap<String, Entry> idxEntryMap = queryTerm("govern");
            for(Map.Entry<String, Entry> entry : idxEntryMap.entrySet())
            {
                ttf += entry.getValue().getTf();
                Utils.cout(entry.getKey() + " (TF=" + entry.getValue().getTf() + ") - ");
                for(Long off : entry.getValue().getOffs())
                {
                    Utils.cout(off + " ");
                }
                Utils.cout("\n");
            }
            Utils.cout("Total term frequency is " + ttf + "\n");
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}
/* End of SearchClient.java */