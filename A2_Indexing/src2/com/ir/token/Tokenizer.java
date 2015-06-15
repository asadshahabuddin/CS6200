package com.ir.token;

/* Import list */
import java.io.*;
import java.util.*;
import com.ir.global.Utils;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.ir.global.Properties;
import org.tartarus.snowball.ext.PorterStemmer;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 5, 2015
 */

public class Tokenizer
{
    /* Static data members */
    private static boolean stopSwitch;
    private static boolean stemSwitch;

    /* Non-static data members */
    private long termId;
    private long docId;
    private HashMap<String, Long> docMap;
    private HashMap<String, Long> termMap;
    private HashMap<String, Integer> docLenMap;
    private ArrayList<Tuple> tuples;
    private Pattern pattern;
    private Matcher matcher;
    private long docCount;
    private long allDocLength;
    private BufferedReader br;
    private FileWriter fw;
    private PorterStemmer stemmer;

    /**
     * Constructor
     */
    public Tokenizer()
    {
        termId       = -1;
        docId        = -1;
        docMap       = new HashMap<String, Long>();
        termMap      = new HashMap<String, Long>();
        docLenMap    = new HashMap<String, Integer>();
        tuples       = new ArrayList<Tuple>();
        pattern      = Pattern.compile(Properties.REGEX_TOKEN);
        docCount     = 0;
        allDocLength = 0;
        stemmer      = new PorterStemmer();
    }

    /**
     * Tokenize a file and produce all tuples of the form
     * (term id, document id, position).
     * @param fileName
     *            File name.
     */
    public void tokenizeFile(String fileName, HashSet<String> stopSet)
        throws IOException
    {
        for(Map.Entry<String, String> entry : createDocInfoMap(fileName).entrySet())
        {
            // tuples.addAll(tokenizeDocument(entry.getKey(), entry.getValue(), stopSet));
            tokenizeDocument(entry.getKey(), entry.getValue(), stopSet);
            docCount++;
        }
        // allDocLength = tuples.size();
    }

    /**
     * Extract the document numbers and and corresponding content
     * from all documents in the specified file.
     * @param fileName
     *            File name.
     * @return
     *            A map of document numbers and the corresponding text.
     */
    public HashMap<String, String> createDocInfoMap(String fileName)
        throws IOException
    {
        HashMap<String, String> docInfoMap = new HashMap<String, String>();
        br = new BufferedReader(new FileReader(fileName));
        String line  = "";
        String docNo = "";
        StringBuilder text  = new StringBuilder();

        while((line = br.readLine()) != null)
        {
            if(line.equals(Properties.XELEM_DOC_BEGIN))
            {
                while(!(line = br.readLine()).contains(Properties.XELEM_DOC_END))
                {
                    if(line.contains(Properties.XELEM_DOCNO_BEGIN))
                    {
                        docNo = line.substring(line.indexOf(">") + 1,
                                line.indexOf("</")).trim();
                    }
                    if(line.contains(Properties.XELEM_TEXT_BEGIN))
                    {
                        while(!(line = br.readLine()).contains(Properties.XELEM_TEXT_END))
                        {
                            text.append(line + " ");
                        }
                    }
                }

                /* For debugging purpose */
                if(docNo.equals("AP890207-0217"))
                {
                    Utils.cout(text.toString().trim() + "\n");
                }
                docInfoMap.put(docNo, text.toString().trim());
                text  = new StringBuilder();
            }
        }

        br.close();
        return docInfoMap;
    }

    /**
     * Tokenize a document and produce all tuples of the form
     * (term id, document id, position).
     * @param docNo
     *            Document number.
     * @param text
     *            Content for the document number.
     * @return
     *            A list of tuples contained in the document.
     */
    public ArrayList<Tuple> tokenizeDocument(String docNo,
                                             String text,
                                             HashSet<String> stopSet)
    {
        ArrayList<Tuple> tupleList = new ArrayList<Tuple>();
        int docLength = 0;
        matcher = pattern.matcher(text);
        /* Update the inverse document index */
        docMap.put(docNo, ++docId);

        while(matcher.find())
        {
            String term = Utils.filterText(matcher.group(0).toLowerCase());
            if(stopSet.contains(term) ||
               term.length() == 0)
            {
                continue;
            }

            if(stemSwitch)
            {
                stemmer.setCurrent(term);
                stemmer.stem();
                term = stemmer.getCurrent();
            }
            if(!termMap.containsKey(term))
            {
                termMap.put(term, ++termId);
            }
            // tupleList.add(new Tuple(termMap.get(term), docId, matcher.start()));
            docLength++;
        }

        /* Update the document length map and return */
        docLenMap.put(docNo, docLength);
        allDocLength += docLength;
        return tupleList;
    }

