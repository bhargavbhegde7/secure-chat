package com.secure.chat;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by goodbytes on 8/28/2016.
 */
public class Client extends Thread{

    private String serverName;
    private int port;

    public Client(String serverIp, int serverPort){
        this.serverName = serverIp;
        this.port = serverPort;
    }

    private void handleUserInput(String input){
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

    @Override
    public void run() {
        try
        {
            System.out.println("Connecting to " + serverName +" on port " + port);
            Socket socket = new Socket(serverName, port);
            System.out.println("Just connected to "+ socket.getRemoteSocketAddress());

            OutputStream outTputStream = socket.getOutputStream();
            DataOutputStream out = new DataOutputStream(outTputStream);
            out.writeUTF("pooperson");

            while(true) {

                System.out.println("enter message");
                Scanner scan = new Scanner(System.in);
                String inputMsg = scan.next();

                //out.writeUTF(inputMsg);
                handleUserInput(inputMsg);

                /*InputStream inFromServer = socket.getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);
                System.out.println("Server says " + in.readUTF());*/

            }

        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
