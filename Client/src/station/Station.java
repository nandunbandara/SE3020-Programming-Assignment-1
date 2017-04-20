/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package station;

import java.awt.Color;
import static java.awt.Color.red;
import server.ServerRMI;
import java.rmi.*;
import java.awt.Toolkit;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    public Station() throws RemoteException{
        
    }
    
    public static void main(String[] args){
        try{
            System.setSecurityManager(new RMISecurityManager());
            Registry reg = LocateRegistry.getRegistry("localhost",1009);
            ServerRMI server = (ServerRMI) reg.lookup("server");
//            ServerRMI server = (ServerRMI) Naming.lookup("rmi://localhost:1009/server");
            Station station = new Station();
            server.addStation(station);
            station.run();
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
    @Override
    public void alert(String alertText) throws RemoteException {
        Toolkit.getDefaultToolkit().beep();
        Document doc = stationInterface.getTextPane().getDocument();
        SimpleAttributeSet red = new SimpleAttributeSet();
        StyleConstants.setFontFamily(red, "Courier New Italic");
        StyleConstants.setForeground(red, Color.RED);
            //        try {
//            doc.insertString(doc.getLength(), alertText, null);
//        } catch (BadLocationException ex) {
//            Logger.getLogger(StationInterface.class.getName()).log(Level.SEVERE, null, ex);
//        }
        try{
            doc.insertString(doc.getLength(), alertText, red);
        } catch (BadLocationException ex) {
            Logger.getLogger(Station.class.getName()).log(Level.SEVERE, null, ex);
        }
        stationInterface.getTextPane().setCaretPosition(doc.getLength());
    }

    @Override
    public void addLog(String logText) throws RemoteException {
        Document doc = stationInterface.getTextPane().getDocument();
        SimpleAttributeSet blue = new SimpleAttributeSet();
        StyleConstants.setFontFamily(blue, "Courier New Italic");
        StyleConstants.setForeground(blue, Color.BLUE);
            //        try {
//            doc.insertString(doc.getLength(), logText, null);
//        } catch (BadLocationException ex) {
//            Logger.getLogger(StationInterface.class.getName()).log(Level.SEVERE, null, ex);
//        }
        try{
            doc.insertString(doc.getLength(), logText+"\n", blue);
        } catch (BadLocationException ex) {
            Logger.getLogger(Station.class.getName()).log(Level.SEVERE, null, ex);
        }
        stationInterface.getTextPane().setCaretPosition(doc.getLength());
    }

    @Override
    public void run() {
        stationInterface = new StationInterface();
        stationInterface.show();
    }

    @Override
    public void setConnectedSensors(int count) throws RemoteException {
        stationInterface.setConnectedSensors(count);
    }
    
    @Override
    public void setSensorList(ArrayList<String> list) throws RemoteException{
        stationInterface.getConnectedSensorsList().setListData(list.toArray());
    }
}
