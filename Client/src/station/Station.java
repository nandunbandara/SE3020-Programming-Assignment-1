/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package station;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import server.ServerRMI;
import java.rmi.*;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.FileReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 *
 * @author ntban_000
 */
public class Station extends UnicastRemoteObject implements StationRMI, Runnable {

    private static StationInterface stationInterface;
    private static ServerRMI server;
    private static Station station;
    private static String rmi_host;
    private static int rmi_port;
    
    public Station() throws RemoteException {
        System.out.println("Loading configurations...");
        try(BufferedReader bf = new BufferedReader(new FileReader("station.conf"))){
            for(String input; (input=bf.readLine())!=null;){
                if(input.startsWith("@rmi_port")){
                    //set port for socket communication
                   rmi_port = Integer.parseInt(input.split(" ")[1]);
                }else if (input.startsWith("@rmi_host")){
                    //set host
                    rmi_host = input.split(" ")[1];
                }
                else{
                    //set default configurations
                   rmi_port=1099;
                   rmi_host="localhost";
                }
            }
        }catch(Exception e){
             //set default configurations
             rmi_port=1099;
             rmi_host="localhost";
        }
        //display loaded configurations
        System.out.println("RMI: "+rmi_host+":"+rmi_port);
    }
    
    public static void main(String[] args) {
        //Remove monitoring station from the server's record on exit
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    server.removeMonitoringStation(station);
                } catch (RemoteException ex) {
                    Logger.getLogger(Station.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NullPointerException e) {
                    
                }
            }
        }));

        //Locate Registry and connect
        try {
            System.setSecurityManager(new RMISecurityManager());
            station = new Station();
            Registry reg = LocateRegistry.getRegistry(rmi_host, rmi_port);
            server = (ServerRMI) reg.lookup("server");
        } catch (ConnectException e) {
            javax.swing.JOptionPane.showMessageDialog(stationInterface, "Server is not running!"+e);
            System.exit(0);
        } catch (RemoteException e) {
            javax.swing.JOptionPane.showMessageDialog(stationInterface, e);
            System.exit(0);
        } catch (NotBoundException e) {
            javax.swing.JOptionPane.showConfirmDialog(stationInterface, e);
            System.exit(0);
        }

        //Login Popup
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JFrame frame = new JFrame();
        JPanel label = new JPanel(new GridLayout(0, 1, 2, 2));
        label.add(new JLabel("Username", SwingConstants.RIGHT));
        label.add(new JLabel("Password", SwingConstants.RIGHT));
        panel.add(label, BorderLayout.WEST);
        
        JPanel controls = new JPanel(new GridLayout(0, 1, 2, 2));
        JTextField username = new JTextField();
        controls.add(username);
        JPasswordField password = new JPasswordField();
        controls.add(password);
        panel.add(controls, BorderLayout.CENTER);
        
        JOptionPane.showMessageDialog(frame, panel, "login", JOptionPane.OK_CANCEL_OPTION);
        try {
            if (!server.authenticateUser(username.getText(), password.getText())) {
                JOptionPane.showMessageDialog(panel, "Could not validate user: " + username.getText());
                System.exit(0);
            } else {
                server.addStation(station);
                station.run();
            }
            //end login
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(panel, "Could not validate user: " + username.getText());
            System.exit(0);
        }

        //start thread to authenticate user every 5 seconds
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        //close user session on invalid credentials
                        if(!server.authenticateUser(username.getText(), password.getText())){
                            javax.swing.JOptionPane.showMessageDialog(stationInterface, "Error: User session expired!");
                            System.exit(1);
                        }
                        Thread.sleep(2000);
                    } catch (RemoteException ex) {
                        System.out.println("Could not validate user");
                    } catch (InterruptedException ex) {
                        System.out.println("Could not validate user");
                    }
                }
            }            
        }).start();
    }

    //Alerts from sensors method
    @Override
    public void alert(String alertText) throws RemoteException {
        Toolkit.getDefaultToolkit().beep();
        Document doc = stationInterface.getTextPane().getDocument();
        SimpleAttributeSet red = new SimpleAttributeSet();
        StyleConstants.setFontFamily(red, "Courier New Italic");
        StyleConstants.setForeground(red, Color.RED);
        
        try {
            doc.insertString(doc.getLength(), alertText, red);
        } catch (BadLocationException ex) {
            System.out.println("Error writing logs/alerts");
        }
        stationInterface.getTextPane().setCaretPosition(doc.getLength());
    }

    //Logging sensors
    @Override
    public void addLog(String logText) throws RemoteException {
        Document doc = stationInterface.getTextPane().getDocument();
        SimpleAttributeSet blue = new SimpleAttributeSet();
        StyleConstants.setFontFamily(blue, "Courier New Italic");
        StyleConstants.setForeground(blue, Color.BLUE);
        
        try {
            doc.insertString(doc.getLength(), logText + "\n", blue);
        } catch (BadLocationException ex) {
            System.out.println("Error writing logs/alerts");
        }
        stationInterface.getTextPane().setCaretPosition(doc.getLength());
    }
    
    @Override
    public void run() {
        try {
            stationInterface = new StationInterface();
            stationInterface.show();
            server.setSensorCount();
            server.setConnectedSensors();
            server.setConnectedMonitoringStationsCount();
        } catch (RemoteException ex) {
            Logger.getLogger(Station.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    //set the connected sensor count
    @Override
    public void setConnectedSensors(int count) throws RemoteException {
        stationInterface.setConnectedSensors(count);
    }
    
    //set the active sensors list
    @Override
    public void setSensorList(ArrayList<String> list) throws RemoteException {
        stationInterface.getConnectedSensorsList().setListData(list.toArray());
    }
    
    //set the connected monitoring stations count
    @Override
    public void setMonitoringStationsCount(int count) throws RemoteException {
        stationInterface.setConnectedMonitoringStations(count);
    }
    
    //get sensor readings from the server given a sensor name
    public static ArrayList<String> getSensorReadings(String name) {
        try {
            return server.getSensorReadings(name);
        } catch (RemoteException ex) {
            javax.swing.JOptionPane.showMessageDialog(stationInterface, "Error reading from server!");
        }
        return null;
    }
    
}
