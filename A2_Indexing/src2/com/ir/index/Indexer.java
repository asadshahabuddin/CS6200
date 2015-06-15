package com.ir.index;

/* Import list */
import java.io.*;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.TreeMap;
import com.ir.global.Utils;
import com.ir.token.Tokenizer;
import java.util.concurrent.*;
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
    /* Constant */
    private static final int INDEX_COUNT = 85;

    /* Static data members */
    private static boolean stopSwitch;
    private static boolean stemSwitch;
    private static HashSet<String> stopSet;
    private static HashMap<String, Long> docMap;
    private static HashMap<String, Long> termMap;
    private static Pattern pattern;

    /* Non-static data members */
    private Tokenizer tokenizer;
    private Matcher matcher;
    private PorterStemmer stemmer;
    private TreeMap<Long, HashMap<Long, Entry>> index;
    private HashMap<Long, Long> catalog;
    private HashSet<Long>[] catalogues;
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
            stopSet = Utils.createStopSet();
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
        tokenizer  = new Tokenizer();
        stemmer    = new PorterStemmer();
        index      = new TreeMap<Long, HashMap<Long, Entry>>();
        catalog    = new HashMap<Long, Long>();
        catalogues = new HashSet[INDEX_COUNT];
        for(int i = 0; i < INDEX_COUNT; i++)
        {
            catalogues[i] = new HashSet<Long>();
        }
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
        /* Write the remaining index and the catalogues to the file system */
        writeIndexToFS(++fileIdx);
        serializeCatalogues();
        index.clear();
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
                index.clear();
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
            /* Do not process stop words, et cetera. */
            String term = Utils.filterText(matcher.group(0).toLowerCase());
            if(stopSet.contains(term) ||
               term.length() == 0)
            {
                continue;
            }

            /* Stem individual terms */
            if(stemSwitch)
            {
                stemmer.setCurrent(term);
                stemmer.stem();
                term = stemmer.getCurrent();
            }
            long termId = termMap.get(term);

            /* Update the index */
            if(!index.containsKey(termId))
            {
                index.put(termId, new HashMap<Long, Entry>());
            }
            if(!index.get(termId).containsKey(docId))
            {
                index.get(termId).put(docId, new Entry());
            }
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

        for(Map.Entry<Long, HashMap<Long, Entry>> entryMap : index.entrySet())
        {
            sb.append(entryMap.getKey() + " ");
            for(Map.Entry<Long, Entry> entry : entryMap.getValue().entrySet())
            {
                sb.append(entry.getKey() + " ");
                for(Long off : entry.getValue().getOffs())
                {
                    sb.append(off + ";");
                }
                sb.setLength(sb.length() - 1);
                sb.append(" ");
            }
            sb.setLength(sb.length() - 1);
            sb.append("\n");
            fw.write(sb.toString());
            catalogues[idx - 1].add(entryMap.getKey());
            sb = new StringBuilder();
        }
        fw.close();
    }

    /**
     * Get an array on all the index file objects.
     * @param n
     *            The total number of partial index files.
     * @return
     *            An array of all the index file objects.
     * @throws IOException
     */
    public BufferedReader[] getIdxFiles(int n)
        throws IOException
    {
        BufferedReader[] files = new BufferedReader[n];
        for(int i = 1; i <= n; i++)
        {
            files[i - 1] = new BufferedReader(new FileReader(
                    Properties.DIR_IDX + "/part" + i + ".idx"));
        }
        return files;
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
     * Serialize the catalog objects corresponding to the part-index files.
     * @throws IOException
     */
    public void serializeCatalogues()
        throws IOException
    {
        ObjectOutputStream out = new ObjectOutputStream(
            new FileOutputStream(Properties.DIR_CATALOG + "/" + Properties.FILE_CATALOG_OBJ));
        out.writeObject(catalogues);
        out.close();
    }

    /**
     * Get an array of all the catalogues.
     * @param n
     *            The total number of partial index files.
     * @return
     *            An array of all the catalogues.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public HashSet<Long>[] getCatalogues(int n)
        throws IOException, ClassNotFoundException
    {
        Utils.cout("\n>Loading all the catalogues");
        ObjectInputStream in = new ObjectInputStream(
            new FileInputStream(Properties.DIR_CATALOG + "/" + Properties.FILE_CATALOG_OBJ));
        HashSet<Long>[] catalogues = (HashSet<Long>[]) in.readObject();
        return catalogues;
    }

    /**
     * Merge all the indices into a master index.
     * Revision 2 of merge indices has a much better time complexity.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void mergeIndices2()
        throws IOException, ClassNotFoundException,
               ExecutionException, InterruptedException
    {
        long vocabSize = Long.valueOf(Tokenizer.
                                      getStatistics().
                                      get(Properties.KEY_VOCAB_SIZE));
        HashSet<Long>[] catalogues = getCatalogues(INDEX_COUNT);
        BufferedReader[] files = getIdxFiles(INDEX_COUNT);
        FileWriter fw = new FileWriter(Properties.DIR_IDX + "/part0.idx", true);
        catalog = new HashMap<Long, Long>();
        StringBuilder sb = new StringBuilder();
        String line = "";
        long curOff = 0;

        Utils.cout("\n>Unifying indices into a master index\n");
        for(long i = 0; i < vocabSize; i++)
        {
            sb.append(i);
            for(int j = 0; j < INDEX_COUNT; j++)
            {
                if(catalogues[j].contains(i))
                {
                    line = files[j].readLine();
                    sb.append(line.substring(line.indexOf(" ")));
                }
            }
            fw.write(sb.append("\n").toString());
            catalog.put(i, curOff);
            curOff += sb.length();
            sb = new StringBuilder();

            /* Log every 1000th iteration */
            if(i % 1000 == 0)
            {
                Utils.echo("Passed term with ID " + i);
            }
        }

        fw.close();
        serializeCatalog(0);
        Utils.cout("\n>Cleaning up");
        cleanup(INDEX_COUNT, files);
    }

    /**
     * Execute cleanup of all the partial indices and catalog files.
     * @param n
     *            The total number of partial index files.
     */
    public void cleanup(int n, BufferedReader[] files)
        throws IOException
    {
        for(int i = 1; i <= n; i++)
        {
            new File(Properties.DIR_CATALOG + "/" + Properties.FILE_CATALOG_OBJ).delete();
            files[i - 1].close();
            new File(Properties.DIR_IDX + "/part" + i + ".idx").delete();
        }
    }

    /**
     * DEPRECATED : Merge all the part-indices into a single index.
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
     * DEPRECATED : Merge the two indices identified by the arguments.
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

        if(args.length < 2)
        {
            Utils.error("A minimum of 2 arguments are required.");
            Utils.echo("-stop=true/false -stem=true/false");
            System.exit(-1);
        }

        stopSwitch = args[0].equalsIgnoreCase("-stop=true");
        stemSwitch = args[1].equalsIgnoreCase("-stem=true");
        Utils.echo("Stop word removal has been set to " + stopSwitch);
        Utils.echo("Stemming has been set to " + stemSwitch + "\n");
        if(!stopSwitch)
        {
            stopSet = new HashSet<String>();
        }

        try
        {
            Indexer i = new Indexer();
            /* Create the partial indices */
            i.index("E:/Home/Repository/Java/IdeaProjects/A2_Indexing/input");
            startTime = Utils.elapsedTime(startTime, "Creation of index completed.");
            /* Merge the partial indices into a unified index */
            i.mergeIndices2();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        catch(ClassNotFoundException cnfe)
        {
            cnfe.printStackTrace();
        }
        catch(ExecutionException exece)
        {
            exece.printStackTrace();
        }
        catch(InterruptedException intre)
        {
            intre.printStackTrace();
        }
        finally
        {
            Utils.elapsedTime(startTime, "\nUnification of partial indices completed.");
        }
    }
}
/* End of Indexer.java */