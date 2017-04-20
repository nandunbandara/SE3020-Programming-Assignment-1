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
import java.util.concurrent.TimeUnit;
/**
 *
 * @author ntban_000
 */
public class SensorApplication {
    private static Socket clientSock;
    private static ObjectOutputStream clientOut;
    private static Sensor sensor;
    public static void main(String[] args) throws InterruptedException, IOException{
        System.out.println("Enter location: ");
        Scanner input = new Scanner(System.in);
        String location = input.nextLine();
        System.out.println("Available Sensor Types:\n1.\tAir Pressure\n2.\tHumidity\n3.\tRainfall\n4.\tTemperature\n");
        System.out.println("Select type of sensor: ");
        int type = input.nextInt();
        
        switch(type){
            case 1:
                sensor = new AirPressureSensor(location);
                break;
            case 2:
                sensor = new HumiditySensor(location);
                break;
            case 3:
                sensor = new RainfallSensor(location);
                break;
            case 4:
                sensor = new Temperature(location);
                break;
            default:
                System.out.println("Please enter a valid option");
                return;
                
        }
        
        try{
            clientSock = new Socket("127.0.0.1",6666);
            clientOut = new ObjectOutputStream(clientSock.getOutputStream());
        }catch(Exception ex){
            ex.printStackTrace();
        }
        
        System.out.println("Connected to server");
        
        String[] config = {location,type==1?"airepressure":(type==2?"humidity":"rainfall")};
        Random rand = new Random();
        Gson gson = new Gson();
        int count = 0;
        String readings[] = new String[12];
        if(clientSock != null && clientOut != null){
            while(true){
                clientOut.writeObject(config);
                for(int i=0;i<12;i++){
                    sensor.setValue(rand.nextDouble()*100);
                    String json = gson.toJson(sensor);
                    readings[i]=json;
                    TimeUnit.SECONDS.sleep(1);
                }
                try{
                    clientOut.writeObject(readings);
                    //TimeUnit.SECONDS.sleep(1);
                }catch(IOException e){
                    System.out.println("Connection to the Server broke!");
                    break;
                }
            }
        }
        clientOut.close();
        clientSock.close();
    }

}
