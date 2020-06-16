package Server.NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.logging.Logger;

//TODO 참고. https://gist.github.com/Botffy/3860641
public class NiochatServer implements Runnable {
    private final int PORT;
    private final String IP;

    private ServerSocketChannel serverSocketChannel = null;
    private Selector selector = null;

    private ByteBuffer buf = ByteBuffer.allocate(1024); //maximum data length

    EventManager eventManager = null;

    public NiochatServer(String IP, int PORT) throws IOException {
        this.PORT = PORT;
        this.IP = IP;

        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();

        serverSocketChannel.socket().bind(new InetSocketAddress(IP, PORT));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }


    @Override
    public void run() {
        try {
            Logger.getGlobal().info("현재 서버 정상 동작중.. " + IP + ":" + PORT);

            Iterator<SelectionKey> iter;
            SelectionKey key;
            while (!Thread.interrupted()) { //Event Loop Start
                selector.select();
                iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    key = iter.next();
                    iter.remove();

                    eventManager = new EventManager(selector, key);
                    if (key.interestOps() == SelectionKey.OP_ACCEPT) {
                        eventManager.handleAccept(key);
                    } else {
                        new Thread(eventManager).start();
                    }
                }
            }
        } catch (IOException e) {
            Logger.getGlobal().info("서버 생성 중 오류발생 " + IP + ":" + PORT);
            e.printStackTrace();
        }
    }
}
