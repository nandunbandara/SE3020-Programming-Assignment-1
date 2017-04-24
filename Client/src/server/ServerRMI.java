/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.rmi.*;
import java.util.ArrayList;
import station.StationRMI;

/**
 *
 * @author ntban_000
 */
public interface ServerRMI extends Remote{
    public void addStation(StationRMI station) throws RemoteException;
    public void setSensorCount() throws RemoteException;
    public void setConnectedSensors() throws RemoteException;
    public void setConnectedMonitoringStationsCount() throws RemoteException;
    public void removeMonitoringStation(StationRMI station) throws RemoteException;
    public ArrayList<String> getSensorReadings(String sensor_name) throws RemoteException;
    public boolean authenticateUser(String uname, String pwd) throws RemoteException;
}
