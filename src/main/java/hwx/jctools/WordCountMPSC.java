package hwx.jctools;

import org.jctools.queues.MpscArrayQueue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

import hwx.Timer;
import hwx.storm.Utils;


public class WordCountMPSC {

    public static void main(String[] args)  throws InterruptedException {
        String file = "/Users/rnaik/tmp/book3.txt";
        MpscArrayQueue<String> q1 = new MpscArrayQueue<>(1024*8);

        LocalGrouping<String> lg1 = new LocalGrouping<>(q1);
        SpoutOutputCollector<String> soc = new SpoutOutputCollector<>(lg1);
        FileReaderSpout spout = new FileReaderSpout(soc, file);

        MpscArrayQueue<String> q2 = new MpscArrayQueue<>(1024*8);
        LocalGrouping<String> lg2 = new LocalGrouping<>(q2);
        BoltOutputCollector<String> boc1 = new BoltOutputCollector<>(lg2);
        SplitSentenceBolt splitterBolt = new SplitSentenceBolt();

        SpoutExecutor spoutExecutor = new SpoutExecutor(spout);
        BoltExecutor<String, String> boltExecutor = new BoltExecutor<>(q1, splitterBolt, boc1);


        CounterBolt counterBolt = new CounterBolt();
        NoOpGrouping ng = new NoOpGrouping();
        BoltOutputCollector<None> boc2 = new BoltOutputCollector<>(ng);

        BoltExecutor<String, None> boltExecutor2 = new BoltExecutor<>(q2, counterBolt, boc2);

        spoutExecutor.start();
        boltExecutor.start();
        boltExecutor2.start();

        spoutExecutor.join();
        Thread.sleep(5000);
        boltExecutor.shutdown();
        boltExecutor2.shutdown();

        boltExecutor.join();
        boltExecutor2.join();


        spout.stop();
        splitterBolt.stop();
        counterBolt.stop();
    }

    interface Grouper<T> {
        /** returns the destination id */
        MpscArrayQueue<T> getDestination(T item);
    }


    static class LocalGrouping<T> implements Grouper<T> {

        MpscArrayQueue<T> localDestination; /** recipient Q */

        public LocalGrouping(MpscArrayQueue<T> localDestination) {
            this.localDestination = localDestination;
        }

        public MpscArrayQueue<T> getDestination(T item) {
            return localDestination;
        }
    }

    static class NoOpGrouping implements Grouper<None> {
        @Override
        public MpscArrayQueue<None> getDestination(None item) {
            return null;
        }
    }

    static class SpoutOutputCollector<T> {
        Grouper g;
        public SpoutOutputCollector(Grouper<T> g) {
            this.g = g;
        }

        void emit(T tuple, long anchor) {
            MpscArrayQueue<T> q = g.getDestination(tuple);
            while ( !q.offer(tuple) ) {
                Utils.nap(1);
            }
        }

    }

    static class BoltOutputCollector<T> {

        private final Grouper<T> grouper;

        public BoltOutputCollector(Grouper<T> grouper) {
            this.grouper = grouper;
        }

        void emit(T tuple) {
            MpscArrayQueue<T> q = grouper.getDestination(tuple);
            while ( !q.offer(tuple) ) {
                Utils.nap(1);
            }
        }
    }

    interface Spout {
        boolean nextTuple(); /** returns true if work completed */
        void stop();
    }


    interface Bolt<In, Out> {
        void execute(In input, BoltOutputCollector<Out> collector);
        void stop();
    }


    static class FileReaderSpout implements Spout {

        private SpoutOutputCollector collector;
        private BufferedReader reader;
        private File file;
        private long count = 0;
        private boolean done = false;

        public FileReaderSpout(SpoutOutputCollector collector, String file) {
            this.collector = collector;
            this.file = new File(file);

            try {
                this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.file)));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        // returns true if done
        public boolean nextTuple() {
            try {
                if(count==0) {
                    System.err.println("Starting : " + Utils.now());
                }
                String line = reader.readLine();
                if(line==null) {
                    if(!done) {
                        done = true;
                    }
                    return true;
                }
                collector.emit(line, count);
                count++;
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Could not read file", e);
            }
        }

        @Override
        public void stop() {
            System.err.format("Total sentences  %,d\n", count);
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    } // class FileReaderSpout


    static class SplitSentenceBolt implements Bolt<String, String> {
        int emitCount = 0;
        public void execute(String sentence, BoltOutputCollector<String> collector) {
            for (String word : splitSentence(sentence)) {
                collector.emit(word);
                ++emitCount;
            }
        }

        @Override
        public void stop() {
            System.err.format("Words Emitted %,d\n", emitCount);
        }

        public static String[] splitSentence(String sentence) {
            if (sentence != null) {
                return sentence.split("\\s+");
            }
            return null;
        }
    } // class SplitSentenceBolt


    static class None {
    }

    static class CounterBolt implements Bolt<String,None> {
        HashMap<String,LongAdder> wordCounts = new HashMap<>(20_000);
        int max = 16_254_700;
        long wordCount =0;
        @Override
        public void execute(String word, BoltOutputCollector<None> collector) {
            LongAdder la = wordCounts.get(word);
            if( la == null ) {
                LongAdder count = new LongAdder();
                count.increment();
                wordCounts.put(word, count);
            } else {
                wordCounts.get(word).increment();
            }
            ++wordCount;
            if(wordCount==max-1) {
                System.err.println("Done Time = " + Utils.now());
            }
        }


        @Override
        public void stop() {
            System.err.format("Unique words : %,d\n", wordCounts.keySet().size());
            System.err.format("Total words : %,d\n", wordCount);
        }
    } // class CounterBolt
    static public class BoltExecutor<In,Out> extends Thread {
        private final MpscArrayQueue<In> qu;
        private final Bolt bolt;
        private final BoltOutputCollector<Out> oc;
        final AtomicBoolean stop = new AtomicBoolean(false);

        public BoltExecutor(MpscArrayQueue<In> qu, Bolt<In,Out> bolt, BoltOutputCollector<Out> oc) {
            this.qu = qu;
            this.bolt = bolt;
            this.oc = oc;
        }

        public void shutdown() {
            stop.compareAndSet(false,true);
//        System.err.println("shutting down");
        }

        @Override
        public void run() {
            while(! stop.get() ) {
                In tuple = qu.poll();
                if(tuple!=null)
                    bolt.execute(tuple, oc);
                else
                if(!Utils.nap(1))
                    break;
            }
        }
    }

    static class SpoutExecutor extends Thread {
        private final Spout spout;
        private final AtomicBoolean stop = new AtomicBoolean(false);

        public SpoutExecutor(Spout spout) {
            this.spout = spout;
        }


        @Override
        public void run() {
            Timer t1 = new Timer("Spout");
            int count=0;
            while( ! stop.get() ) {
                if( ! spout.nextTuple() ) {
                    ++count;
                }  else {
                    t1.stop(count);
                    return;
                }

            }
        }
    }
}




