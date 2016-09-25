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

    private DataOutputStream dataOutputStream;

    private DataInputStream dataInputStream;

    private ClientHolder clientHolder;

    private Socket socket;

    public ClientHolder getClientHolder(){
        return clientHolder;
    }

    public Client(String serverIp, int serverPort, String uName, PublicKey pubKey, PrivateKey privKey){

        //Socket socket = null;
        this.publicKey = pubKey;
        this.privateKey = privKey;

        this.userName = uName;

        System.out.println("Connecting to " + serverIp +" on port " + serverPort);
        try {
            socket = new Socket(serverIp, serverPort);

            OutputStream outputStream = socket.getOutputStream();
            this.dataOutputStream = new DataOutputStream(outputStream);

            InputStream inFromServer = socket.getInputStream();
            this.dataInputStream = new DataInputStream(inFromServer);
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.println("Just connected to "+ socket.getRemoteSocketAddress());
    }

    private void sendBytes(byte[] message){
        try {
            sendInt(message.length);
            dataOutputStream.write(message);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void sendInt(int message){
        try {
            dataOutputStream.writeInt(message);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void sendString(String message){
        try {
            sendInt(message.length());
            dataOutputStream.write(message.getBytes("UTF-8"));
        }catch(IOException e){
            e.printStackTrace();
        }
    }

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
                dataInputStream.readFully(bytes, 0, bytes.length); // read the message
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
            responseMessage = dataInputStream.readInt();
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

        //check if it's any special signal
        if(serverMsg.contains("%^targetChange^%")){
            //prompt the target id of the caller
            System.out.println(serverMsg+" : respond with y/n");
            //take user input (yes or no)
            String isAccepted = getUserInput();
            //sendString response (yes or no)
            sendString(isAccepted);
        }
        else{
            //print it and wait for the user to respond
            System.out.println(serverMsg);
            String inputMsg = getUserInput();
            //sendUTF(inputMsg);
            handleUserInput(inputMsg);
        }
    }

    /*private void startUserInputListenerThread(){
        Thread userInputThread = new Thread(new Runnable() {//todo lambda
            public void run() {
                String userInput;
                while(true){
                    userInput = getUserInput();
                    handleUserInput(userInput);
                }
            }
        });
        userInputThread.start();
    }*/

    private void startUserInputListener(){
        String userInput;
        while(true){
            userInput = getUserInput();
            handleUserInput(userInput);
        }
    }

    /*private void startServerListener(){
        String serverMsg;
        while(true){
            serverMsg = receiveString();
            handleServerMessage(serverMsg);
        }
    }*/

    private void startServerListenerThread(){
        Thread serverListenerThread = new Thread(new Runnable() {//todo lambda
            public void run() {
                String serverMsg;
                while(true){
                    serverMsg = receiveString();
                    handleServerMessage(serverMsg);
                }
            }
        });
        serverListenerThread.start();
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

            sendString(inputMsg);

            //receive clients
            String clientsJSON = receiveString();
            List<ClientHolder> holders = getClientsFromJSON(clientsJSON);

            //show clients
            printHolders(holders);
            System.out.println("Enter the user id of the target client");

            //get client target from userinput
            //get target client ID from user
            //todo keep target dataInputStream the client
            inputMsg = getUserInput();

            //sendString target id
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
                System.out.println("target rejected the request");
            }
        }
        else{
            //simple message. sendString it directly
            sendString(inputMsg);
        }
    }

    @Override
    public void run(){

        //sendString userName
        sendString(userName);

        //sendString public key
        sendBytes(publicKey.getEncoded());

        //separate thread to keep getting user input and handling it asynch
        //startUserInputListenerThread();
        //startServerListenerThread();

        //startUserInputListener();
        //startServerListener();

        //keep listening to server message
        String serverMsg;
        while(true){
            serverMsg = receiveString();
            handleServerMessage(serverMsg);
        }
    }
}
