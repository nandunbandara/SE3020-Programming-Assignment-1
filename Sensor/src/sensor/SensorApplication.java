/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sensor;

import java.net.*;
import java.io.*;
import java.util.*;
import com.google.gson.*;
/**
 *
 * @author ntban_000
 */
public class SensorApplication {
    public static void main(String[] args){
        System.out.println("Enter location: ");
        Scanner input = new Scanner(System.in);
        String location = input.nextLine();
        
        Sensor sensor = new Sensor(location);
        //client socket for connection
        Socket clientSock = null;
        ObjectOutputStream clientOut = null;
        try{
            clientSock = new Socket("127.0.0.1",6666);
            clientOut = new ObjectOutputStream(clientSock.getOutputStream());
        }catch(Exception ex){
            ex.printStackTrace();
        }
        
        System.out.println("Connected to server");
        //Timer
        Timer timer = new Timer();
        Random rand = new Random();
        Gson gson = new Gson();
        if(clientSock != null && clientOut != null){
            while(true){
                sensor.setRainfall(rand.nextDouble()*100);
                sensor.setHumidity(rand.nextDouble()*100);
                sensor.setAirPressure(rand.nextDouble()*100);
                String json = gson.toJson(sensor);
                try{
                    clientOut.writeObject(json);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
