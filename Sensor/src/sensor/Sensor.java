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
public class Sensor implements Rainfall, Humidity, AirPressure{
    private String location;
    private double rainfall;
    private double humidity;
    private double airPressure;
    
    public Sensor(String location){
        this.location = location;
    }
    public String getLocation(){
        return location;
    }
    
    public void setLocation(String location){
        this.location = location;
    }
    
    @Override
    public double getRainfall() {
        return rainfall;
    }

    @Override
    public void setRainfall(double rainfall) {
        this.rainfall = rainfall;
    }

    @Override
    public double getHumidity() {
        return humidity;
    }

    @Override
    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    @Override
    public double getAirPressure() {
        return airPressure;
    }

    @Override
    public void setAirPressure(double airPressure) {
        this.airPressure = airPressure;
    }
}
