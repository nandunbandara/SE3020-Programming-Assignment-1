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
import security.AESEncryption;
/**
 *
 * @author ntban_000
 */
public class Server extends UnicastRemoteObject implements ServerRMI, Runnable{
//    private List<GSON>
    public static ArrayList<StationRMI> stations;
    public static ArrayList<String> sensors; 
    public static HashMap<String, SensorThread> sensorThreads;
    private static FileWriter fileOutput;
    private int server_com_port;
    private static int rmi_port;
    public Server() throws RemoteException{
        //load configurations
        System.out.println("Loading configurations...");
        try(BufferedReader bf = new BufferedReader(new FileReader("server.conf"))){
            for(String input; (input=bf.readLine())!=null;){
                if(input.startsWith("@server_com")){
                    //set port for socket communication
                   this.server_com_port = Integer.parseInt(input.split(" ")[1]);
                }else if(input.startsWith("@rmi_port")){
                    //set port for rmi
                    this.rmi_port = Integer.parseInt(input.split(" ")[1]);
                }else{
                    //set default configurations
                    this.server_com_port=6666;
                    this.rmi_port=1099;
                }
            }
        }catch(Exception e){
            //set default configurations
            this.server_com_port=6666;
            this.rmi_port=1099;
        }
        //display loaded configurations
        System.out.println("Socket communication port: "+server_com_port);
        System.out.println("RMI port: "+rmi_port);
    }
    
    public static void main(String[] args) throws RemoteException{
        
        sensors = new ArrayList<>();
        stations = new ArrayList<StationRMI>();
        sensorThreads = new HashMap<>();
        
        //RMI
        try {
            Server server = new Server();
            Registry reg = LocateRegistry.createRegistry(rmi_port);
            reg.rebind("server",server);
            Thread thread = new Thread(server);
            thread.start();
        } catch (RemoteException ex) {
            System.out.println("Error: Server status");
        }
        new Thread(new Runnable(){
            @Override
            public void run() {
               String input = new Scanner(System.in).nextLine();
               if(input.startsWith("@add")){
                   //add new user
                   String uname = input.split(" ")[1];
                   String pwd = input.split(" ")[2];
                   addUser(uname, pwd);
               }else if(input.startsWith("@remove")){
                   //remove existing user
                   String uname = input.split(" ")[1];
                   removeUser(uname);
               }else if(input.equals("exit")){
                   //shut down server
                   System.out.println("Shutting down server...");
                   System.exit(0);
               }
            }
        }).start();
    }

    @Override
    public void run() {
        ServerSocket serverSock = null;
        Socket clientSock = null;
        ObjectInputStream input = null;
        try{
            //create a server socket to listen on for connections
            serverSock = new ServerSocket(server_com_port);
        }catch(IOException e){
            System.out.println("Error: Server initialization failed");
        }
        System.out.println("Server started...\nListening on port "+server_com_port+" for sensors");
        System.out.println("Type 'exit' to shutdown server");
        while(true){
            try {
                clientSock = serverSock.accept();
                SensorThread sensor = new SensorThread(clientSock,sensors);
                sensor.start();
                String sensor_name = sensor.getLocation()+"-"+sensor.getType();
                //update the connected sensors list and the count of connected sensors
                updateConnectedSensors();
                updateConnectedSensorsCount();
            } catch (IOException ex) {
                
            }
        }
    }
    
    //add a new monitoring station
     @Override
    public void addStation(StationRMI station) throws RemoteException {
        Server.stations.add(station);
    }
    
    //update the list of connected sensors
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
    
    //update the connected sensors count
    public static void updateConnectedSensorsCount(){
        for(StationRMI station: stations){
            try{
                station.setConnectedSensors(sensors.size());
            }catch(RemoteException e){
                System.out.println(e);
            }catch(NullPointerException ex){
                
            }
        }
    }
    
    //add a new sensor
    public static void setSensors(String name){
        sensors.add(name);
    }

    @Override
    public void setSensorCount() throws RemoteException {
        for(StationRMI station: stations){
            try{
                station.setConnectedSensors(sensors.size());
            }catch(RemoteException e){
                System.out.println(e);
            }
        }
    }

    //set the list of connected sensors
    @Override
    public void setConnectedSensors() throws RemoteException {
        for(StationRMI station : stations){
            try {
                station.setSensorList(sensors);
            } catch (RemoteException ex) {
                
            } 
        }
    }

