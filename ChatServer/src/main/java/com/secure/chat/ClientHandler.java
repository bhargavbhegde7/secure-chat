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

    private DataInputStream in;

    private DataOutputStream out;

    public ClientHandler(Client clientInstance){
        this.client = clientInstance;
        try {
            InputStream inFromClient = client.getSocket().getInputStream();
            in = new DataInputStream(inFromClient);

            OutputStream outputStream = client.getSocket().getOutputStream();
            out = new DataOutputStream(outputStream);
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
         * send the json map of all the clients connected
         */
        try{
            ObjectMapper mapper = new ObjectMapper();

            String clientsListJSON = mapper.writeValueAsString(getClientsMap(Server.clients));

            send(clientsListJSON);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void send(String message){
        try {
            out.writeUTF(message);
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

    private String receiveUTF(){
        String responseMessage = null;
        try{
            responseMessage = in.readUTF();
        }catch(IOException e){
            e.printStackTrace();
        }
        return responseMessage;
    }

    private int receiveInt(){
        int responseMessage = 0;
        try{
            responseMessage = in.readInt();
        }catch(IOException e){
            e.printStackTrace();
        }
        return responseMessage;
    }

    public void handleClientMessage(String message){

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
        try{

            //read username
            String message = receiveUTF();
            client.setUserName(message);

            //read public key
            int length = receiveInt();// read length of incoming message
            byte[] pubKey = new byte[0];
            if(length>0) {
                pubKey = new byte[length];
                in.readFully(pubKey, 0, pubKey.length); // read the message
            }
            try{
                //set public key for current client
                PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubKey));
                client.setPublicKey(publicKey);
            }catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }

        }catch(IOException e){
            e.printStackTrace();
        }

        //send details of all the clients
        sendClientsJSON();

        //get the target clients
        int targetClientID = receiveInt();
        Client targetClient = getClientByID(targetClientID, Server.clients);
        ClientHolder holder = new ClientHolder(targetClient.getId(), targetClient.getUserName(), targetClient.getPublicKey().getEncoded());
        client.setClientHolder(holder);

        while(true){
            try {
                //keep listening to the client
                String message = receiveUTF();
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
