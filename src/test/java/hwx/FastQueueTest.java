package hwx;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class FastQueueTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public FastQueueTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( FastQueueTest.class );
    }

    /**
     * todo: put assertions
     */
    public static void testBasicCorrectness() {
        FastQueue fq = new FastQueue(2);
        System.err.println(fq.remove());   // fail { }
        fq.print();
        System.err.println(fq.insert(1)); // {1}
        fq.print();
        System.err.println(fq.insert(2)); // {1 2}
        fq.print();
        System.err.println(fq.insert(3)); // fail {1 2}
        fq.print();

        System.err.println(fq.remove());  // {2}
        fq.print();
        System.err.println(fq.remove());  // {}
        fq.print();
        System.err.println(fq.remove());  // fail {}
        fq.print();
        System.err.println(fq.insert(4)); // {4}
        fq.print();
        System.err.println(fq.insert(5)); // {4 5}
        fq.print();

        System.err.println(fq.remove());  // {5}
        fq.print();

        System.err.println(fq.insert(6)); // {5 6}
        fq.print();
        System.err.println(fq.insert(7)); // fail {7}
        fq.print();
    }}
