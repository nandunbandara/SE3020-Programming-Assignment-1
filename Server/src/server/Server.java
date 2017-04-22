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
    public static ArrayList<String> sensors; 
    public static HashMap<String, SensorThread> sensorThreads;
    public Server() throws RemoteException{
        
    }
    
    public static void main(String[] args) throws RemoteException{
        
        sensors = new ArrayList<>();
        stations = new ArrayList<StationRMI>();
        sensorThreads = new HashMap<>();
        
        try {
            Registry reg = LocateRegistry.createRegistry(1009);
            Server server = new Server();
            reg.rebind("server",server);
            Thread thread = new Thread(server);
            thread.start();
        } catch (RemoteException ex) {
            System.out.println("Monitor station disconnected");
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
                SensorThread sensor = new SensorThread(clientSock,sensors);
                sensor.start();
                String sensor_name = sensor.getLocation()+"-"+sensor.getType();
//                if(!sensors.contains(sensor_name)){
//                    sensors.add(sensor_name);
//                    sensorThreads.put(sensor_name, sensor);
//                }
                updateConnectedSensors();
                updateConnectedSensorsCount();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
     @Override
    public void addStation(StationRMI station) throws RemoteException {
        Server.stations.add(station);
    }
    
    
    public static void updateConnectedSensors(){
        for(StationRMI station : stations){
            try {
                station.setSensorList(sensors);
            } catch (RemoteException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            } catch(NullPointerException ex){
                
            }
        }
    }
    
    public static void updateConnectedSensorsCount(){
        for(StationRMI station: stations){
            try{
                station.setConnectedSensors(sensors.size());
//                  station.setConnectedSensors(4);
            }catch(RemoteException e){
                System.out.println(e);
            }catch(NullPointerException ex){
                
            }
        }
    }
    
    public static void setSensors(String name){
        sensors.add(name);
    }

    @Override
    public void setSensorCount() throws RemoteException {
        for(StationRMI station: stations){
            try{
                station.setConnectedSensors(sensors.size());
//                  station.setConnectedSensors(4);
            }catch(RemoteException e){
                System.out.println(e);
            }
        }
    }

    @Override
    public void setConnectedSensors() throws RemoteException {
        for(StationRMI station : stations){
            try {
                station.setSensorList(sensors);
            } catch (RemoteException ex) {
                
            } 
        }
    }

    @Override
    public void setConnectedMonitoringStationsCount() throws RemoteException {
        for(StationRMI station : stations){
            try {
                station.setMonitoringStationsCount(stations.size());
            } catch (RemoteException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void removeMonitoringStation(StationRMI station) throws RemoteException {
        stations.remove(station);
        System.out.println("Station Disconnected");
        setConnectedMonitoringStationsCount();
    }

    @Override
    public ArrayList<String> getSensorReadings(String sensor_name) throws RemoteException {
        ArrayList<String> readings = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(sensor_name+".txt"))){
            for(String line; (line=br.readLine())!=null;){
                readings.add(line);
            }
        }catch(IOException ex){
            return null;
        }
        return readings;
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
    private ArrayList<String> sensors;
    public SensorThread(Socket clientSocket, ArrayList<String> sensors){
        this.clientSocket = clientSocket;
        this.sensors = sensors;
    }
    
    @Override
    public void run(){
        try {
            input = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        while(true){
            Server.updateConnectedSensors();
            Server.updateConnectedSensorsCount();
            try {
                String[] object = (String[]) input.readObject();
//                System.out.println(object[0]);
                if(object.length==1){
                    if(object[0].startsWith("CLOSE")){
                        String sensor_name = object[0].split("_")[1]+"_"+object[0].split("_")[2];
                        sensors.remove(sensor_name);
                        Server.updateConnectedSensors();
                        Server.updateConnectedSensorsCount();
                    }    
                }
                else if(object.length==2){
                    location = object[0];
                    type = object[1];
                    String sensor_name = location+"_"+type;
                    if(!sensors.contains(sensor_name))
                        sensors.add(sensor_name);
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
                                break;
                            case "temperature":
                                if(value>35.00 || value<20.00){
                                    for(StationRMI station: Server.stations)
                                        station.alert("Alert: Temperature "+value+" in "+location+"\n");
                                }
                                break;
                        }
                        fileOutput = new FileWriter(location+"_"+type+".txt",true);
                        fileOutput.write(jsonObject.toString()+"\n");
                        fileOutput.close();
                    }
                    for(StationRMI station : Server.stations){
                            station.addLog("Log: Received readings from "+location+"-"+type);
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