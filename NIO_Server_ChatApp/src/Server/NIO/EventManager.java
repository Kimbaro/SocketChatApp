package Server.NIO;

import Server.NIO.util.AES256Cipher;
import Server.NIO.util.ClientOption;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.logging.Logger;

public class EventManager implements Runnable {
    Selector selector = null;
    SelectionKey key = null;
    private ByteBuffer buf = ByteBuffer.allocate(1024);

    public EventManager(Selector selector, SelectionKey key) {
        this.selector = selector;
        this.key = key;
    }

    private EventManager() {
    }

    @Override
    public void run() {
        try {
            switch (key.interestOps()) {
                case SelectionKey.OP_READ:
                    key.interestOps(0); //-> 하고 안하고의 처리 과정을 파악할것.
                    handleRead(key);
                    break;
                case SelectionKey.OP_WRITE:
                    key.interestOps(0);
                    handleWrite(key);
                    break;
                case SelectionKey.OP_CONNECT:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleAccept(SelectionKey key) throws IOException {
        Logger.getGlobal().info("handleAccept()");
        SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
        String address =
                (new StringBuilder(sc.socket().getInetAddress().toString())).append(":").append(sc.socket().getPort()).toString();
        System.out.println("address : " + address);
        sc.configureBlocking(false);
//        sc.register(selector, SelectionKey.OP_READ, address);
//        sc.write(welcomeBuf); test0
        //kimbaro -- test 0
//        sc.write(welcomeBuf);
//        welcomeBuf.rewind();
        SelectionKey newClient = sc.register(selector, SelectionKey.OP_READ, address);
        System.out.println("accepted connection from: " + address);
    }

    private void handleRead(SelectionKey key) throws IOException {
        Logger.getGlobal().info("handleRead()");
        SocketChannel ch = (SocketChannel) key.channel();
        StringBuilder sb = new StringBuilder();

        int read = 0;
        if ((read = ch.read(buf)) > 0) {
            buf.flip();
            byte[] bytes = new byte[read];
            byte[] bytes_bottle = buf.array();
            System.arraycopy(bytes_bottle, 0, bytes, 0, read);
            //buf.get(bytes);
            sb.append(new String(bytes));
            buf.clear();
        }
        String msg;
        if (read < 0) {
            msg = "exit:";
            ch.close();
        } else {
            msg = sb.toString();
        }
        strAPI(msg, key); //메시지 명령어 처리
        //System.out.println(msg); kimbaro
        //broadcast(msg); kimbaro
    }

    private void handleWrite(SelectionKey key) {
        Logger.getGlobal().info("handleWrite()");
        ClientOption option = (ClientOption) key.attachment();

        if (option.getPartner_key() == null) { //1:N대화
            if (option.isNewClient()) { //신규 여부 체크 true=신규, false=기존
                option.setNewClient(false);
                System.out.println("신규사용자 접속 정보 : " + option.getMy_key().channel() + "@" + option.getNickname());

                // 접속자 명단 포멧
                StringBuffer sb = new StringBuffer();
                Iterator<SelectionKey> iterator = selector.keys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey userKey = iterator.next();
                    if (userKey.isValid() && userKey.channel() instanceof SocketChannel) {
                        ClientOption clientOption = (ClientOption) userKey.attachment();
                        sb.append(clientOption.getNickname());
                        if (iterator.hasNext()) {
                            sb.append(",");
                        }
                    }
                }

                String welcome_comment = "join:" + option.getNickname() + ":" + sb.toString() + "\r";
                broadcast(welcome_comment);
            } else {
                broadcast(option.getData().toString());
            }
        } else {//1:1 대화
            try {
                unicast(option.getData().toString(), option.getPartner_key());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //1:1대화 후 1:N대화상태로 초기화
                option.setPartner_name(null);
                option.setPartner_key(null);
            }
        }
        key.interestOps(SelectionKey.OP_READ);
        selector.wakeup();
        Logger.getGlobal().info("write작업 정상종료 read작업 전환. ");
    }

    private void broadcast(String msg) {
        Logger.getGlobal().info("broadcast()");
        ByteBuffer msgBuf = ByteBuffer.wrap(msg.getBytes());
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                SocketChannel sch = (SocketChannel) key.channel();
                try {
                    sch.write(msgBuf);
                } catch (IOException e) {
                    try {
                        sch.shutdownOutput();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                msgBuf.rewind();
            }
        }
    }

    private void unicast(String msg, SelectionKey partnerKey) throws IOException {
        Logger.getGlobal().info("unicast()");
        ByteBuffer msgBuf = ByteBuffer.wrap(msg.getBytes());
        SelectionKey[] whisperUsers = {this.key, partnerKey};
        for (SelectionKey key : whisperUsers) {
            SocketChannel sch = (SocketChannel) key.channel();
            sch.write(msgBuf);
            msgBuf.rewind();
        }
    }

    //API
    public void strAPI(String data, SelectionKey clientKey) {
        Logger.getGlobal().info("strAPI() -> " + data);
        String value = data.split(":")[0];
        String nickname, message;

        if (value.equals("join")) { //신규유저참가
            nickname = data.split(":")[1].replaceAll("(\r|\n|\r\n|\n\r)", "");
            clientKey.attach(new ClientOption(nickname, clientKey));

            clientKey.interestOps(SelectionKey.OP_WRITE);
            selector.wakeup();
        } else if (value.equals("message")) { //일반메시지
            message = data.split(":")[1];
            ClientOption option = (ClientOption) clientKey.attachment();
            option.setData(new StringBuffer("message:" + option.getNickname() + " -> " + message));

//            Logger.getGlobal().info(message);
            clientKey.interestOps(SelectionKey.OP_WRITE);
            selector.wakeup();
        } else if (value.equals("exit")) { //채팅방 퇴장
            ClientOption option = (ClientOption) key.attachment();
            option.setExit(true); //기본 값 false
            option.setData(new StringBuffer("exit:" + option.getNickname()));
            try {
                key.channel().close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            broadcast(option.getData().toString() + "\r");
            selector.wakeup();
            // null
        } else if (value.equals("whisper")) { //1:1 대화
            //case2
            ClientOption myOption = (ClientOption) key.attachment();
            String partner_Nickname = data.split(":")[1]; //receive data
            message = data.split(":")[2].replaceAll("(\r|\n|\r\n|\n\r)", "");
            for (SelectionKey searchKey : selector.keys()) {
                if (searchKey.isValid() && searchKey.channel() instanceof SocketChannel) {
                    ClientOption partner_Option = (ClientOption) searchKey.attachment();
                    if (partner_Nickname.equals(partner_Option.getNickname())) {
                        //검증된 파트너 옵션인 경우 진입.
                        //파트너 정보 업데이트
                        myOption.setPartner_name(partner_Nickname);
                        myOption.setPartner_key(searchKey);
                        myOption.setData(new StringBuffer("whisper:" + myOption.getNickname() + "(귓속말로) ->" + message +
                                "\r"));

                        //key.attach(myOption); //변경정보 저장.
                        clientKey.interestOps(SelectionKey.OP_WRITE);
                        selector.wakeup();
                        break;
                    }
                }
            }
        } else if (value.equals("envelope")) { //암호화전송

            AES256Cipher aes256Cipher = AES256Cipher.getInstance();
            AES256Cipher.mainKey = "abcdefghijklmnopqrstuvwxyz123456"; //비밀키 복호화키

            data = data.split(":")[1];

            String key2 = data.split("@")[0];
            String data2 = data.split("@")[1];
//            System.out.println(key2); //mainKey에 의해 암호화된 상대방 비밀키 추출
//            System.out.println(data2); //상대방 비밀키에 의해 암호화된 문자 추출

            try {
                key2 = aes256Cipher.AES_Decode(key2, AES256Cipher.mainKey); //상대방 비밀키 복호화
                data2 = aes256Cipher.AES_Decode(data2, key2); //암호화된 문자를 key2비밀키로 복호화
//                System.out.println(key2);
//                System.out.println(data2);
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            } catch (NoSuchAlgorithmException ex) {
                ex.printStackTrace();
            } catch (NoSuchPaddingException ex) {
                ex.printStackTrace();
            } catch (InvalidKeyException ex) {
                ex.printStackTrace();
            } catch (InvalidAlgorithmParameterException ex) {
                ex.printStackTrace();
            } catch (IllegalBlockSizeException ex) {
                ex.printStackTrace();
            } catch (BadPaddingException ex) {
                ex.printStackTrace();
            }
            strAPI(data2, key); //복호화된 데이터 재귀

        } else { //default
            ClientOption option = (ClientOption) clientKey.attachment();
            option.setData(new StringBuffer("message:" + option.getNickname() + " -> " + data + "\r"));

//            Logger.getGlobal().info(message);
            clientKey.interestOps(SelectionKey.OP_WRITE);
            selector.wakeup();
            Logger.getGlobal().info("unknown API : " + data);
        }
    }
}
