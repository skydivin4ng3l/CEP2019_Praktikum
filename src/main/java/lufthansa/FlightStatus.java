package lufthansa;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

class FlightStatus {
    private String flightNumber;
    private Airport departure;
    private Airport arrival;
    private String departureGate;
    private String arrivalGate;

    public FlightStatus(String flightNumber, Airport departure, Airport arrival, String departureGate, String arrivalGate) {
        this.flightNumber = flightNumber;
        this.departure = departure;
        this.arrival = arrival;
        this.arrivalGate = departureGate;
        this.arrivalGate = arrivalGate;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public Airport getDeparture() {
        return departure;
    }

    public void setDeparture(Airport departure) {
        this.departure = departure;
    }

    public String getDepartureGate() {
        return departureGate;
    }

    public void setDepartureGate(String departureGate) {
        this.departureGate = departureGate;
    }

    public Airport getArrival() {
        return arrival;
    }

    public void setArrival(Airport arrival) {
        this.arrival = arrival;
    }

    public String getArrivalGate() {
        return arrivalGate;
    }

    public void setArrivalGate(String arrivalGate) {
        this.arrivalGate = arrivalGate;
    }
}
