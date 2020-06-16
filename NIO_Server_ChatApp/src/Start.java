import Server.NIO.NiochatServer;
import Server.NIO.util.Data;

import java.io.IOException;

public class Start {
    //Start Server Chat
    public static void main(String[] args) throws IOException {
        NiochatServer server = new NiochatServer(Data.IP, Data.PORT);
        (new Thread(server)).start();
    }
}
