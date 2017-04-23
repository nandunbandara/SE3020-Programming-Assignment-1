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
    private static int type;
    public static void main(String[] args) throws InterruptedException, IOException{
        //remove the client from server records on exit
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    clientOut.writeObject(new String[]{"CLOSE_"+sensor.getLocation()+"_"+(type==1?"airpressure":(type==2?"humidity":"rainfall"))});
                } catch (IOException ex) {
                    System.out.println("Error closing the sensor");
                }
            }
        }));
        //prompt user for location and the type of sensor
        System.out.print("Enter location: ");
        Scanner input = new Scanner(System.in);
        String location = input.nextLine();
        System.out.println("Available Sensor Types:\n1.\tAir Pressure\n2.\tHumidity\n3.\tRainfall\n4.\tTemperature\n");
        System.out.print("Select type of sensor: ");
        type = input.nextInt();
        //set the type of sensor based on the option selected by the user
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
        
        //create socket and object streaming object for client communications
        try{
            clientSock = new Socket("127.0.0.1",6666);
            clientOut = new ObjectOutputStream(clientSock.getOutputStream());
        }catch(Exception ex){
            ex.printStackTrace();
        }
        
        System.out.println("Connected to server");
        //new thread to read the exit command from the user
        //and terminate the sensor application
        new Thread(new Runnable(){
            @Override
            public void run() {
                String code;
                do{
                System.out.println("Type 'exit' to stop the sensor");
                code = new Scanner(System.in).nextLine();
                if(code.equals("exit"))
                    System.exit(0);
                }while(!code.equals("exit"));
            }
            
        }).start();
        
        //send configuration details to server: location and type
        String[] config = {location,type==1?"airpressure":(type==2?"humidity":(type==3?"rainfall":"temperature"))};
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
                    TimeUnit.MINUTES.sleep(5);  
                }
                try{
                    clientOut.writeObject(readings);
                }catch(IOException e){
                    System.out.println("Connection to the Server broke!");
                    break;
                }
            }
        }
        try{
            clientOut.close();
            clientSock.close();
        }catch(Exception e){
            
        }
        
    }

}
