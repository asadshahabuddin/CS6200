package com.ir.eval;

/* Import list */
import java.io.File;
import java.util.TreeMap;
import java.util.HashMap;
import com.ir.global.Utils;
import java.io.IOException;
import java.io.FileInputStream;
import java.text.DecimalFormat;

/**
 * Author : Asad Shahabuddin
 * Created: Jul 16, 2015
 */

public class TrecEval
{
    public void output(int qid, int ret, int rel, int relRet,
                       double[] apar, double map, double[] apac, double rp)
    {
        Utils.cout("\n");
        Utils.cout("Queryid (Num):    " + qid + "\n");
        Utils.cout("Total number of documents over all queries\n");
        Utils.cout("    Retrieved:    " + ret + "\n");
        Utils.cout("    Relevant:     " + rel + "\n");
        Utils.cout("    Rel_ret:      " + relRet + "\n");
        Utils.cout("Interpolated Recall - Precision Averages:\n");
        Utils.cout("    at 0.00       " + new DecimalFormat("#0.0000").format(apar[0]) + "\n");
        Utils.cout("    at 0.10       " + new DecimalFormat("#0.0000").format(apar[1]) + "\n");
        Utils.cout("    at 0.20       " + new DecimalFormat("#0.0000").format(apar[2]) + "\n");
        Utils.cout("    at 0.30       " + new DecimalFormat("#0.0000").format(apar[3]) + "\n");
        Utils.cout("    at 0.40       " + new DecimalFormat("#0.0000").format(apar[4]) + "\n");
        Utils.cout("    at 0.50       " + new DecimalFormat("#0.0000").format(apar[5]) + "\n");
        Utils.cout("    at 0.60       " + new DecimalFormat("#0.0000").format(apar[6]) + "\n");
        Utils.cout("    at 0.70       " + new DecimalFormat("#0.0000").format(apar[7]) + "\n");
        Utils.cout("    at 0.80       " + new DecimalFormat("#0.0000").format(apar[8]) + "\n");
        Utils.cout("    at 0.90       " + new DecimalFormat("#0.0000").format(apar[9]) + "\n");
        Utils.cout("    at 1.00       " + new DecimalFormat("#0.0000").format(apar[10]) + "\n");
        Utils.cout("Average precision (non-interpolated) for all rel docs (averaged over queries)\n");
        Utils.cout("                  " + new DecimalFormat("#0.0000").format(map) + "\n");
        Utils.cout("Precision:\n");
        Utils.cout("  At    5 docs:   " + new DecimalFormat("#0.0000").format(apac[0]) + "\n");
        Utils.cout("  At   10 docs:   " + new DecimalFormat("#0.0000").format(apac[1]) + "\n");
        Utils.cout("  At   15 docs:   " + new DecimalFormat("#0.0000").format(apac[2]) + "\n");
        Utils.cout("  At   20 docs:   " + new DecimalFormat("#0.0000").format(apac[3]) + "\n");
        Utils.cout("  At   30 docs:   " + new DecimalFormat("#0.0000").format(apac[4]) + "\n");
        Utils.cout("  At  100 docs:   " + new DecimalFormat("#0.0000").format(apac[5]) + "\n");
        Utils.cout("  At  200 docs:   " + new DecimalFormat("#0.0000").format(apac[6]) + "\n");
        Utils.cout("  At  500 docs:   " + new DecimalFormat("#0.0000").format(apac[7]) + "\n");
        Utils.cout("  At 1000 docs:   " + new DecimalFormat("#0.0000").format(apac[8]) + "\n");
        Utils.cout("R-Precision (precision after R (= num_rel for a query) docs retrieved:\n");
        Utils.cout("    Exact:        " + new DecimalFormat("#0.0000").format(rp) + "\n");
    }

