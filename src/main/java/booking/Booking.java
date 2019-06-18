package booking;

public class Booking {
    private String flightNumber;
    private CabinClass cabinClass;
    private String passengerName;

    public Booking(String flightNumber, CabinClass cabinClass, String passengerName) {
        this.flightNumber = flightNumber;
        this.cabinClass = cabinClass;
        this.passengerName = passengerName;
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

}
