/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.net.*;
import java.io.*;
import com.google.gson.*;
/**
 *
 * @author ntban_000
 */
public class Server {
//    private List<GSON> 
    public static void main(String[] args){
        ServerSocket serverSock = null;
        Socket clientSock = null;
        ObjectInputStream input = null;
        
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
                
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
