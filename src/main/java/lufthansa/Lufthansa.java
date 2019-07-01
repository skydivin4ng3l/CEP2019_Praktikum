package lufthansa;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Lufthansa {
    private static String baseUrl = "https://api.lufthansa.com/v1/";
    // Enter your client id here
    private static String clientId = "";
    // Enter your client secret here
    private static String clientSecret = "";
    private static String bearer = "";

    private static ArrayList<String> flightNumberBlacklist;
    private static ArrayList<String> airportBlacklist;
    private static ArrayList<String> loungeBlacklist;
    private static HashMap<String, FlightStatus> flightStatuses;
    private static HashMap<String, Airport> airports;

    static {
        bearer = requestToken();
        flightNumberBlacklist = new ArrayList<>();
        airportBlacklist = new ArrayList<>();
        loungeBlacklist = new ArrayList<>();
        flightStatuses = new HashMap<>();
        airports = new HashMap<>();
    }

    public static String getDepartureAirportCode(String flightNumber) {
        if (flightNumberBlacklist.contains(flightNumber)) {
            return null;
        }
        FlightStatus status = flightStatuses.get(flightNumber);
        if (status != null) {
            return status.getDeparture().getCode();
        }
        status = getFlightStatus(flightNumber);
        if (status != null) {
            return status.getDeparture().getCode();
        }
        return null;
    }

    public static String getArrivalAirportCode(String flightNumber) {
        if (flightNumberBlacklist.contains(flightNumber)) {
            return null;
        }
        FlightStatus status = flightStatuses.get(flightNumber);
        if (status != null) {
            return status.getArrival().getCode();
        }
        status = getFlightStatus(flightNumber);
        if (status != null) {
            return status.getArrival().getCode();
        }
        return null;
    }
    // Gates may be changed midflight, currently this would not be covered that
    public static String getArrivalAirportGate(String flightNumber) {
        if (flightNumberBlacklist.contains(flightNumber)) {
            return null;
        }
        FlightStatus status = flightStatuses.get(flightNumber);
        if (status != null) {
            return status.getArrivalGate();
        }
        status = getFlightStatus(flightNumber);
        if (status != null) {
            return status.getArrivalGate();
        }
        return null;
    }

    public static String getDepartureAirportGate(String flightNumber) {
        if (flightNumberBlacklist.contains(flightNumber)) {
            return null;
        }
        FlightStatus status = flightStatuses.get(flightNumber);
        if (status != null) {
            return status.getDepartureGate();
        }
        status = getFlightStatus(flightNumber);
        if (status != null) {
            return status.getDepartureGate();
        }
        return null;
    }

    public static String getArrivalTime(String flightNumber) {
        if (flightNumberBlacklist.contains(flightNumber)) {
            return null;
        }
        FlightStatus status = flightStatuses.get(flightNumber);
        if (status != null) {
            return status.getArrivalTime();
        }
        status = getFlightStatus(flightNumber);
        if (status != null) {
            return status.getArrivalTime();
        }
        return null;
    }

    public static String getDepartureTime(String flightNumber) {
        if (flightNumberBlacklist.contains(flightNumber)) {
            return null;
        }
        FlightStatus status = flightStatuses.get(flightNumber);
        if (status != null) {
            return status.getDepartureTime();
        }
        status = getFlightStatus(flightNumber);
        if (status != null) {
            return status.getDepartureTime();
        }
        return null;
    }

    public static String getConnectionFlight(String flightNumber) {
        if (flightNumberBlacklist.contains(flightNumber)){
            return null;
        }
        String arrivalAirportCode = getArrivalAirportCode(flightNumber);
        String arrivalTime = getArrivalTime(flightNumber);
        String statusAsJson = get("operations/flightstatus/departures/" + arrivalAirportCode + "/" + arrivalTime +"?limit=1" );
        if (statusAsJson != null) {
            String connectionFlightNumber = getDepartingFlightNumberFromJson(statusAsJson);
            return connectionFlightNumber;
        }
        //Doesn't make sense here to block cause the reason is mainly because there is no connectionflight
        // flightNumberBlacklist.add(flightNumber);
        return null;
    }

    private static FlightStatus getFlightStatus(String flightNumber) {
        String statusAsJson = get("operations/flightstatus/" + flightNumber + "/" + LocalDate.now().toString());
        if (statusAsJson != null) {
            String   departureAirportCode = getDepartureAirportFromJson(statusAsJson);
            if (!airports.containsKey(departureAirportCode)) {
                Airport airport = new Airport(departureAirportCode);
                airports.put(departureAirportCode, airport);
            }
            String arrivalAirportCode = getArrivalAirportFromJson(statusAsJson);
            if (!airports.containsKey(arrivalAirportCode)) {
                Airport airport = new Airport(arrivalAirportCode);
                airports.put(arrivalAirportCode, airport);
            }
            String departureAirportGate = getDepartureAirportGateFromJson(statusAsJson);
            String arrivalAirportGate = getArrivalAirportGateFromJson(statusAsJson);

            String departureTime = getDepartureTimeFromJson(statusAsJson);
            String arrivalTime = getArrivalTimeFromJson(statusAsJson);

            FlightStatus status = new FlightStatus(flightNumber, airports.get(departureAirportCode), airports.get(arrivalAirportCode), departureAirportGate, departureTime, arrivalAirportGate, arrivalTime );
            flightStatuses.put(flightNumber, status);
            return status;
        }
        flightNumberBlacklist.add(flightNumber);
        return null;
    }

    public static Object[] getDepartureAirportCoords(String flightNumber) {
        String departureAirportCode = getDepartureAirportCode(flightNumber);
        if (departureAirportCode != null) {
            if (airportBlacklist.contains(departureAirportCode)) {
                return null;
            }
            Airport airport = airports.get(departureAirportCode);
            if (airport.getCoords() != null) {
                return airport.getCoords();
            }
            String airportAsJson = get("references/airports/" + departureAirportCode);
            if (airportAsJson != null) {
                Object[] departureCoords = getDepartureCoordsFromJson(airportAsJson);
                airport.setCoords(departureCoords);
                return departureCoords;
            }
            airportBlacklist.add(departureAirportCode);
        }
        return null;
    }

    public static Object[] getArrivalAirportCoords(String flightNumber) {
        String arrivalAirportCode = getArrivalAirportCode(flightNumber);
        if (arrivalAirportCode != null) {
            if (airportBlacklist.contains(arrivalAirportCode)) {
                return null;
            }
            Airport airport = airports.get(arrivalAirportCode);
            if (airport.getCoords() != null) {
                return airport.getCoords();
            }
            String airportAsJson = get("references/airports/" + arrivalAirportCode);
            if (airportAsJson != null) {
                Object[] arrivalCoords = getArrivalCoordsFromJson(airportAsJson);
                airport.setCoords(arrivalCoords);
                return arrivalCoords;
            }
            airportBlacklist.add(flightNumber);
        }
        return null;
    }

    public static LinkedHashMap<String, Class> getArrivalAirportCoordsMetadata() {
        LinkedHashMap<String, Class> propertyNames = new LinkedHashMap<String, Class>();
        propertyNames.put("destLat", double.class);
        propertyNames.put("destLong", double.class);
        return propertyNames;
    }

    public static LinkedHashMap<String, Class> getDepartureAirportCoordsMetadata() {
        LinkedHashMap<String, Class> propertyNames = new LinkedHashMap<String, Class>();
        propertyNames.put("depLat", double.class);
        propertyNames.put("depLong", double.class);
        return propertyNames;
    }

    public static Lounge[] getAirportLounges(String airportCode) {
        if (airportCode == null) return null;
        if (loungeBlacklist.contains(airportCode)) {
            return null;
        }

        Airport airport = airports.get(airportCode);

        if (airport == null) {
            airport = new Airport(airportCode);
            airports.put(airportCode, airport);
        }
        Lounge[] loungeList = airport.getLounges();
        if (loungeList != null) {
            return loungeList;
        }
        String loungeListAsJson = get("offers/lounges/" + airportCode);
        if (loungeListAsJson != null) {
            loungeList = getLoungeListFromJson(loungeListAsJson);
            airport.setLounges(loungeList);
            return loungeList;
        }
        loungeBlacklist.add(airportCode);
        return null;
    }

    private static Lounge[] getLoungeListFromJson(String loungeListAsJson) {
        JSONObject obj = new JSONObject(loungeListAsJson);
        Lounge[] loungeList = null;
        if (((JSONObject) ((JSONObject) obj.get("LoungeResource")).get("Lounges")).get("Lounge") instanceof JSONObject) {
            JSONObject loungeObject = (JSONObject) ((JSONObject) ((JSONObject) obj.get("LoungeResource")).get("Lounges")).get("Lounge");
            loungeList = new Lounge[1];
            String name = getLoungeNameFromJson(loungeObject);
            String location = getLoungeLocationFromJson(loungeObject);
            boolean restrooms = getLoungeRestroomsFromJson(loungeObject);
            boolean showers = getLoungeShowersFromJson(loungeObject);
            boolean faxMachine = getLoungeFaxMachineFromJson(loungeObject);
            boolean wlan = getLoungeWlanFromJson(loungeObject);
            Lounge lounge = new Lounge(name, location, restrooms, showers, faxMachine, wlan);
            loungeList[0] = lounge;
        } else {
            JSONArray lounges = (JSONArray) ((JSONObject) ((JSONObject) obj.get("LoungeResource")).get("Lounges")).get("Lounge");
            loungeList = new Lounge[lounges.length()];
            for (int i = 0; i < lounges.length(); i++) {
                String name = getLoungeNameFromJson((JSONObject) lounges.get(i));
                String location = getLoungeLocationFromJson((JSONObject) lounges.get(i));
                boolean restrooms = getLoungeRestroomsFromJson((JSONObject) lounges.get(i));
                boolean showers = getLoungeShowersFromJson((JSONObject) lounges.get(i));
                boolean faxMachine = getLoungeFaxMachineFromJson((JSONObject) lounges.get(i));
                boolean wlan = getLoungeWlanFromJson((JSONObject) lounges.get(i));
                Lounge lounge = new Lounge(name, location, restrooms, showers, faxMachine, wlan);
                loungeList[i] = lounge;
            }
        }
        return loungeList;
    }

    private static boolean getLoungeWlanFromJson(JSONObject jsonObject) {
        JSONObject features = (JSONObject) jsonObject.get("Features");
        return features.getBoolean("WLANFacility");
    }

    private static boolean getLoungeFaxMachineFromJson(JSONObject jsonObject) {
        JSONObject features = (JSONObject) jsonObject.get("Features");
        return features.getBoolean("FaxMachine");
    }

    private static boolean getLoungeShowersFromJson(JSONObject jsonObject) {
        JSONObject features = (JSONObject) jsonObject.get("Features");
        return features.getBoolean("ShowerFacilities");
    }

    private static boolean getLoungeRestroomsFromJson(JSONObject jsonObject) {
        JSONObject features = (JSONObject) jsonObject.get("Features");
        return features.getBoolean("Restrooms");
    }

    private static String getLoungeLocationFromJson(JSONObject jsonObject) {
        if (!jsonObject.has("Locations")) return null;
        if (((JSONObject) jsonObject.get("Locations")).get("Location") instanceof JSONObject) {
            JSONObject name = (JSONObject) ((JSONObject) jsonObject.get("Locations")).get("Location");
            return name.getString("$");
        } else {
            JSONArray locationMultiLang = (JSONArray) ((JSONObject) jsonObject.get("Locations")).get("Location");
            for (int i = 0; i < locationMultiLang.length(); i++) {
                if (((JSONObject) locationMultiLang.get(i)).getString("@LanguageCode").equalsIgnoreCase("en"))
                    return ((JSONObject) locationMultiLang.get(i)).getString("$");
            }
            return ((JSONObject) locationMultiLang.get(0)).getString("$");
        }
    }

    private static String getLoungeNameFromJson(JSONObject jsonObject) {
        if (((JSONObject) jsonObject.get("Names")).get("Name") instanceof JSONObject) {
            JSONObject name = (JSONObject) ((JSONObject) jsonObject.get("Names")).get("Name");
            return name.getString("$");
        } else {
            JSONArray name = (JSONArray) ((JSONObject) jsonObject.get("Names")).get("Name");
            return ((JSONObject) name.get(0)).getString("$");
        }
    }

    private static String getDepartingFlightNumberFromJson(String statusAsJson) {
        JSONObject flight = getFlightObject(statusAsJson);
        if (flight.has("OperatingCarrier")) {
            if ( ((JSONObject) flight.get("OperatingCarrier")).has("FlightNumber")) {
                String airLineID = ((JSONObject) flight.get("OperatingCarrier")).get("AirlineID").toString();
                String number = ((JSONObject) flight.get("OperatingCarrier")).get("FlightNumber").toString();
                String flightNumber =airLineID+number;
                return flightNumber;
            }
        }
        return null;
    }

    private static String getArrivalAirportFromJson(String statusAsJson) {
        JSONObject flight = getFlightObject(statusAsJson);
        return ((JSONObject) flight.get("Arrival")).getString("AirportCode");
    }

    private static String getDepartureAirportFromJson(String statusAsJson) {
        JSONObject flight = getFlightObject(statusAsJson);
        return ((JSONObject) flight.get("Departure")).getString("AirportCode");
    }

    private static String getArrivalAirportGateFromJson(String statusAsJson) {
        JSONObject flight = getFlightObject(statusAsJson);
        JSONObject arrival = (JSONObject) flight.get("Arrival");
        return getGateInfoFromJson(arrival);
    }

    private static String getDepartureAirportGateFromJson(String statusAsJson) {
        JSONObject flight = getFlightObject(statusAsJson);
        JSONObject departure = (JSONObject) flight.get("Departure");
        return getGateInfoFromJson(departure);
    }

    private static String getArrivalTimeFromJson(String statusAsJson){
        JSONObject flight = getFlightObject(statusAsJson);
        JSONObject arrival = (JSONObject) flight.get("Arrival");
        return getActualOrEstimatedOrScheduledTimeLocalFromJson(arrival);
    }

    private static String getDepartureTimeFromJson(String statusAsJson){
        JSONObject flight = getFlightObject(statusAsJson);
        JSONObject departure = (JSONObject) flight.get("Departure");
        return getActualOrEstimatedOrScheduledTimeLocalFromJson(departure);
    }

    private static String getActualOrEstimatedOrScheduledTimeLocalFromJson(JSONObject departureOrArrival) {
        String time = "n/a";
        if (departureOrArrival.has("ActualTimeLocal")) {
            time = ((JSONObject) departureOrArrival.get("ActualTimeLocal")).get("DateTime").toString();
        } else if (departureOrArrival.has("EstimatedTimeLocal")) {
            time = ((JSONObject) departureOrArrival.get("EstimatedTimeLocal")).get("DateTime").toString();
        } else if (departureOrArrival.has("ScheduledTimeLocal")) {
            time = ((JSONObject) departureOrArrival.get("ScheduledTimeLocal")).get("DateTime").toString();
        }
        return time;
    }

    @NotNull
    private static String getGateInfoFromJson(JSONObject departureOrArrival) {
        String gate = "none";
        String terminalName = "none";
        if ( departureOrArrival.has("Terminal") ) {
            JSONObject terminal = (JSONObject) departureOrArrival.get("Terminal");
            if (terminal.has("Gate")){ gate = terminal.get("Gate").toString(); }
            if (terminal.has("Name")){ terminalName = terminal.get("Name").toString(); }
        }
//        Object[] gateInfo ={terminalName, gate};
        String gateInfo ="Terminal: "+terminalName+" Gate: "+gate;
        return gateInfo;
    }

    private static JSONObject getFlightObject(String statusAsJson){
        JSONObject obj = new JSONObject(statusAsJson);
        JSONObject flight;
        if (((JSONObject) ((JSONObject) obj.get("FlightStatusResource")).get("Flights")).get("Flight") instanceof JSONObject) {
            flight = (JSONObject) ((JSONObject) ((JSONObject) obj.get("FlightStatusResource")).get("Flights")).get("Flight");
        } else {
            flight = (JSONObject) ((JSONArray) ((JSONObject) ((JSONObject) obj.get("FlightStatusResource")).get("Flights")).get("Flight")).get(0);
        }
        return flight;
    }

    private static Object[] getArrivalCoordsFromJson(String statusAsJson) {
        JSONObject obj = new JSONObject(statusAsJson);
        JSONObject airport = (JSONObject) ((JSONObject) ((JSONObject) obj.get("AirportResource")).get("Airports")).get("Airport");
        JSONObject coords = (JSONObject) ((JSONObject) airport.get("Position")).get("Coordinate");
        Object[] latLong = {coords.getDouble("Latitude"), coords.getDouble("Longitude")};
        return latLong;
    }

    private static Object[] getDepartureCoordsFromJson(String statusAsJson) {
        JSONObject obj = new JSONObject(statusAsJson);
        JSONObject airport = (JSONObject) ((JSONObject) ((JSONObject) obj.get("AirportResource")).get("Airports")).get("Airport");
        JSONObject coords = (JSONObject) ((JSONObject) airport.get("Position")).get("Coordinate");
        Object[] latLong = {coords.getDouble("Latitude"), coords.getDouble("Longitude")};
        return latLong;
    }

    private static String get(String urlSuffix) {
        String assembledOutput = "";
        try {
            URL urlObject = new URL(baseUrl + urlSuffix);
            HttpsURLConnection connection = (HttpsURLConnection) urlObject.openConnection();


            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/" + "json");
            connection.setRequestProperty("authorization", "Bearer " + bearer);

            connection.connect();

            if (connection.getResponseCode() == 200) {


                BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(
                        (connection.getInputStream())));

                String output;
                while ((output = responseBuffer.readLine()) != null) {
                    assembledOutput = assembledOutput + output;
                }
            } else {
                throw new IOException("Failed with HTTP code " + connection.getResponseCode() + " calling " + baseUrl + urlSuffix);
            }
            connection.disconnect();
        } catch (IOException e) {
            System.err.println(e);
            return null;
        }

        return assembledOutput;
    }

    private static String requestToken() {
        String assembledOutput = "";

        try {
            URL urlObject = new URL(baseUrl + "oauth/token");
            HttpsURLConnection connection = (HttpsURLConnection) urlObject.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String urlParameters = "client_id=" + clientId + "&client_secret=" + clientSecret + "&grant_type=client_credentials";
            byte[] postData = urlParameters.getBytes();

            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(postData);
            }

            BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(
                    (connection.getInputStream())));
            String output;
            while ((output = responseBuffer.readLine()) != null) {
                assembledOutput = assembledOutput + output;
            }
        } catch (IOException e) {
            System.err.println(e);
            return null;
        }
        JSONObject obj = new JSONObject(assembledOutput);
        return obj.getString("access_token");
    }
}
