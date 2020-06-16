package Client.OIO;

import Client.OIO.util.AES256Cipher;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class GUI extends JFrame {
    private JPanel panel1;
    private JButton envelope;
    private JButton msgSend;
    private JTextArea textArea1;
    private JTextField chatInput;
    private JTable table1;

    //socket init
    private String myName;
    private Socket serverSocket;

    public GUI(String myName, Socket serverSocket) {
        super("Kimbaro's Chat");
        this.setSize(1000, 400);
        this.setLocation(100, 100);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panel1);
        this.pack();

        //socket ready
        this.serverSocket = serverSocket;
        this.myName = myName;
        new ChatClientReceiveThread(serverSocket).start(); //read thread

        envelope.addActionListener(new ActionListener() { //암호화버튼 push event
            @Override
            public void actionPerformed(ActionEvent e) {
                AES256Cipher aes256Cipher = AES256Cipher.getInstance();
                String key = aes256Cipher.createKey(); //유저 키
                JOptionPane.showInputDialog(panel1,
                        "생성된 암호키 입니다.", key);

                String data = chatInput.getText();

                try {
                    //키 암호화
                    key = aes256Cipher.AES_Encode(key, AES256Cipher.mainKey);
                    //데이터 암호화
                    data = aes256Cipher.AES_Encode(data); //암호화
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
                } finally {
                    data = "envelope:" + key + "@" + data;  //비밀키 @ 채팅창데이터 서버에 보낼 데이터
                    System.out.println(data);
                }
                chatInput.setText(data);
                chatInput.requestFocus();
            }
        });

        msgSend.addActionListener(new ActionListener() { //메시지 보내기 버튼 리스너
            @Override
            public void actionPerformed(ActionEvent e) {
                new ChatClientSendMessageThread().sendMessage(chatInput.getText());
            }
        });
        chatInput.addKeyListener(new KeyAdapter() { //채팅창 리스너
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);

                char key = e.getKeyChar();
                if (key == KeyEvent.VK_ENTER) {
                    new ChatClientSendMessageThread().sendMessage(chatInput.getText());
                    //event ..
                }
            }
        });
    }

    public static DefaultTableModel defaultTableModel = new DefaultTableModel(null, new String[]{"접속자"}) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    //table 생성
    private void createUIComponents() { //침기지목록 테이블 클릭 시
        // TODO: place custom component creation code here
        table1 = new JTable(defaultTableModel);
        table1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int row = table1.rowAtPoint(e.getPoint());
                int col = table1.columnAtPoint(e.getPoint());
                if (row >= 0 && col >= 0) {
                    String data = (String) defaultTableModel.getValueAt(row, col);
                    chatInput.setText("whisper:" + data + ":");
                    //JOptionPane.showMessageDialog(table1, "Column header #" + data + " is clicked");
                }
            }
        });
    }

    //table 컬럼 추가
    public void addTableColumn(String userList) {
        for (int x = 0; x < defaultTableModel.getRowCount(); x++) {
            defaultTableModel.removeRow(x);
        }

        StringTokenizer tokenizer = new StringTokenizer(userList, ",");
        while (tokenizer.hasMoreTokens()) {
            defaultTableModel.addRow(new Object[]{tokenizer.nextToken()});
        }
    }

    //table 컬럼 제거
    public void removeTableColumn(String nickname) {
        for (int x = 0; x < defaultTableModel.getRowCount(); x++) {
            if (nickname.equals(defaultTableModel.getValueAt(x, 0))) {
                defaultTableModel.removeRow(x);
            }
        }
    }

    //1:1채팅여부 체크
    public boolean whisperCheck(String data) {
        try {
            String value = data.split(":")[0];
            if (value.equals("whisper")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    //암호화여부 체크
    public boolean envelopeCheck(String data) {
        try {
            String value = data.split(":")[0];
            if (value.equals("envelope")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    //receive event thread
    private class ChatClientReceiveThread extends Thread {
        Socket socket = null;

        ChatClientReceiveThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                        StandardCharsets.UTF_8));
                while (true) {
                    String msg = br.readLine();
                    msg = strAPI(msg);
                    if (msg != null) {
                        textArea1.append(msg);
                        textArea1.append("\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //서버 수신 데이터 처리
        private String strAPI(String data) {
            String value = data.split(":")[0];
            String nickname, message;
            Logger.getGlobal().info("strAPI -> " + data);
            if (value.equals("join")) {
                nickname = data.split(":")[1];
                message = data.split(":")[2];
                System.out.println("채팅방 참가 명단 : " + message);
                addTableColumn(message);
                String welcome_comment = "관리자 : 환영합니다! " + nickname;
                return welcome_comment;
                //Logger.getGlobal().info(nickname);
            } else if (value.equals("exit")) {
                Logger.getGlobal().info(data);
                nickname = data.split(":")[1];
                removeTableColumn(nickname);
                String exit_comment = "관리자 : 퇴장하셨습니다! " + nickname;
                return exit_comment;
            } else if (value.equals("message")) {
                return data.split(":")[1];
            } else if (value.equals("whisper")) {

                return data.split(":")[1];
            } else {//message 외에도 join, exit를 제외한 다른 명령어를 처리
                return "(알 수 없는 명령어)" + data;
            }
        }
    }

    //send event thread
    private class ChatClientSendMessageThread extends Thread {
        //send event
        private void sendMessage(String data) {
            PrintWriter pw;
            try {
                String msg = data;
                if (!whisperCheck(msg) && !envelopeCheck(msg)) { //"whisper: 와 envelope: 아닌경우 message: 송신"
                    msg = "message:" + msg + "\r";
                }
                System.out.println(msg);
                pw = new PrintWriter(new OutputStreamWriter(serverSocket.getOutputStream(), StandardCharsets.UTF_8),
                        true);
                pw.println(msg);

                chatInput.setText("");
                chatInput.requestFocus();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
