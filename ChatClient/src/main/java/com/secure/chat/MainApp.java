package com.secure.chat;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * Created by goodbytes on 8/28/2016.
 */
public class MainApp {
    public static void main(String [] args)
    {


        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        String userName = args[2];

        try{
            KeyGen keyGen = new KeyGen();

            Thread clientThread = new Thread(new Client(hostname, port, userName, keyGen.getPublicKey(), keyGen.getPrivateKey()));
            clientThread.start();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
