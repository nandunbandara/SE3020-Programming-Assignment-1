/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sensor;

/**
 *
 * @author ntban_000
 */
public class Sensor{
    private String location;
    private String type;
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
