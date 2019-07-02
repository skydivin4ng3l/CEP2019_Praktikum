package lufthansa;

public class ConnectionFlight {
    private String flightNumber;
    private String connectionFlightNumber;

    public ConnectionFlight(String flightNumber, String connectionFlightNumber){
        this.flightNumber = flightNumber;
        this.connectionFlightNumber = connectionFlightNumber;
    }

    public void setConnectionFlightNumber(String connectionFlightNumber) {
        this.connectionFlightNumber = connectionFlightNumber;
    }

    public String getConnectionFlightNumber() {
        return connectionFlightNumber;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }
}
