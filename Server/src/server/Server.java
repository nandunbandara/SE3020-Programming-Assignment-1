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
        
        try{
            clientSock = serverSock.accept();
            input = new ObjectInputStream(clientSock.getInputStream());
            
            while(true){
                Gson object = (Gson)input.readObject();
                
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
