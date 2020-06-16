package Server.NIO.util;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class GetTime {
    public String now() {
        long systemTime = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        String dTime = formatter.format(systemTime);
        return "["+dTime+"] ";
    }
}
