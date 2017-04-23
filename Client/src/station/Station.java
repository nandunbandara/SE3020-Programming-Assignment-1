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
public class Station extends UnicastRemoteObject implements StationRMI, Runnable{
    private static StationInterface stationInterface;
    private static ServerRMI server;
    private static Station station;
    public Station() throws RemoteException{
        
    }
    
    public static void main(String[] args){
        //Remove monitoring station from the server's record on exit
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    server.removeMonitoringStation(station);
                } catch (RemoteException ex) {
                    Logger.getLogger(Station.class.getName()).log(Level.SEVERE, null, ex);
                } catch(NullPointerException e){
                    
                }
            }
        }));
        
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
        if(!(username.getText().equals("user"))&&!(new String(password.getPassword()).equals("password"))){
            System.exit(0);
        }
        //end login
        
        //Locate Registry and connect
        try{
            System.setSecurityManager(new RMISecurityManager());
            Registry reg = LocateRegistry.getRegistry("localhost",1009);
            server = (ServerRMI) reg.lookup("server");

            
            station = new Station();
            server.addStation(station);
            station.run();
        }catch(ConnectException e){
            javax.swing.JOptionPane.showMessageDialog(stationInterface, "Server is not running!");
            System.exit(0);
        }catch(RemoteException e){
            javax.swing.JOptionPane.showMessageDialog(stationInterface, e);
            System.exit(0);
        }catch(NotBoundException e){
            javax.swing.JOptionPane.showConfirmDialog(stationInterface, e);
            System.exit(0);
        }
    }
    
    //Alerts from sensors method
    @Override
    public void alert(String alertText) throws RemoteException {
        Toolkit.getDefaultToolkit().beep();
        Document doc = stationInterface.getTextPane().getDocument();
        SimpleAttributeSet red = new SimpleAttributeSet();
        StyleConstants.setFontFamily(red, "Courier New Italic");
        StyleConstants.setForeground(red, Color.RED);
            
        try{
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

        try{
            doc.insertString(doc.getLength(), logText+"\n", blue);
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

    @Override
    public void setConnectedSensors(int count) throws RemoteException {
        stationInterface.setConnectedSensors(count);
    }
    
    @Override
    public void setSensorList(ArrayList<String> list) throws RemoteException{
        stationInterface.getConnectedSensorsList().setListData(list.toArray());
    }

    @Override
    public void setMonitoringStationsCount(int count) throws RemoteException {
        stationInterface.setConnectedMonitoringStations(count);
    }
    
    public static ArrayList<String> getSensorReadings(String name){
        try {
            return server.getSensorReadings(name);
        } catch (RemoteException ex) {
            javax.swing.JOptionPane.showMessageDialog(stationInterface, "Error reading from server!");
        }
        return null;
    }
    
}
