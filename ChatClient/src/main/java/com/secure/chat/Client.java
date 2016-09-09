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

    private String userName;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    private DataOutputStream out;

    private DataInputStream in;

    private ClientHolder clientHolder;

    public void setClientHolder(ClientHolder holder){
        this.clientHolder = holder;
    }

    public ClientHolder getClientHolder(){
        return clientHolder;
    }

    public Client(String serverIp, int serverPort, String uName, PublicKey pubKey, PrivateKey privKey){

        Socket socket = null;
        this.publicKey = pubKey;
        this.privateKey = privKey;

        this.userName = uName;

        System.out.println("Connecting to " + serverIp +" on port " + serverPort);
        try {
            socket = new Socket(serverIp, serverPort);

            OutputStream outputStream = socket.getOutputStream();
            this.out = new DataOutputStream(outputStream);

            InputStream inFromServer = socket.getInputStream();
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

    public ClientHolder getClientHolderByID(int id, List<ClientHolder> holders){
        ClientHolder holder = null;
        for(ClientHolder h : holders){
            if(h.getId() == id){
                holder = h;
                break;
            }
        }
        return holder;
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

        //get target client ID from user
        //todo keep target in the client
        inputMsg = getUserInput();
        sendInt(Integer.parseInt(inputMsg));

        setClientHolder(getClientHolderByID(Integer.parseInt(inputMsg), holders));
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
