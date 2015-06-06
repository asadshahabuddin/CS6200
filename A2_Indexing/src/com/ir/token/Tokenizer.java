package com.ir.token;

/* Import list */
import java.io.File;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author : Asad Shahabuddin
 * Created: Jun 5, 2015
 */

public class Tokenizer
{
    /* Static data members */
    private static long termId;
    private static long docId;
    private static HashSet<String> stopSet;
    private static HashMap<String, Long> termMap;
    private static HashMap<String, Long> docMap;
    private static Pattern pattern;
    private static Matcher matcher;
    private static int docCount;
    private static int allDocLength;
    private static int avgDocLength;

    private BufferedReader br;

    static
    {
        termId       = -1;
        docId        = -1;
        termMap      = new HashMap<String, Long>();
        docMap       = new HashMap<String, Long>();
        pattern      = Pattern.compile(Properties.REGEX_TOKEN);
        docCount     = 0;
        allDocLength = 0;
        avgDocLength = 0;
    }

    /**
     * Create a set of all stop words.
     * @throws IOException
     */
    public void createStopSet()
            throws IOException
    {
        stopSet = new HashSet<String>();
        br = new BufferedReader(new FileReader(Properties.FILE_STOPLIST));
        String line = "";

        while((line = br.readLine()) != null)
        {
            stopSet.add(line);
        }
    }

    /**
     * Tokenize a file and produce all tuples of the form
     * (term id, document id, position).
     * @param fileName
     *            File name.
     */
    public void tokenizeFile(String fileName)
        throws IOException
    {
        for(Map.Entry<String, String> entry : createDocInfoMap(fileName).entrySet())
        {
            allDocLength += tokenizeDocument(entry.getKey(), entry.getValue()).size();
            docCount++;
        }
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

                docInfoMap.put(docNo, text.toString());
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
    public ArrayList<Tuple> tokenizeDocument(String docNo, String text)
    {
        ArrayList<Tuple> tupleList = new ArrayList<Tuple>();
        matcher = pattern.matcher(text);
        /* Update the inverse document index */
        docMap.put(docNo, ++docId);


        while(matcher.find())
        {
            String term = matcher.group(0).toLowerCase();
            if(stopSet.contains(term))
            {
                continue;
            }

            if(!termMap.containsKey(term))
            {
                termMap.put(term, ++termId);
            }
            tupleList.add(new Tuple(termId, docId, matcher.start()));
        }

        return tupleList;
    }

    /**
     * Main method for unit testing.
     * @param args
     *            Command-line arguments.
     */
    public static void main(String[] args)
    {
        Tokenizer t = new Tokenizer();

        try
        {
            t.createStopSet();
            for (File file : new File("E:/Home/Repository/Java/IdeaProjects/A2_Indexing/input").listFiles())
            {
                t.tokenizeFile(file.getAbsolutePath());
            }
            avgDocLength = allDocLength / docCount;

            /* Output to console */
            System.out.println("Average document length is " + avgDocLength);
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}
/* End of Tokenizer.java */