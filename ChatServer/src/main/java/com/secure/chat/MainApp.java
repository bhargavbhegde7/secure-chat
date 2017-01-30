package com.secure.chat;

import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by goodbytes on 8/28/2016.
 */
public class MainApp {
    static Logger logger = LoggerAdapter.getLogger(String.valueOf(MainApp.class));
    public static void main(String[] args) {
        logger.error("Shit my pants");
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
