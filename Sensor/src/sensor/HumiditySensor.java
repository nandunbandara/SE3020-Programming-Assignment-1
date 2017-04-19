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
public class HumiditySensor extends Sensor{
    public HumiditySensor(String location){
        super(location);
        super.setUnit("%");
        super.setType("humidity");
    }
}