    /**
     * Sort the tuples by (term id, document id).
     * @return
     *            The sorted list of tuples.
     */
    private ArrayList<Tuple> sortTuples()
    {
        Collections.sort(tuples, new TupleComparator());
        return tuples;
    }

    /**
     * Serialize the inverted indices and write document statistics
     * to the file system.
     */
    public void writeObjectsToFS()
        throws IOException, ClassNotFoundException
    {
        /* Serialize the inverted document index */
        ObjectOutputStream out = new ObjectOutputStream(
            new FileOutputStream(Properties.DIR_OBJ + "/" + Properties.FILE_DOC_IDX));
        out.writeObject(docMap);
        out.close();

        /* Serialize the inverted term index */
        out = new ObjectOutputStream(
            new FileOutputStream(Properties.DIR_OBJ + "/" + Properties.FILE_TERM_IDX));
        out.writeObject(termMap);
        out.close();

        /* Serialize the document length map */
        out = new ObjectOutputStream(
            new FileOutputStream(Properties.DIR_OBJ + "/" + Properties.FILE_DOCLEN_OBJ));
        out.writeObject(docLenMap);
        out.close();

        /* Write the tuples and various maps to the file system */
        // writeTuplesToFS();
        writeMapsToFS(Properties.TYPE_DOC);
        writeMapsToFS(Properties.TYPE_TERM);
        writeMapsToFS(Properties.TYPE_DOCLEN);

        /* Write document statistics to the file system */
        fw = new FileWriter(Properties.DIR_OBJ + "/" + Properties.FILE_STATS);
        fw.write(Properties.KEY_DOC_COUNT + " " + docCount + "\n");
        fw.write(Properties.KEY_VOCAB_SIZE + " " + (termId + 1) + "\n");
        fw.write(Properties.KEY_ALL_DOC_LEN + " " + allDocLength + "\n");
        fw.write(Properties.KEY_AVG_DOC_LEN + " " + (allDocLength / docCount) + "\n");
        fw.close();
    }

    /**
     * Write all the tuples to the file system.
     * @throws IOException
     */
    private void writeTuplesToFS()
        throws IOException
    {
        fw = new FileWriter(Properties.DIR_OBJ + "/" + Properties.FILE_TUPLES_TEXT, true);
        for(Tuple tuple : tuples)
        {
            fw.write(tuple.getTermId() + " " +
                     tuple.getDocId() + " " +
                     tuple.getPos() + "\n");
        }
        fw.close();
    }

    /**
     * Write the map identified by the key to the file system.
     * @param key
     *            Identifier for the map type.
     */
    public void writeMapsToFS(int key)
        throws IOException, ClassNotFoundException
    {
        String file = "";
        StringBuilder sb = new StringBuilder();
        HashMap typeMap = new HashMap();

        if(key == Properties.TYPE_DOC)
        {
            file    = Properties.DIR_OBJ + "/" + Properties.FILE_DOC_TXT;
            typeMap = getDocMap();
        }
        else if(key == Properties.TYPE_TERM)
        {
            file    = Properties.DIR_OBJ + "/" + Properties.FILE_TERM_TXT;
            typeMap = getTermMap();
        }
        else if(key == Properties.TYPE_DOCLEN)
        {
            file    = Properties.DIR_OBJ + "/" + Properties.FILE_DOCLEN_TXT;
            typeMap = getDocLenMap();
        }

        fw = new FileWriter(file, true);
        for (Object entry : typeMap.keySet())
        {
            sb.append(entry.toString() + " " + typeMap.get(entry) + "\n");
        }
        fw.write(sb.toString());
        fw.close();
    }

    /**
     * Get the inverted document index.
     * @return
     *            The inverted document index.
     */
    public static HashMap<String, Long> getDocMap()
        throws IOException, ClassNotFoundException
    {
        ObjectInputStream in = new ObjectInputStream(
            new FileInputStream(Properties.DIR_OBJ + "/" + Properties.FILE_DOC_IDX));
        HashMap<String, Long> docMap = (HashMap<String, Long>) in.readObject();
        in.close();
        return docMap;
    }

