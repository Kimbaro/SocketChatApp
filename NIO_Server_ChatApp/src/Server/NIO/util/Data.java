package Server.NIO.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class Data {
    public static String IP;

    static {

        try {
            InetAddress[] local = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
            for (int i = 0; i < local.length; i++) {
                System.out.println("[" + i + "]:" + local[i].getHostAddress());
                System.out.println("[" + i + "]:" + local[i].getHostName());
            }

            IP = local[1].getHostAddress(); //배열 위치번호만 변경 요함.

        } catch (UnknownHostException e) {
            Logger.getGlobal().warning("검색된 자신의 정확한 InetAddress 배열을 대입해주세요.");
        }
    }

    public static final int PORT = 5000;

}
