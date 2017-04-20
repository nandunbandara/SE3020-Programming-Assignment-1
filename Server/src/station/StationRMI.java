/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package station;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 *
 * @author ntban_000
 */
public interface StationRMI extends Remote{
    public void alert(String alertText) throws RemoteException;
    public void addLog(String logText) throws RemoteException;
    public void setConnectedSensors(int count) throws RemoteException;
    public void setSensorList(ArrayList<String> list) throws RemoteException;
}
