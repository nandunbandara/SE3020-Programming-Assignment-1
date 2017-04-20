/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import station.StationRMI;
import java.net.*;
import java.io.*;
import com.google.gson.*;
import java.awt.Toolkit;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author ntban_000
 */
public class Server extends UnicastRemoteObject implements ServerRMI, Runnable{
//    private List<GSON>
    public static ArrayList<StationRMI> stations;
    public static HashMap<String, SensorThread> sensors; 
    
    public Server() throws RemoteException{
        
    }
    
    public static void main(String[] args) throws RemoteException{
        
        sensors = new HashMap<>();
        stations = new ArrayList<StationRMI>();
        try {
            Registry reg = LocateRegistry.createRegistry(1009);
            Server server = new Server();
            reg.rebind("server",server);
            Thread thread = new Thread(server);
            thread.start();
        } catch (RemoteException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    @Override
    public void run() {
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
        while(true){
            try {
                clientSock = serverSock.accept();
                SensorThread sensor = new SensorThread(clientSock);
                String sensor_name = sensor.getLocation()+"-"+sensor.getType();
                if(!sensors.containsKey(sensor_name)){
                    sensors.put(sensor_name, sensor);
                }
                sensor.start();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
     @Override
    public void addStation(StationRMI station) throws RemoteException {
        Server.stations.add(station);
    }
}
//used to create seperate threads for each connected sensor
class SensorThread extends Thread{
    private Socket clientSocket;
    private ObjectInputStream input;
    private FileWriter fileOutput;
    private String location;
    private String type;
    private int status;
    private HashMap<String, SensorThread> sensors;
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
                String[] object = (String[]) input.readObject();
                System.out.println(object[0]);
                if(object.length==2){
                    location = object[0];
                    type = object[1];
                }else{
                    //write to file
                    for(int i=0;i<object.length;i++){
                        JsonObject jsonObject = new JsonParser().parse(object[i]).getAsJsonObject();
    //                    System.out.println(jsonObject);
                        location = jsonObject.get("location").toString().split("\"")[1];
                        type = jsonObject.get("type").toString().split("\"")[1];
                        //alert
                        double value = Double.parseDouble(jsonObject.get("value").toString());
                        switch(type){
                            case "rainfall":
                                if(value>20.00){
                                  //alert when rainfall is greater than 20 mm
                                  for(StationRMI station : Server.stations){
                                      station.alert("Alert: Rainfall "+value+" in "+location+"\n");
//                                      System.out.print(station);
                                  }
                                }
                        }
                        fileOutput = new FileWriter(location+"_"+type+".txt",true);
                        fileOutput.write(jsonObject.toString()+"\n");
                        fileOutput.close();
                    }
                }
            } catch (IOException ex) {
                System.out.println("Connection lost");
                break;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(SensorThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            clientSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(SensorThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String getLocation(){
        return location;
    }
    
    public String getType(){
        return type;
    }
    
    public int getStatus(){
        return status;
    }

   
}