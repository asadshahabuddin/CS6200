package com.ir.index;

/* Import list */
import java.io.*;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import com.ir.global.Utils;
import com.ir.token.Tokenizer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.ir.global.Properties;
import org.tartarus.snowball.ext.PorterStemmer;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 8, 2015
 */

public class Indexer
{
    /* Static data members */
    private static HashSet stopSet;
    private static HashMap<String, Long> docMap;
    private static HashMap<String, Long> termMap;
    private static Pattern pattern;

    /* Non-static data members */
    private Tokenizer tokenizer;
    private Matcher matcher;
    private PorterStemmer stemmer;
    private HashMap<Long, HashMap<Long, Entry>> index;
    private HashMap<Long, Long> catalog;
    private BufferedReader br;
    private FileWriter fw;
    int docIdx  = 0;
    int fileIdx = 0;

    /**
     * Static block.
     */
    static
    {
        try
        {
            stopSet = Tokenizer.createStopSet();
            docMap  = Tokenizer.getDocMap();
            termMap = Tokenizer.getTermMap();
            pattern = Pattern.compile(Properties.REGEX_TOKEN);
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        catch(ClassNotFoundException cnfe)
        {
            cnfe.printStackTrace();
        }
    }

    /**
     * Constructor
     */
    public Indexer()
    {
        tokenizer = new Tokenizer();
        stemmer   = new PorterStemmer();
        index     = new HashMap<Long, HashMap<Long, Entry>>();
        catalog   = new HashMap<Long, Long>();
    }

    /**
     * Index all files in a directory.
     * @param dir
     *           The directory containing all the files to be indexed.
     * @throws IOException
     */
    public void index(String dir)
        throws IOException
    {
        for(File file : new File(dir).listFiles())
        {
            indexFile(file.getAbsolutePath());
        }
        /* Write the remaining index and the catalog to the file system */
        writeIndexToFS(++fileIdx);
        serializeCatalog(fileIdx);
    }

    /**
     * Index a file.
     * @param file
     *            The file to be indexed.
     * @throws IOException
     */
    public void indexFile(String file)
        throws IOException
    {
        Utils.echo("Indexing file " + file);
        for(Map.Entry<String, String> entry : tokenizer.createDocInfoMap(file).entrySet())
        {
            indexDocument(entry.getKey(), entry.getValue());
            if(++docIdx == 1000)
            {
                writeIndexToFS(++fileIdx);
                serializeCatalog(fileIdx);
                index.clear();
                catalog.clear();
                docIdx = 0;
            }
        }
    }

    /**
     * Index a document identified by the document number.
     * @param docNum
     *            The document number.
     * @param text
     *            Contents of the document.
     */
    public void indexDocument(String docNum, String text)
    {
        long docId = docMap.get(docNum);
        matcher = pattern.matcher(text);

        while(matcher.find())
        {
            /* Do not process stop words */
            if(stopSet.contains(matcher.group(0).toLowerCase()))
            {
                continue;
            }

            /* Stem individual terms */
            stemmer.setCurrent(matcher.group(0).toLowerCase());
            stemmer.stem();
            long termId = termMap.get(stemmer.getCurrent());

            /* Update the index */
            if(!index.containsKey(termId))
            {
                index.put(termId, new HashMap<Long, Entry>());
            }
            if(!index.get(termId).containsKey(docId))
            {
                index.get(termId).put(docId, new Entry());
            }
            index.get(termId).get(docId).addTf();
            index.get(termId).get(docId).addOff(matcher.start());
        }
    }

    /**
     * Write the index to the file system.
     * @param idx
     *            Index of the index file used as its file name suffix.
     * @throws IOException
     */
    public void writeIndexToFS(int idx)
        throws IOException
    {
        Utils.echo("Executing batch write to the file system with index " + idx + "\n");
        fw = new FileWriter(Properties.DIR_IDX + "/part" + idx + ".idx");
        StringBuilder sb = new StringBuilder();
        long curOff = 0;

        for(Map.Entry<Long, HashMap<Long, Entry>> entryMap : index.entrySet())
        {
            sb.append(entryMap.getKey() + " ");
            for(Map.Entry<Long, Entry> entry : entryMap.getValue().entrySet())
            {
                sb.append(entry.getKey() + " " +
                          entry.getValue().getTf() + " ");
                for(Long off : entry.getValue().getOffs())
                {
                    sb.append(off + " ");
                }
            }
            sb.setLength(sb.length() - 1);
            sb.append("\n");
            fw.write(sb.toString());
            catalog.put(entryMap.getKey(), curOff);
            curOff += sb.length();
            sb = new StringBuilder();
        }
        fw.close();
    }

    /**
     * Serialize the catalog corresponding to an index part file denoted
     * by the index argument.
     */
    public void serializeCatalog(int idx)
        throws IOException
    {
        ObjectOutputStream out = new ObjectOutputStream(
            new FileOutputStream(Properties.DIR_CATALOG + "/part" + idx + ".catalog"));
        out.writeObject(catalog);
        out.close();

        /* DEBUG : Write the map to file system */
        fw = new FileWriter(Properties.DIR_CATALOG + "/catalog" + idx + ".txt");
        StringBuilder sb = new StringBuilder();
        for(Long key : catalog.keySet())
        {
            sb.append(key + " " + catalog.get(key) + "\n");
        }
        fw.write(sb.toString());
        fw.close();
    }

    /**
     * Main method for unit testing.
     * @param args
     *            Program arguments.
     */
    public static void main(String[] args)
    {
        /* Calculate start time */
        long startTime = System.nanoTime();

        Utils.cout("\n");
        Utils.cout("=======\n");
        Utils.cout("INDEXER\n");
        Utils.cout("=======\n");

        try
        {
            Indexer i = new Indexer();
            i.index("E:/Home/Repository/Java/IdeaProjects/A2_Indexing/input");
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        finally
        {
            Utils.elapsedTime(startTime, "Creation of index completed.");
        }
    }
}
/* End of Indexer.java */