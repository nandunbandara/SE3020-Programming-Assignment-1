/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.net.*;
import java.io.*;
import com.google.gson.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author ntban_000
 */
public class Server {
//    private List<GSON> 
    public static void main(String[] args) throws ClassNotFoundException{
        ServerSocket serverSock = null;
        Socket clientSock = null;
        ObjectInputStream input = null;
        HashMap<String,Gson> weatherRecords = new HashMap<>();
        final int PORT = 6666;
        
        try{
            serverSock = new ServerSocket(PORT);
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.println("Server listening on port "+PORT+" for sensors");
        
//        try{
//            clientSock = serverSock.accept();
//            input = new ObjectInputStream(clientSock.getInputStream());
//            
//            while(true){
//                String object = (String) input.readObject();
//                System.out.println(object);
//            }
//        }catch(IOException e){
//            e.printStackTrace();
//        }
        while(true){
            try {
                clientSock = serverSock.accept();
                new SensorThread(clientSock).start();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
//used to create seperate threads for each connected sensor
class SensorThread extends Thread{
    private Socket clientSocket;
    private ObjectInputStream input;
    
    public SensorThread(Socket clientSocket){
        this.clientSocket = clientSocket;
    }
    
    @Override
    public void run(){
        try {
            input = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        while(true){
            try {
                System.out.println((String)input.readObject());
            } catch (IOException ex) {
                Logger.getLogger(SensorThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(SensorThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}