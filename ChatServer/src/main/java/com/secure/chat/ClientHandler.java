package com.secure.chat;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by goodbytes on 8/28/2016.
 */
public class ClientHandler implements Runnable{
    private Client client;
    public ClientHandler(Client clientInstance){
        this.client = clientInstance;
    }

    private Map<Integer, String> getIdUserMap(List<Client> clients){
        Map<Integer, String> idUserMap = new HashMap<Integer, String>();

        for(Client client : clients){
            idUserMap.put(client.getId(), client.getUserName());
        }

        return idUserMap;
    }

    private void handleClientMessage(String message){
        /**
         * echo the incoming message
         */
        if(message.contains("%getecho%")){
            try{
                DataOutputStream out = new DataOutputStream(client.getSocket().getOutputStream());
                out.writeUTF(message);//echo
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        /**
         * send the json map of all the clients connected
         */
        if(message.equals("%getclients%")){
            try{

                ObjectMapper mapper = new ObjectMapper();

                String idUserNameJsonMap = mapper.writeValueAsString(getIdUserMap(Server.clients));

                DataOutputStream out = new DataOutputStream(client.getSocket().getOutputStream());
                out.writeUTF(idUserNameJsonMap);//echo
            }catch(IOException e){
                e.printStackTrace();
            }
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

    public void run() {
        DataInputStream in = null;
        try{
            in = new DataInputStream(client.getSocket().getInputStream());
            String message = in.readUTF();
            client.setUserName(message);
        }catch(IOException e){
            e.printStackTrace();
        }

        while(true){
            try {
                in = new DataInputStream(client.getSocket().getInputStream());
                String message = in.readUTF();
                System.out.println(message);

                handleClientMessage(message);
            } catch (IOException e) {
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
