package com.secure.chat;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Created by goodbytes on 1/30/2017.
 */
public class LoggerAdapter {
    public static Logger getLogger(String classname){
        String log4jConfigFile = System.getProperty("user.dir")+"/src/main/resources/"+ "log4j.properties";
        PropertyConfigurator.configure(log4jConfigFile);
        return Logger.getLogger(classname);
    }
}
