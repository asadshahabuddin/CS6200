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
     * Get the catalog for an index file identified by the argument.
     * @return
     *           The catalog for the specified index.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static HashMap<Long, Long> getCatalog(int idx)
            throws IOException, ClassNotFoundException
    {
        ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(Properties.DIR_CATALOG + "/part" + idx + ".catalog"));
        HashMap<Long, Long> catalog = (HashMap<Long, Long>) in.readObject();
        in.close();
        return catalog;
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
        index.clear();
        catalog.clear();
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
     * Serialize the catalog corresponding to a part-index file denoted
     * by the argument.
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
     * Merge all the part-indices into a single index.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void mergeIndices()
        throws IOException, ClassNotFoundException
    {
        Utils.cout("\n>Unifying indices into a master index\n");
        int n = 85;
        int jump = 1;
        while(jump < n)
        {
            for (int i = 1; i <= n; i += jump * 2)
            {
                Utils.echo("Merging indices " + i + " and " + (jump + i));
                mergeIndicesWithIdx(i, jump + i);
            }
            jump *= 2;
        }
    }

    /**
     * Merge the two indices identified by the arguments.
     * @param idx1
     *            Identifier for the first index file.
     * @param idx2
     *            Identifier for the second index file.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void mergeIndicesWithIdx(int idx1, int idx2)
        throws IOException, ClassNotFoundException
    {
        File file1 = new File(Properties.DIR_IDX + "/part" + idx1 + ".idx");
        File file2 = new File(Properties.DIR_IDX + "/part" + idx2 + ".idx");
        if(!file1.exists() || !file2.exists())
        {
            return;
        }

        BufferedReader br1 = new BufferedReader(new FileReader(file1));
        RandomAccessFile raf = new RandomAccessFile(file2, "r");
        FileWriter fw = new FileWriter(Properties.DIR_IDX + "/part0.idx", true);
        catalog = getCatalog(idx1);
        HashMap<Long, Long> catalog2 = getCatalog(idx2);
        HashSet<Long> oldTerms = new HashSet<Long>();
        String line = "";
        long curOff = 0;

        /* Merge the two index files based on term ids */
        while((line = br1.readLine()) != null)
        {
            Long termId = Long.valueOf(line.substring(0, line.indexOf(" ")));
            if(catalog2.containsKey(termId))
            {
                raf.seek(catalog2.get(termId));
                String newLine = raf.readLine();
                line += newLine.substring(newLine.indexOf(" "));
            }
            fw.write(line + "\n");
            catalog.put(termId, curOff);
            oldTerms.add(termId);
            curOff += line.length() + 1;
        }
        br1.close();
        raf.seek(0);

        /* Merge the remainder of the second index file */
        while((line = raf.readLine()) != null)
        {
            Long termId = Long.valueOf(line.substring(0, line.indexOf(" ")));
            if(!oldTerms.contains(termId))
            {
                fw.write(line + "\n");
                catalog.put(termId, curOff);
                curOff += line.length() + 1;
            }
        }
        fw.close();
        raf.close();
        serializeCatalog(idx1);

        /* Delete the first index file and rename part0.idx */
        file1.delete();
        file2.delete();
        new File(Properties.DIR_IDX + "/part0.idx").renameTo(file1);
    }

    /**
     * Create a map of Document IDs and their corresponding 'Entry' objects
     * from a line.
     * @param s
     *            The line to parse.
     * @return
     *            The resulting map of Document IDs and the corresponding
     *            'Entry' object.
     */
    public HashMap<String, Entry> getIdxEntryMap(String s)
    {
        HashMap<String, Entry> idxEntryMap = new HashMap<String, Entry>();
        String[] words = s.split("\\s");

        for(int i = 1; i < words.length; i++)
        {
            Entry entry = new Entry();
            int j;
            for(j = i + 2;
                j < words.length &&
                j < (i + 2 + Integer.valueOf(words[i + 1]));
                j++)
            {
                entry.addTf();
                entry.addOff(Long.valueOf(words[j]));
            }
            idxEntryMap.put(words[i], entry);
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
            i.mergeIndices();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        catch(ClassNotFoundException cnfe)
        {
            cnfe.printStackTrace();
        }
        finally
        {
            Utils.elapsedTime(startTime, "Creation of index completed.");
        }
    }
}
/* End of Indexer.java */