    /**
     * Get the inverted term index.
     * @return
     *            The inverted term index.
     */
    public static HashMap<String, Long> getTermMap()
        throws IOException, ClassNotFoundException
    {
        ObjectInputStream in = new ObjectInputStream(
            new FileInputStream(Properties.DIR_OBJ + "/" + Properties.FILE_TERM_IDX));
        HashMap<String, Long> termMap = (HashMap<String, Long>) in.readObject();
        in.close();
        return termMap;
    }

    /**
     * Get the document length map.
     * @return
     *            The document length map.
     */
    public static HashMap<String, Integer> getDocLenMap()
        throws IOException, ClassNotFoundException
    {
        ObjectInputStream in = new ObjectInputStream(
            new FileInputStream(Properties.DIR_OBJ + "/" + Properties.FILE_DOCLEN_OBJ));
        HashMap<String, Integer> docLenMap = (HashMap<String, Integer>) in.readObject();
        in.close();
        return docLenMap;
    }

    /**
     * Get statistics for the corpus.
     * @return
     *            Statistics for the corpus.
     */
    public static HashMap<String, String> getStatistics()
        throws IOException
    {
        HashMap<String, String> statMap = new HashMap<String, String>();
        BufferedReader br = new BufferedReader(new FileReader(
            Properties.DIR_OBJ + "/" + Properties.FILE_STATS));
        String line = "";

        while((line = br.readLine()) != null)
        {
            statMap.put(line.split("\\s")[0], line.split("\\s")[1]);
        }

        br.close();
        return statMap;
    }

    /**
     * (1) Output time elapsed since last checkpoint.
     * (2) Return start time for this checkpoint.
     * @param startTime
     *            Start time for the current phase.
     * @param message
     *            User-friendly message.
     * @return
     *            Start time for the next phase.
     */
    public static long elapsedTime(long startTime, String message)
    {
        if(message != null)
        {
            Utils.cout(message + "\n");
        }
        long elapsedTime = (System.nanoTime() - startTime) / 1000000000;
        Utils.cout("Elapsed time: " + elapsedTime + " second(s)\n");
        return System.nanoTime();
    }

    /**
     * Main method for unit testing.
     * @param args
     *            Command-line arguments.
     */
    public static void main(String[] args)
    {
        /* Calculate start time */
        long startTime = System.nanoTime();

        Utils.cout("\n");
        Utils.cout("=========\n");
        Utils.cout("TOKENIZER\n");
        Utils.cout("=========\n");

        if(args.length < 2)
        {
            Utils.error("A minimum of 2 arguments are required.");
            Utils.echo("-stop=true/false -stem=true/false");
            System.exit(-1);
        }

        stopSwitch = args[0].equalsIgnoreCase("-stop=true");
        stemSwitch = args[1].equalsIgnoreCase("-stem=true");
        Utils.echo("Stop word removal has been set to " + stopSwitch);
        Utils.echo("Stemming has been set to " + stemSwitch);

        try
        {
            Tokenizer t = new Tokenizer();
            HashSet<String> stopSet = stopSwitch ? Utils.createStopSet() : new HashSet<String>();
            Utils.cout("\n>Creation of tuples\n");
            for (File file : new File("E:/Home/Repository/Java/IdeaProjects/A2_Indexing/input").listFiles())
            {
                Utils.echo("Processing file " + file.getName());
                t.tokenizeFile(file.getAbsolutePath(), stopSet);
            }
            // Utils.cout("\n>Sorting the tuples\n");
            // t.sortTuples();
            Utils.cout("\n>Writing all the objects to the file system\n\n");
            t.writeObjectsToFS();

            /* Output to console */
            HashMap<String, String> statMap = getStatistics();
            Utils.cout(">Corpus statistics\n");
            Utils.cout("Documents in corpus: " +
                       statMap.get(Properties.KEY_DOC_COUNT) + "\n");
            Utils.cout("Vocabulary size: " +
                       statMap.get(Properties.KEY_VOCAB_SIZE) + "\n");
            Utils.cout("Total length of all documents: " +
                       statMap.get(Properties.KEY_ALL_DOC_LEN) + "\n");
            Utils.cout("Average document length: " +
                       statMap.get(Properties.KEY_AVG_DOC_LEN) + "\n");
            Utils.cout("\n");
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
            elapsedTime(startTime, "Creation of tokens completed.");
        }
    }
}
/* End of Tokenizer.java */