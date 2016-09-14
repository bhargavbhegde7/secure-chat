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

    /*public void setClientHolder(ClientHolder holder){
        this.clientHolder = holder;
    }*/

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

    private void send(String message){
        try {
            sendInt(message.length());
            //out.write(hexStringToByteArray(message));
            out.write(message.getBytes("UTF-8"));
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    /*private String receive(){
        String responseMessage = null;
        try{
            responseMessage = in.readUTF();
        }catch(IOException e){
            e.printStackTrace();
        }
        return responseMessage;
    }*/

    private String receiveString(){
        int length = receiveInt();// read length of incoming message
        byte[] bytes = receiveByteArray(length);
        return new String(bytes);
    }

    private byte[] receiveByteArray(int length){
        System.out.println("waiting for incoming bytes");
        byte[] bytes = new byte[0];
        if(length>0) {
            bytes = new byte[length];
            try{
                in.readFully(bytes, 0, bytes.length); // read the message
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return bytes;
    }

    private int receiveInt(){
        System.out.println("waiting for incoming int");
        int responseMessage = 0;
        try{
            responseMessage = in.readInt();
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

    private List<ClientHolder> getClientsFromJSON(String clientsJSON){

        ObjectMapper mapper = new ObjectMapper();
        List<ClientHolder> holders = null;

        try{
            holders = mapper.readValue(clientsJSON, new TypeReference<List<ClientHolder>>() {});
        }catch(IOException e){
            e.printStackTrace();
        }
        return holders;
    }

    private void handleServerMessage(String serverMsg){

        if(serverMsg.contains("%^targetChange^%")){
            //prompt out the target id of the caller
            System.out.println(serverMsg+" : respond with y/n");
            //take user input (yes or no)
            String isAccepted = getUserInput();
            //send response (yes or no)
            send(isAccepted);
        }

        else{
            System.out.println(serverMsg);
        }
    }

    private void startListener(){
        Thread listenerThread = new Thread(new Runnable() {
            public void run() {
                String serverMsg;
                while(true){
                    serverMsg = receiveString();
                    handleServerMessage(serverMsg);
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

    private void handleUserInput(String inputMsg){
        if(inputMsg.contains("%^getClientsList^%")){

            send(inputMsg);

            //receive clients
            String clientsJSON = receiveString();
            List<ClientHolder> holders = getClientsFromJSON(clientsJSON);

            //show clients
            printHolders(holders);
            System.out.println("Enter the user id of the target client");

            //get client target from userinput
            //get target client ID from user
            //todo keep target in the client
            inputMsg = getUserInput();

            //send target id
            sendInt(Integer.parseInt(inputMsg));

            //wait for target accepted or declined
            String accepted = receiveString();

            //if accepted
            if("yes".equals(accepted)){
                //change current target
                this.clientHolder = getClientHolderByID(Integer.parseInt(inputMsg), holders);
            }
            else{
                //do nothing
            }
        }
        else{
            //simple message. send it directly
            send(inputMsg);
        }
    }

    @Override
    public void run(){
        String inputMsg;

        /* ------- initial communication ------- */
        //send userName
        send(userName);

        //send public key
        sendBytes(publicKey.getEncoded());

        //get all connected clients details

        /* ------- initial communication ------- */

        /* start a thread to keep listening to the server */
        startListener();

        /* in the current thread, keep getting user input and sending it asynch */
        while(true) {
            inputMsg = getUserInput();
            //sendUTF(inputMsg);
            handleUserInput(inputMsg);
        }
    }
}
