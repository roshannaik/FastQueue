package hwx.storm;

import java.util.concurrent.atomic.AtomicBoolean;

import hwx.Qu;

/**
 * Created by rnaik on 8/10/16.
 */

public class BoltExecutor<In,Out> extends Thread {
    private final Qu<In> qu;
    private final Bolt bolt;
    private final BoltOutputCollector<Out> oc;
    final AtomicBoolean stop = new AtomicBoolean(false);

    public BoltExecutor(Qu<In> qu, Bolt<In,Out> bolt, BoltOutputCollector<Out> oc) {
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
            In tuple = qu.remove();
            if(tuple!=null)
                bolt.execute(tuple, oc);
            else
                if(!Utils.nap(1))
                    break;
        }
    }
}
