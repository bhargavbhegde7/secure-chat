package com.secure.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by goodbytes on 8/28/2016.
 */
public class Client extends Thread{

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

    public String getIntent(String message){
        return null;
    }

    public void send(String message){
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

    public void responseHandler(String message){
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

        /**
         * request for the client list
         */
        if(input.equals("%getclients%")){
            send("%getclients%");
            String responseMessage = receive();

            ObjectMapper mapper = new ObjectMapper();
            Map<Integer, String> map = new HashMap<Integer, String>();

            try {
                    map = mapper.readValue(responseMessage, new TypeReference<Map<Integer, String>>() {});
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private String getUserInput(){
        System.out.println("enter message");
        Scanner scan = new Scanner(System.in);
        String inputMsg = scan.next();
        return inputMsg;
    }

    public void shakeHands(){
        //todo other handshake things if existing
        send(userName);
    }

    @Override
    public void run() {
        shakeHands();
        while(true) {
            String inputMsg = getUserInput();
            handleUserInput(inputMsg);
        }
    }
}
