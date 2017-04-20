/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package station;

import server.ServerRMI;
import java.rmi.*;
import java.awt.Toolkit;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
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
        try {
            doc.insertString(doc.getLength(), alertText, null);
        } catch (BadLocationException ex) {
            Logger.getLogger(StationInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void addLog(String logText) throws RemoteException {
        Document doc = stationInterface.getTextPane().getDocument();
        try {
            doc.insertString(doc.getLength(), logText, null);
        } catch (BadLocationException ex) {
            Logger.getLogger(StationInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        stationInterface = new StationInterface();
        stationInterface.show();
    }
}
