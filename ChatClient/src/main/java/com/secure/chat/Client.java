package com.secure.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
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
    private PublicKey publicKey;
    private PrivateKey privateKey;

    private OutputStream outTputStream;
    private DataOutputStream out;

    private InputStream inFromServer;
    private DataInputStream in;

    public Client(String serverIp, int serverPort, String uname, PublicKey pubKey, PrivateKey privKey){

        this.publicKey = pubKey;
        this.privateKey = privKey;

        this.serverName = serverIp;
        this.port = serverPort;
        this.userName = uname;

        System.out.println("Connecting to " + serverName +" on port " + port);
        try {
            this.socket = new Socket(serverName, port);

            this.outTputStream = socket.getOutputStream();
            this.out = new DataOutputStream(outTputStream);

            this.inFromServer = socket.getInputStream();
            this.in = new DataInputStream(inFromServer);
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.println("Just connected to "+ socket.getRemoteSocketAddress());
    }

    private void sendUTF(String message){
        try {
            out.writeUTF(message);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void sendBytes(byte[] message){
        try {
            sendInt(message.length);
            out.write(message);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void sendInt(int message){
        try {
            out.writeInt(message);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private String receive(){
        String responseMessage = null;
        try{
            responseMessage = in.readUTF();
        }catch(IOException e){
            e.printStackTrace();
        }
        return responseMessage;
    }

    private String getUserInput(){
        System.out.println("enter message");
        Scanner scan = new Scanner(System.in);
        String inputMsg = scan.next();
        return inputMsg;
    }

    private void printHolders(List<ClientHolder> holders){

        for(ClientHolder ch : holders){
            System.out.println(" id : "+ ch.getId() +" , "+" userName : "+ch.getUserName());
        }

        System.out.println("\n\n");
    }

    private List<ClientHolder> getClients(String clientsJSON){

        ObjectMapper mapper = new ObjectMapper();
        List<ClientHolder> holders = null;

        try{
            holders = mapper.readValue(clientsJSON, new TypeReference<List<ClientHolder>>() {});
        }catch(IOException e){
            e.printStackTrace();
        }
        return holders;
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

        /* ------- initial communication ------- */
        //send userName
        sendUTF(userName);

        //send public key
        sendBytes(publicKey.getEncoded());

        //get all connected clients details
        String clientsJSON = receive();
        List<ClientHolder> holders = getClients(clientsJSON);

        printHolders(holders);
        System.out.println("Enter the user id of the target clients with comma separated values");

        //get target clients from user
        //todo keep list of targets in the client (or not !!) undecided
        inputMsg = getUserInput();
        sendUTF(inputMsg);
        /* ------- initial communication ------- */

        /* start a thread to keep listening to the server */
        startListener();

        /* in the current thread, keep getting user input and sending it asynch */
        while(true) {
            inputMsg = getUserInput();
            sendUTF(inputMsg);
        }
    }
}
