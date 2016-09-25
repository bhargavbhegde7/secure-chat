package com.secure.chat;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by goodbytes on 8/28/2016.
 */
public class ClientHandler implements Runnable{

    private Client client;

    private DataInputStream dataInputStream;

    private DataOutputStream dataOutputStream;

    public ClientHandler(Client clientInstance){
        this.client = clientInstance;
        try {
            InputStream inFromClient = client.getSocket().getInputStream();
            dataInputStream = new DataInputStream(inFromClient);

            OutputStream outputStream = client.getSocket().getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<ClientHolder> getClientsMap(List<Client> clients){
        List<ClientHolder> holders = new ArrayList<ClientHolder>();

        for(Client c : clients){
            ClientHolder holder = new ClientHolder(c.getId(),c.getUserName(),c.getPublicKey().getEncoded());
            holders.add(holder);
        }

        return holders;
    }

    private void sendClientsJSON(){
        /**
         * sendUTF the json map of all the clients connected
         */
        try{
            ObjectMapper mapper = new ObjectMapper();

            String clientsListJSON = mapper.writeValueAsString(getClientsMap(Server.clients));

            sendString(clientsListJSON);
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

    private void sendInt(int message){
        try {
            dataOutputStream.writeInt(message);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public List<Client> removeClient(List<Client> clients, Client client){
        for(Client c : clients){
            if(c.getId() == client.getId()){
                clients.remove(c);
            }
        }
        return clients;
    }

    private String receiveString(){

        int length = receiveInt();// read length of incoming message
        byte[] bytes = receiveByteArray(length);
        return new String(bytes);
    }

    private byte[] receiveByteArray(int length){
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
        int message = 0;
        try{
            message = dataInputStream.readInt();
        }catch(IOException e){
            e.printStackTrace();
        }
        return message;
    }

    private boolean targetAccepts(int clientID){
        //get target client
        Client targetClient = getClientByID(clientID, Server.clients);

        //sendString request alert 'targetchange' request

        try{
            OutputStream targetOutputStream = targetClient.getSocket().getOutputStream();
            DataOutputStream targetDataOutputStream = new DataOutputStream(targetOutputStream);
            targetDataOutputStream.writeUTF("%^targetChange^% client with uname "+client.getUserName()+" wants to connect ");
        }
        catch(IOException e){
            e.printStackTrace();
        }

        //receiveString ack
        String ack = receiveString();

        //return
        if("y".equals(ack))
        {
            return true;
        }
        else{
            return false;
        }
    }

    private void handleClientMessage(String message){

        if(message.contains("%^getClientsList^%")){

            //sendUTF all the peers
            sendClientsJSON();

            //receiveString the target client
            int targetClientID = receiveInt();
            Client targetClient = getClientByID(targetClientID, Server.clients);
            ClientHolder holder = new ClientHolder(targetClient.getId(), targetClient.getUserName(), targetClient.getPublicKey().getEncoded());

            //check with the target if it's okay to change its target
            if(targetAccepts(targetClientID)){
                client.setClientHolder(holder);
                //sendString target accepted message
                sendString("yes");
            }
            else{
                //sendString target declined message
                sendString("no");
            }
        }

        else{
            //target is not set yet
            if(client.getClientHolder()==null){

                 //tell the client that target is not set
                sendString("Set a target by sending %^getClientsList^% to the server as a message");

            }
            else{
                ClientHolder target = client.getClientHolder();

                Client targetClient = getClientByID(target.getId(), Server.clients);
                Socket targetSocket = targetClient.getSocket();

                try{
                    OutputStream targetOutputStream = targetSocket.getOutputStream();
                    DataOutputStream targetOut = new DataOutputStream(targetOutputStream);

                    targetOut.writeUTF(message);
                }catch(IOException e){
                    e.printStackTrace();
                }
            }

        }
    }

    private Client getClientByID(int targetClientID, List<Client> clients){
        Client targetClient = null;
        for(Client c : clients){
            if(c.getId() == targetClientID){
                targetClient = c;
                break;
            }
        }
        return targetClient;
    }

    public void run() {

        String message;

        //read username
        message = receiveString();
        client.setUserName(message);

        //read public key
        int keyLength = receiveInt();
        byte[] pubKey = receiveByteArray(keyLength);

        try{
            //set public key for current client
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubKey));
            client.setPublicKey(publicKey);
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        while(true){
            try {
                //keep listening to the client
                message = receiveString();
                //message handler
                handleClientMessage(message);
            } catch (Exception e) {//IOException if client disconnects
                e.printStackTrace();
                break;
            }
        }
        try {
            client.getSocket().close();
            Server.clients = removeClient(Server.clients, client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//run ends
}
