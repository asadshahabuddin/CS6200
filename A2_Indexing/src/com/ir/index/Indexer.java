package com.ir.index;

/* Import list */
import java.util.Map;
import java.util.HashMap;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.io.BufferedReader;
import com.ir.global.Properties;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 8, 2015
 */

public class Indexer
{
    private BufferedReader br;
    private FileWriter fw;
    private HashMap<Long, HashMap<Long, Entry>> index;

    public void sortTokens(String tokenFile)
    {

    }

    public void index(String tokenFile)
        throws IOException
    {
        String line = "";
        long minIdx = -1;
        long maxIdx = 1000;
        boolean keepGoing = true;

        // while(keepGoing)
        // {
            br = new BufferedReader(new FileReader(Properties.FILE_TUPLES_TEXT));
            index = new HashMap<Long, HashMap<Long, Entry>>();
            keepGoing = false;

            while ((line = br.readLine()) != null)
            {
                String[] token = line.split("\\s");
                long termId = Long.valueOf(token[0]);
                long docId = Long.valueOf(token[1]);

                if (termId > minIdx && termId < maxIdx)
                {
                    if (!index.containsKey(termId))
                    {
                        index.put(termId, new HashMap<Long, Entry>());
                    }
                    if (!index.get(termId).containsKey(docId))
                    {
                        HashMap<Long, Entry> entryMap = new HashMap<Long, Entry>();
                        entryMap.put(docId, new Entry(0, new ArrayList<Long>()));
                        index.put(termId, entryMap);
                    }
                    index.get(termId).get(docId).addTf();
                    index.get(termId).get(docId).addOff(Long.valueOf(token[2]));
                    keepGoing = true;
                }
            }

            updateIndex();
            minIdx += 1000;
            maxIdx += 1000;
        // }

        br.close();
    }

    public void updateIndex()
        throws IOException
    {
        fw = new FileWriter(Properties.FILE_INDEX, true);
        StringBuilder sb = new StringBuilder();

        for(Map.Entry<Long, HashMap<Long, Entry>> outerEntry : index.entrySet())
        {
            for(Map.Entry<Long, Entry> innerEntry : outerEntry.getValue().entrySet())
            {
                sb.append(outerEntry.getKey() + " " +
                          innerEntry.getKey() + " " +
                          innerEntry.getValue().getTf() + " ");
                for(Long off : innerEntry.getValue().getOffs())
                {
                    sb.append(off + ",");
                }
                sb.setLength(sb.length() - 1);
                sb.append("\n");

                fw.write(sb.toString());
                sb = new StringBuilder();
            }
        }

        fw.close();
    }

    /**
     * Main method for unit testing.
     * @param args
     *            Program arguments.
     */
    public static void main(String[] args)
    {
        Indexer i = new Indexer();

        try
        {
            i.index(Properties.FILE_TUPLES_TEXT);
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}
/* End of Indexer.java */