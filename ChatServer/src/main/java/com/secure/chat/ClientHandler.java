package com.secure.chat;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
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
    public ClientHandler(Client clientInstance){
        this.client = clientInstance;
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
            OutputStream outTputStream = client.getSocket().getOutputStream();
            DataOutputStream out = new DataOutputStream(outTputStream);
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

    private String receive(){
        String responseMessage = null;
        try{
            InputStream inFromClient = client.getSocket().getInputStream();
            DataInputStream in = new DataInputStream(inFromClient);
            responseMessage = in.readUTF();
        }catch(IOException e){
            e.printStackTrace();
        }
        return responseMessage;
    }

    public void handleClientMessage(String message){
        //// TODO: 9/5/2016
        //send it to all the targets of the current client
    }

    public void run() {
        DataInputStream in = null;
        try{
            in = new DataInputStream(client.getSocket().getInputStream());

            //read username
            String message = receive();
            client.setUserName(message);

            //read public key
            int length = in.readInt();// read length of incoming message
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
        String targetCSV = receive();
        //todo set list of client targets in each client
        //todo set the client targets
        while(true){
            try {
                //keep listening to the client
                String message = receive();
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
