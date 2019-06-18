package lufthansa;

public class Lounge {

    private String name;
    private String location;
    private boolean restrooms;
    private boolean showers;
    private boolean faxMachine;
    private boolean wlan;

    public Lounge(String name, String location, boolean restrooms, boolean showers, boolean faxMachine, boolean wlan) {
        this.name = name;
        this.location = location;
        this.restrooms = restrooms;
        this.showers = showers;
        this.faxMachine = faxMachine;
        this.wlan = wlan;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isRestrooms() {
        return restrooms;
    }

    public void setRestrooms(boolean restrooms) {
        this.restrooms = restrooms;
    }

    public boolean isShowers() {
        return showers;
    }

    public void setShowers(boolean showers) {
        this.showers = showers;
    }

    public boolean isFaxMachine() {
        return faxMachine;
    }

    public void setFaxMachine(boolean faxMachine) {
        this.faxMachine = faxMachine;
    }

    public boolean isWlan() {
        return wlan;
    }

    public void setWlan(boolean wlan) {
        this.wlan = wlan;
    }
}
