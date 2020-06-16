package Client.OIO;

import Client.OIO.util.Data;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

//ChatApp Start
public class ClientThread2 {
    private String name = null;

    public ClientThread2(String nickName) {
        name = nickName;
    }

    public void startChat() {
        Socket serverSocket = new Socket();
        PrintWriter pw = null;
        try {
            serverSocket.connect(new InetSocketAddress(Data.IP, Data.PORT));
            System.out.println("채팅방에 입장하였습니다. " + serverSocket.getRemoteSocketAddress());

            pw = new PrintWriter(new OutputStreamWriter(serverSocket.getOutputStream(),
                    StandardCharsets.UTF_8)
                    , true);
            String request = "join:" + name + "\r"; //join의사를 서버에 전달
            //System.out.println("원문 : " + request);
            pw.println(request);

            GUI testGUI = new GUI(name, serverSocket);
            testGUI.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
