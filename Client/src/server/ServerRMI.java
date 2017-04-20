/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.rmi.*;
import station.StationRMI;

/**
 *
 * @author ntban_000
 */
public interface ServerRMI extends Remote{
    public void addStation(StationRMI station) throws RemoteException;
}
