package com.ir.ml;

/* Import list */
import java.io.File;
import java.util.HashSet;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import com.ir.global.Properties;

/**
 * Author : Asad Shahabuddin
 * Created: Jul 31, 2015
 */

public class Train
{
    public HashSet<String> createDocList()
        throws IOException
    {
        File file = new File(Properties.FILE_QRELS);
        if(!file.exists())
        {
            BufferedReader br = new BufferedReader(new FileReader(file));
            FileWriter fw = new FileWriter(Properties.FILE_DOCLIST);
            StringBuilder sb = new StringBuilder();
            String line;

            while((line = br.readLine()) != null)
            {
                sb.append(line.split(" ")[2] + "\n");
            }

            /* Output the document list to file system and close the file handles. */
            fw.write(sb.toString());
            fw.close();
            br.close();
        }
        return getDocSet();
    }

    public HashSet<String> getDocSet()
        throws IOException
    {
        HashSet<String> docs = new HashSet<>();
        BufferedReader br = new BufferedReader(new FileReader(Properties.FILE_DOCLIST));
        String line;
        while((line = br.readLine()) != null)
        {
            docs.add(line);
        }
        return docs;
    }

    public void createFeatureMatrix()
    {
        // TODO
    }

    /**
     * Main method for unit testing.
     * @param args
     *            Program arguments.
     */
    public static void main(String[] args)
    {
        Train t = new Train();
        try
        {
            t.createDocList();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}
/* End of Train.java */