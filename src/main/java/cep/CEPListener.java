package cep;

import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class CEPListener implements UpdateListener {
    private String id;
    public CEPListener(String id) {
        this.id = id;
    }
    public void update(EventBean[] newData, EventBean[] oldData) {
        /*if (id.equalsIgnoreCase("ife")) {
            System.out.println("(" + id + ") " + "Event matched: " + newData[0].getUnderlying());
        }*/
        /*else if (id.equalsIgnoreCase("lhDestinationCoordinates")){
            System.out.println("(" + id + ") " + "Event matched: " + newData[0].getUnderlying());
        }*/
        /*else if (id.equalsIgnoreCase("Distance")){
            System.out.println("(" + id + ") " + "Event matched: " + newData[0].getUnderlying());
        }
        else if (id.equalsIgnoreCase("Speed")){
            System.out.println("(" + id + ") " + "Event matched: " + newData[0].getUnderlying());
        }
        else*/ if (id.equalsIgnoreCase("ETA")){
            System.out.println("(" + id + ") " + "Event matched: " + newData[0].getUnderlying());
        }
        else if (id.equalsIgnoreCase("OnBoardSights")){
        System.out.println("(" + id + ") " + "Event matched: " + newData[0].getUnderlying());
        }
        /*else if (id.equalsIgnoreCase("CurrentWeather")){
            System.out.println("(" + id + ") " + "Event matched: " + newData[0].getUnderlying());
        }*/
        /*else if (id.equalsIgnoreCase("lhDestinationCity")){
            System.out.println("(" + id + ") " + "Event matched: " + newData[0].getUnderlying());
        }*/
        else if (id.equalsIgnoreCase("DestinationWeatherSight")){
            System.out.println("(" + id + ") " + "Event matched: " + newData[0].getUnderlying());
        }
        else if (id.equalsIgnoreCase("connectionFlightGates")){
            System.out.println("(" + id + ") " + "Event matched: " + newData[0].getUnderlying());
        }
        /*else if (id.equalsIgnoreCase("Gates")){
            System.out.println("(" + id + ") " + "Event matched: " + newData[0].getUnderlying());
        }*/
        else if (id.equalsIgnoreCase("loungeSelector")){
            System.out.println("(" + id + ") " + "Event matched: " + newData[0].getUnderlying());
        }
        /*else if (id.equalsIgnoreCase("connectionFlightsDestination")){
            System.out.println("(" + id + ") " + "Event matched: " + newData[0].getUnderlying());
        }
        else if (id.equalsIgnoreCase("connectionFlightsGoodWeather")){
            System.out.println("(" + id + ") " + "Event matched: " + newData[0].getUnderlying());
        }*/
        else if (id.equalsIgnoreCase("advertiseGoodWeatherConnectionFlight")){
            System.out.println("(" + id + ") " + "Event matched: " + newData[0].getUnderlying());
        }
    }
}
