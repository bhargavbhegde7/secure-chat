package com.secure.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by goodbytes on 8/28/2016.
 */
public class Server extends Thread
{
    public static List<Client> clients;

    private ServerSocket serverSocket;

    public Server(int port) throws IOException{
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(0); //zero is infinite
        clients = new ArrayList<Client>();
    }



    @Override
    public void run(){
        while(true){

            try{
                System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
                Socket socket = serverSocket.accept();
                System.out.println("Just connected to "+ socket.getRemoteSocketAddress());
                Client client = new Client(socket);
                clients.add(client);
                ClientHandler clientHandler = new ClientHandler(client);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }catch(SocketTimeoutException s){
                System.out.println("Socket timed out!");
                break;
            }catch(IOException e){
                e.printStackTrace();
                break;
            }
            System.out.println("another iteration");
        }//while ends
    }//run ends
}