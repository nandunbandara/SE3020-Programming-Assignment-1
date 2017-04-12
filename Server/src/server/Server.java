/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.net.*;
import java.io.*;
/**
 *
 * @author ntban_000
 */
public class Server {
    public static void main(String[] args){
        ServerSocket serverSock = null;
        Socket clientSock = null;
        
        final int PORT = 6666;
        
        try{
            serverSock = new ServerSocket(PORT);
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.println("Server listening on port "+PORT+" for sensors");
        
        try{
            clientSock = serverSock.accept();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
