package com.secure.chat;

import java.io.IOException;

/**
 * Created by goodbytes on 8/28/2016.
 */
public class MainApp {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        try
        {
            Thread t = new Server(port);
            t.start();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
