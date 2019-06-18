package lufthansa;

class Airport {
    private String code;
    private Object[] coords;
    private Lounge[] lounges;

    public Airport(String code) {
        this(code, null, null);
    }

    private Airport(String code, Object[] coords, Lounge[] lounges) {
        this.code = code;
        this.coords = coords;
        this.lounges = lounges;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object[] getCoords() {
        return coords;
    }

    public void setCoords(Object[] coords) {
        this.coords = coords;
    }

    public Lounge[] getLounges() {
        return lounges;
    }

    public void setLounges(Lounge[] lounges) {
        this.lounges = lounges;
    }
}
