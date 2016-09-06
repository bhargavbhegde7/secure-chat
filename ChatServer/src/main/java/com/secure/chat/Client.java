package com.secure.chat;

import java.net.Socket;
import java.security.PublicKey;

/**
 * Created by goodbytes on 8/28/2016.
 */
public class Client {
    private Socket socket;
    private static int clientCount = 0;
    private int id;
    private String userName;
    private PublicKey publicKey;
    private ClientHolder clientHolder;

    public Client(Socket socketInstance) {
        this.socket = socketInstance;
        clientCount = clientCount+1;
        id = clientCount;
    }

    public void setClientHolder(ClientHolder holder){
        this.clientHolder = clientHolder;
    }

    public ClientHolder getClientHolder(){
        return clientHolder;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getId() {
        return id;
    }

    /*public void setId(int id) {
        this.id = id;
    }*/

    public Socket getSocket() {
        return socket;
    }

    /*public void setSocket(Socket socket) {
        this.socket = socket;
    }*/

    public PublicKey getPublicKey(){
        return publicKey;
    }

    public void setPublicKey(PublicKey pubKey){
        this.publicKey = pubKey;
    }
}
