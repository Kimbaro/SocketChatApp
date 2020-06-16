package Client.OIO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

//닉네임 입력 GUI
public class ClientGUI_Start2 {
    private String nickName = "알수없음";
    private ClientThread2 clientThread = null;

    public ClientGUI_Start2() {
        JFrame fr = new JFrame("Kimbaro's Chat");
        JPanel pn = new JPanel();

        JButton bt;
        JTextField jt;
        GridBagConstraints[] gbc = new GridBagConstraints[2];

        GridBagLayout gbl = new GridBagLayout();
        pn.setLayout(gbl);

        /*Item Init*/
        bt = new JButton("접속");
        gbc[0] = new GridBagConstraints();

        jt = new JTextField(10);
        gbc[1] = new GridBagConstraints();

        /*Event Set*/
        bt.addActionListener(e -> {
            if (jt.getText().equals(" ")) {
                nickName = jt.getText();
            } else {

                //case1
                clientThread = new ClientThread2(jt.getText());
                clientThread.startChat();
            }
            //다음 프레임 넘어감

        });
        jt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                char keyCode = e.getKeyChar();
                if (keyCode == KeyEvent.VK_ENTER) {
                    //다음 프레임 넘어감.
                    if (jt.getText().equals(" ")) {
                        nickName = jt.getText();
                    } else {
                        clientThread = new ClientThread2(jt.getText());
                        clientThread.startChat();
                    }
                }
            }
        });

        /*Layout Set*/
        gbc[0].gridx = 0;
        gbc[0].gridy = 0;
        pn.add(bt, gbc[0]);

        gbc[1].gridx = 1;
        gbc[1].gridy = 0;
        pn.add(jt, gbc[1]);

        fr.setContentPane(pn);

        fr.setSize(500, 300);
        fr.setLocation(100, 100);
        fr.setVisible(true);
    }

    public String getNickName() {
        return nickName;
    }
}
