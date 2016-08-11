package hwx.storm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by rnaik on 8/10/16.
 */
public class Utils {
    public static boolean nap(long duration) {
        try {
            Thread.sleep(duration);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    public static String now() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSSS");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
