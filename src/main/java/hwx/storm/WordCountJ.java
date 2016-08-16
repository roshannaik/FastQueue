package hwx.storm;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

import hwx.Timer;

/**
 * Simple word frequency counting in regular Java
 */
public class WordCountJ {
    public static void main(String[] args) throws  Exception {
        String file = "/Users/rnaik/tmp/book3.txt";
        Timer t1 = new Timer("java line count");
        Timer t2 = new Timer("java word count");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        int lineCount=0, wordCount =0;
        String line = reader.readLine();
        HashMap<String,LongAdder> counts = new HashMap<>(34_000);
        while(line!=null) {
            ++lineCount;
            String[] words = splitSentence(line);
            for (String word : words) {
                ++wordCount;
                LongAdder count = counts.get(word);
                if( count!=null) {
                    count.increment();
                } else {
                    count = new LongAdder(); // 0
                    count.increment(); //1
                    counts.put(word, count);
                }
            }
            line = reader.readLine();
        } // while

        t1.stop(lineCount);
        t2.stop(wordCount);
    }

    public static String[] splitSentence(String sentence) {
        if (sentence != null) {
            return sentence.split("\\s+");
        }
        return null;
    }
}
