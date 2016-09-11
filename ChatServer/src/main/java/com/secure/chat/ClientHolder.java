package com.secure.chat;

/**
 * Created by goodbytes on 9/5/2016.
 *
 * this class is used for holding the target client in every peer.
 * using this holder the server figures out which client is talking to which
 *
 */
public class ClientHolder {
    private int id;
    String userName;
    private byte[] publicKey;

    public ClientHolder(int id, String userName, byte[] publicKey) {
        this.id = id;
        this.userName = userName;
        this.publicKey = publicKey;
    }

    public ClientHolder() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }
}
