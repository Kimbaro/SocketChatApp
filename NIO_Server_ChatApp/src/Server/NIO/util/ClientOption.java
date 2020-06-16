package Server.NIO.util;

import java.nio.channels.SelectionKey;

public class ClientOption {
    String nickname = null;
    StringBuffer data = null;
    SelectionKey my_key = null;

    String partner_name = null; // 1:1채팅 상대방 닉네임. => 명확하게 상대방 키를 찾기 위한 비교값으로 활용.
    SelectionKey partner_key = null; // 1:1채팅 대상
    boolean newClient = true; //true =신규, false =기존

    boolean exit = false; //false =채팅방접속중, true =채팅방퇴장

    public ClientOption(String nickname, SelectionKey my_key, SelectionKey partner_key) {
        this.nickname = nickname;
        this.my_key = my_key;
        this.partner_key = partner_key;
    }

    public ClientOption(String nickname, SelectionKey my_key) {
        this.nickname = nickname;
        this.my_key = my_key;
    }

    public boolean isExit() {
        return exit;
    }

    public void setExit(boolean exit) {
        this.exit = exit;
    }

    public ClientOption(String nickname) {
        this.nickname = nickname;
    }

    public ClientOption() {
    }

    public String getPartner_name() {
        return partner_name;
    }

    public void setPartner_name(String partner_name) {
        this.partner_name = partner_name;
    }

    public String getNickname() {
        return nickname;
    }

    public StringBuffer getData() {
        return data;
    }

    public void setData(StringBuffer data) {
        this.data = data;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public SelectionKey getMy_key() {
        return my_key;
    }

    public void setMy_key(SelectionKey my_key) {
        this.my_key = my_key;
    }

    public SelectionKey getPartner_key() {
        return partner_key;
    }

    public void setPartner_key(SelectionKey partner_key) {
        this.partner_key = partner_key;
    }

    public boolean isNewClient() {
        return newClient;
    }

    public void setNewClient(boolean newClient) {
        this.newClient = newClient;
    }
}
