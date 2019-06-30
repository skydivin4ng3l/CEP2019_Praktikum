package booking;

public class Booking {
    private String flightNumber;
    private CabinClass cabinClass;
    private String passengerName;
    private String connectionFlightNumber;

    public Booking(String flightNumber, CabinClass cabinClass, String passengerName, String connectionFlightNumber) {
        this.flightNumber = flightNumber;
        this.cabinClass = cabinClass;
        this.passengerName = passengerName;
        this.connectionFlightNumber = connectionFlightNumber;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public CabinClass getCabinClass() {
        return cabinClass;
    }

    public void setCabinClass(CabinClass cabinClass) {
        this.cabinClass = cabinClass;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getConnectionFlightNumber() {
        return connectionFlightNumber;
    }

    public void setConnectionFlightNumber(String connectionFlightNumber) {
        this.connectionFlightNumber = connectionFlightNumber;
    }
}
