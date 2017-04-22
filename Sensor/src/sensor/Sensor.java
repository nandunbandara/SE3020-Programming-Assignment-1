/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sensor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author ntban_000
 */
public class Sensor{
    private String location;
    private String type;
    private String datetime;
    private double value;
    private String unit;
    
    public Sensor(String location){
        this.location = location;
    }
    public String getLocation(){
        return location;
    }
    
    public void setLocation(String location){
        this.location = location;
    }
    
    public void setType(String type){
        this.type = type;
    }
    
    public String getType(){
        return type;
    }
    
    public void setValue(double value){
        this.value = value;
        DateFormat dateFormat = new SimpleDateFormat("dd.MM hh:mm a");
        Date date = new Date();
        this.datetime = dateFormat.format(date);
    }
    
    public double getValue(){
        return value;
    }
    
    public void setUnit(String unit){
        this.unit = unit;
    }
    
    public String getUnit(){
        return unit;
    }
}
