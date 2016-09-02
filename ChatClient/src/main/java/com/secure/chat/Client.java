package com.secure.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
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
    private Socket socket = null;
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

    private void handleUserInput(String input){
        /**
         * send an echo message
         */
        if(input.contains("%getecho%")){
            send("%getecho%");
            String responseMessage = receive();//todo handle null
            System.out.println("server says : "+responseMessage);
        }
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

    private void getClients(){
        send(GET_CLIENTS);
        String responseMessage = receive();

        ObjectMapper mapper = new ObjectMapper();
        Map<Integer, String> idUserNameMap;

        try{
            idUserNameMap = mapper.readValue(responseMessage, new TypeReference<Map<Integer, String>>() {});
            Iterator it = idUserNameMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                System.out.println(pair.getKey() + " = " + pair.getValue());
                it.remove(); // avoids a ConcurrentModificationException
            }
            System.out.println("\n\n");
            System.out.println("Enter the user id of the target clients with comma separated values");//todo move this to main thread

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        shakeHands();

        /* ------- initial communication ------- */
        send(userName);
        getClients();
        /* ------- initial communication ------- */

        while(true) {
            String inputMsg = getUserInput();
            handleUserInput(inputMsg);
        }
    }
}