    public void evaluate(String[] args)
        throws IOException
    {
        /*
        (1) Get the names of 'qrel' and 'trec' files.
        (2) Check for '-q' option.
        */
        File qrelFile;
        File trecFile;
        boolean printAllQueries = false;
        if(args.length == 3)
        {
            qrelFile = new File(args[1]);
            trecFile = new File(args[2]);
            printAllQueries = true;
        }
        else
        {
            qrelFile = new File(args[0]);
            trecFile = new File(args[1]);
        }

        /* Process the 'qrel' file first. */
        if(!qrelFile.exists())
        {
            Utils.error("'qrel' file does not exist");
            System.exit(-1);
        }
        FileInputStream fin = new FileInputStream(qrelFile);
        byte[] dataB = new byte[(int) qrelFile.length()];
        fin.read(dataB);
        fin.close();
        String[] data = new String(dataB, "UTF-8").split("\\s|\\n");

        /*
        Now, take the values from the data array (four at a time) and put them in a
        data structure.  Here's how it will work.
        (1) qrel is a hash whose keys are Topic IDs and whose values are references to hashes.
        Each referenced hash has keys which are Doc IDs and values which are relevance values.
        In other words...
            (a) qrel				      The qrel hash.
            (b) qrel{topic}			      Reference to a hash for $topic.
            (c) qrel{$topic}->{$doc_id}	  The relevance of $doc_id in $topic.
            (d) numrel			          Hash whose values are (expected) number
                                          of docs relevant for each topic.
        */
        HashMap<String, HashMap<String, Double>> qrel = new HashMap<>();
        HashMap<String, Double> numrel = new HashMap<>();
        for(int i = 0; i < data.length; i += 4)
        {
            if(!qrel.containsKey(data[i]))
            {
                qrel.put(data[i], new HashMap<String, Double>());
                numrel.put(data[i], 0D);
            }
            qrel.get(data[i]).put(data[i + 2], Double.valueOf(data[i + 3]));
            numrel.put(data[i], numrel.get(data[i]) + Double.valueOf(data[i + 3]));
        }

        /* The following code snippet tests the above data structure. */
        /*
        for(String topicId : qrel.keySet())
        {
            for(String docId : qrel.get(topicId).keySet())
            {
                Utils.cout(topicId + " " + docId + " " + qrel.get(topicId).get(docId) + "\n");
            }
        }
        */

        /* Process the 'trec' file. */
        if(!trecFile.exists())
        {
            Utils.error("'qrel' file does not exist");
            System.exit(-1);
        }
        fin = new FileInputStream(trecFile);
        dataB = new byte[(int) trecFile.length()];
        fin.read(dataB);
        fin.close();
        data = new String(dataB, "UTF-8").split("\\s|\\n");

        /* Process the trec_file data in much the same manner as above. */
        TreeMap<String, HashMap<String, Double>> trec = new TreeMap<>();
        for(int i = 0; i < data.length; i += 6)
        {
            if(!trec.containsKey(data[i]))
            {
                trec.put(data[i], new HashMap<String, Double>());
            }
            trec.get(data[i]).put(data[i + 2], Double.valueOf(data[i + 4]));
        }

        /* The following code snippet tests the above data structure. */
        /*
        for(String topicId : trec.keySet())
        {
            for(String docId : trec.get(topicId).keySet())
            {
                Utils.cout(topicId + " " + docId + " " + trec.get(topicId).get(docId) + "\n");
            }
        }
        */

        /* Initialize some arrays. */
        double[] recalls = new double[] {0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
        int[] cutoffs    = new int[] {5, 10, 15, 20, 30, 100, 200, 500, 1000};
        int numTopics = 0;
        int totNumRet = 0;
        double totNumRel = 0;
        double totNumRelRet = 0;
        double sumAvgPrec = 0;
        double sumRPrec = 0;
        double[] sumPrecAtCutoffs = new double[cutoffs.length];
        double[] sumPrecAtRecalls = new double[recalls.length];

        /* Now, let's process the data from trec_file to get results. */
        for(String topicId : trec.keySet())
        {
            if(numrel.containsKey(topicId))
            {
                /* Processing another topic. */
                numTopics++;
                /* Hash pointer. */
                HashMap<String, Double> href = trec.get(topicId);
                /* Precision list. */
                double[] precList = new double[1001];
                /* Recall list. */
                double[] recList = new double[1001];
                /* Initialize the number retrieved. */
                int numRet = 0;
                /* Initialize the number relevant retrieved. */
                double numRelRet = 0;
                /* Initialize sum precision. */
                double sumPrec = 0;
                /* Initialize the priority queue. */
                Queue q = new Queue();

                /*
                Now, sort Document IDs based on scores and calculate stats.
                Note 1: Break score ties lexicographically based on doc IDs.
                Note 2: Explicitly quit after 1000 docs to conform to TREC while still
                        handling trec_files with possibly more docs.
                */
                for(String docId : href.keySet())
                {
                    q.add(new NodeScorePair(docId, href.get(docId)));
                }
                NodeScorePair nsp;
                while((nsp = q.remove()) != null)
                {
                    numRet++;
                    if(qrel.get(topicId).containsKey(nsp.getNode()))
                    {
                        sumPrec += qrel.get(topicId).get(nsp.getNode()) * (1 + numRelRet) / numRet;
                        numRelRet += qrel.get(topicId).get(nsp.getNode());
                    }
                    precList[numRet] = numRelRet / numRet;
                    recList[numRet] = numRelRet / numrel.get(topicId);
                    if(numRet >= 1000)
                    {
                        break;
                    }
                }
                double avgPrec = sumPrec / numrel.get(topicId);

                /* If necessary, fill out the remainder of the precision and recall lists. */
                for(int i = numRet + 1; i <= 1000; i++)
                {
                    precList[i] = numRelRet / i;
                    recList[i] = numRelRet / numrel.get(topicId);
                }

                /*
                Calculate precision at document cutoff levels and R-precision.
                Note that arrays are indexed starting at 0.
                */
                double[] precAtCutoffs = new double[cutoffs.length];
                int i = 0;
                for(int cutoff : cutoffs)
                {
                    precAtCutoffs[i++] = precList[cutoff];
                }

                /*
                Calculate R-precision.  We'll be a bit anal here and actually interpolate
                if the number of relevant docs is not an integer.
                */
                double rPrec;
                if(numrel.get(topicId) > numRet)
                {
                    rPrec = numRelRet / numrel.get(topicId);
                }
                else
                {
                    /* Integer part. */
                    int intNumRel = (int) numrel.get(topicId).doubleValue();
                    double fracNumRel = numrel.get(topicId) - intNumRel;
                    rPrec = (fracNumRel > 0) ?
                            (1 - fracNumRel) * precList[intNumRel] +
                            fracNumRel * precList[intNumRel + 1] :
                            precList[intNumRel];
                }

                /* Calculate interpolated precision. */
                double maxPrec = 0D;
                for(i = 1000; i >= 1; i--)
                {
                    if(precList[i] > maxPrec)
                    {
                        maxPrec = precList[i];
                    }
                    else
                    {
                        precList[i] = maxPrec;
                    }
                }

                /* Calculate the precision at recall levels. */
                double[] precAtRecalls = new double[recalls.length];
                i = 0;
                int j = 1;
                for(double recall : recalls)
                {
                    while(j <= 1000 && recList[j] < recall)
                    {
                        j++;
                    }
                    if(j < 1000)
                    {
                        precAtRecalls[i++] = precList[j];
                    }
                    else
                    {
                        precAtRecalls[i++] = 0;
                    }
                }

                /* If requested, print statistics on a per-query basis. */
                if(printAllQueries)
                {
                    output(Integer.valueOf(topicId), numRet, (int) numrel.get(topicId).doubleValue(),
                          (int) numRelRet, precAtRecalls, avgPrec, precAtCutoffs, rPrec);
                }

                /* Update running sums for overall statistics. */
                totNumRet    += numRet;
                totNumRel    += numrel.get(topicId);
                totNumRelRet += numRelRet;

                for(i = 0; i < cutoffs.length; i++)
                {
                    sumPrecAtCutoffs[i] += precAtCutoffs[i];
                }
                for(i = 0; i < recalls.length; i++)
                {
                    sumPrecAtRecalls[i] += precAtRecalls[i];
                }

                sumAvgPrec += avgPrec;
                sumRPrec   += rPrec;
            }
        }

        /* Calculate summary statistics. */
        double[] avgPrecAtCutoffs = new double[cutoffs.length];
        double[] avgPrecAtRecalls = new double[recalls.length];
        for(int i = 0; i < cutoffs.length; i++)
        {
            avgPrecAtCutoffs[i] = sumPrecAtCutoffs[i] / numTopics;
        }
        for(int i = 0; i < recalls.length; i++)
        {
            avgPrecAtRecalls[i] = sumPrecAtRecalls[i] / numTopics;
        }
        double meanAvgPrec = sumAvgPrec / numTopics;
        double avgRPrec    = sumRPrec   / numTopics;

        output(numTopics, totNumRet, (int) totNumRel, (int) totNumRelRet,
               avgPrecAtRecalls, meanAvgPrec, avgPrecAtCutoffs, avgRPrec);
    }

    /**
     * Main method for unit testing.
     * @param args
     *            Program arguments.
     */
    public static void main(String[] args)
    {
        if(args.length < 2 || args.length > 3)
        {
            Utils.error("Incorrect number of arguments");
            Utils.cout("USAGE  : TrecEval [-q] <qrel_file> <trec_file>\n");
            Utils.cout("EXAMPLE: TrecEval qrels.txt trec.txt");
            System.exit(-1);
        }

        try
        {
            new TrecEval().evaluate(args);
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}
/* End of TrecEval.java */