    //set the count of connected monitoring stations
    @Override
    public void setConnectedMonitoringStationsCount() throws RemoteException {
        for(StationRMI station : stations){
            try {
                station.setMonitoringStationsCount(stations.size());
            } catch (RemoteException ex) {
            }
        }
    }

    //remove a monitoring station when it disconnects
    @Override
    public void removeMonitoringStation(StationRMI station) throws RemoteException {
        stations.remove(station);
        System.out.println("Station Disconnected");
        setConnectedMonitoringStationsCount();
    }

    //read from sensor data files and return an array of JSON strings
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

    //authenticate users logging in through monitoring stations
    @Override
    public boolean authenticateUser(String uname, String pwd) throws RemoteException {
        String encrypted_uname_check;
        String encrypted_pwd_check;
        try {
            encrypted_uname_check = AESEncryption.encrypt(uname);
            encrypted_pwd_check = AESEncryption.encrypt(pwd);
        } catch (Exception ex) {
            System.out.println("Error: "+ex);
            return false;
        }
        //read users.conf
        try(BufferedReader input = new BufferedReader(new FileReader("users.conf"))){
            for(String inputLine; (inputLine=input.readLine())!=null;){
                if(inputLine.startsWith("#user")&&inputLine.split(" ")[1].equals(encrypted_uname_check)){
                    if(inputLine.split(" ")[2].equals(encrypted_pwd_check)){
                        return true;
                    }
                    else
                        return false;
                }
            }
        }catch(Exception ex){
            System.out.println("Error: "+ex);
            return false;
        }
        return false;
    }
    
    //Add new user for monitoring stations
    private static void addUser(String uname, String pwd){
        try {
            String encryptedUName = AESEncryption.encrypt(uname);
            String encryptedPwd = AESEncryption.encrypt(pwd);
            fileOutput = new FileWriter("users.conf",true);
            fileOutput.write("#user "+encryptedUName+" "+encryptedPwd+"\n");
            System.out.println("User added:"+uname);
            fileOutput.close();
        } catch (Exception ex) {
            System.out.println("Error: Can not add user" + ex);
        }
    }
    
    //Remove user
    private static void removeUser(String uname){
        try(BufferedReader bf = new BufferedReader(new FileReader("users.conf"))){
            String encrypted_uname_check = AESEncryption.encrypt(uname);
            ArrayList<String> users = new ArrayList<>();
            for(String inputLine; (inputLine=bf.readLine())!=null;){
                if(inputLine.startsWith("#user")&&!inputLine.split(" ")[1].equals(encrypted_uname_check)){
                    users.add(inputLine);
                }
            }
            fileOutput = new FileWriter("users.conf");
            for(String user : users)
                fileOutput.write(user+"\n");
            fileOutput.close();
            System.out.println("User removed: "+uname);
        }catch(Exception ex){
            System.out.println("Error: Could not remove user - "+ex);
        }
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
                if(object.length==1){
                    if(object[0].startsWith("CLOSE")){
                        String sensor_name = object[0].split("_")[1]+"_"+object[0].split("_")[2];
                        sensors.remove(sensor_name);
                        Server.updateConnectedSensors();
                        Server.updateConnectedSensorsCount();
                        for(StationRMI station : Server.stations)
                            station.alert("Sensor disconnected: "+location+"_"+type+"\n");
                    }    
                }
                else if(object.length==2){
                    location = object[0];
                    type = object[1];
                    String sensor_name = location+"_"+type;
                    if(!sensors.contains(sensor_name)){
                        sensors.add(sensor_name);
                        for(StationRMI station : Server.stations)
                            station.addLog("Sensor connected: "+location+"_"+type);
                    }
                }else{
                    //write to file
                    for(int i=0;i<object.length;i++){
                        JsonObject jsonObject = new JsonParser().parse(object[i]).getAsJsonObject();
                        location = jsonObject.get("location").toString().split("\"")[1];
                        type = jsonObject.get("type").toString().split("\"")[1];
                        //alert monitoring stations
                        double value = Double.parseDouble(jsonObject.get("value").toString());
                        switch(type){
                            case "rainfall":
                                if(value>20.00){
                                  //alert when rainfall is greater than 20 mm
                                  for(StationRMI station : Server.stations){
                                      station.alert("Alert: Rainfall "+value+" in "+location+"\n");
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
                        //write the received sensor data into data files
                        fileOutput = new FileWriter(location+"_"+type+".txt",true);
                        fileOutput.write(jsonObject.toString()+"\n");
                        fileOutput.close();
                    }
                    //send logs to all monitoring stations 
                    //notify them for received sensor readings
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
            System.out.println("Error: Can not close the client port!");
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