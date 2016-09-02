package com.secure.chat;

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
            Thread clientThread = new Thread(new Client(hostname, port, userName));
            clientThread.start();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
