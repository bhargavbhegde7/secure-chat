package com.secure.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by goodbytes on 8/28/2016.
 */
public class Client extends Thread{

    private static final String GET_CLIENTS = "%getclients%";

    private String serverName;
    private int port;
    private static Socket socket = null;
    private String userName;

    public Client(String serverIp, int serverPort, String uname){
        this.serverName = serverIp;
        this.port = serverPort;
        this.userName = uname;

        System.out.println("Connecting to " + serverName +" on port " + port);
        try {
            socket = new Socket(serverName, port);
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.println("Just connected to "+ socket.getRemoteSocketAddress());
    }

    private String getIntent(String message){
        return null;
    }

    private void send(String message){
        try {
            OutputStream outTputStream = socket.getOutputStream();
            DataOutputStream out = new DataOutputStream(outTputStream);
            out.writeUTF(message);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private String receive(){
        String responseMessage = null;
        try{
            InputStream inFromServer = socket.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            responseMessage = in.readUTF();
            //responseHandler(responseMessage);
        }catch(IOException e){
            e.printStackTrace();
        }
        return responseMessage;
    }

    private void responseHandler(String message){
        //todo handle response message with another attribute for decide what to do with it
        System.out.println("Server says " + message);
    }

    private String getUserInput(){
        System.out.println("enter message");
        Scanner scan = new Scanner(System.in);
        String inputMsg = scan.next();
        return inputMsg;
    }

    private void shakeHands(){
        //todo other handshake things if existing
    }

    private void printMap(Map map){
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
        System.out.println("\n\n");
    }

    private Map<Integer,String> getClients(){
        send(GET_CLIENTS);
        String responseMessage = receive();

        ObjectMapper mapper = new ObjectMapper();
        Map<Integer, String> idUserNameMap = null;

        try{
            idUserNameMap = mapper.readValue(responseMessage, new TypeReference<Map<Integer, String>>() {});
        }catch(IOException e){
            e.printStackTrace();
        }
        return idUserNameMap;
    }

    private void startListener(){
        Thread listenerThread = new Thread(new Runnable() {
            public void run() {
                String serverMsg;
                while(true){
                    serverMsg = receive();
                    System.out.println(serverMsg);
                }
            }
        });
        listenerThread.start();
    }

    @Override
    public void run(){
        String inputMsg;
        //shakeHands();//todo

        /* ------- initial communication ------- */
        send(userName);
        Map idUserNameMap = getClients();
        printMap(idUserNameMap);
        System.out.println("Enter the user id of the target clients with comma separated values");//todo move this to main thread
        inputMsg = getUserInput();
        send(inputMsg);
        /* ------- initial communication ------- */

        /* start a thread to keep listening to the server */
        startListener();

        /* in this thread, keep getting user input and sending it asynch */
        while(true) {
            inputMsg = getUserInput();
            send(inputMsg);
        }
    }
}
