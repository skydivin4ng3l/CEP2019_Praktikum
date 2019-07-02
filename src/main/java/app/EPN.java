package app;

import booking.Booking;
import booking.CabinClass;
import cep.CEPListener;
import com.espertech.esper.client.*;
import com.google.gson.JsonSyntaxException;
import com.github.javafaker.Faker;
import net.aksingh.owmjapis.api.APIException;
import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.model.CurrentWeather;

import cities.Cities;
import org.opensky.api.OpenSkyApi;
import org.opensky.model.OpenSkyStates;
import org.opensky.model.StateVector;
import utils.Callsign;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Date;

public class EPN {

    private static EPRuntime cepRT;
    private static Faker faker = new Faker();

    public static void main(String[] args) throws APIException {

        // setup
        EPServiceProvider cep = EPServiceProviderManager.getProvider("myCEPEngine");
        cep.initialize();
        EPAdministrator cepAdm = cep.getEPAdministrator();
        ConfigurationOperations cp = cepAdm.getConfiguration();
        cepRT = cep.getEPRuntime();

        // event types
        cp.addEventType("StateVector", StateVector.class.getName());
        cp.addEventType("CurrentWeather", CurrentWeather.class.getName());
        cp.addEventType("Booking", Booking.class.getName());

        // -------Cause of ReadTimeOut Errors we choose to combine EPA's into one EPStatement as it seems fit
        // event queries (EPAs)
        EPStatement lhFilter = cepAdm.createEPL("insert into LHStateVectorStream select * " +
                "from StateVector(callsign regexp '[ \\t\\n\\f\\r]*(EWG|DLH|AUA|SWR)[0-9]{1,4}[ \\t\\n\\f\\r]*')");

        EPStatement callsignToFlightNumber = cepAdm.createEPL("insert into LHStateVFlightNumberStream select" +
                " *, utils.Callsign.icaoToIata(callsign) as flightNumber " +
                "from LHStateVectorStream");

        EPStatement lhDestinationCoordinates = cepAdm.createEPL("insert into LHStateVFlightNumberDestiCoordStream select " +
                "*,lufthansa.Lufthansa.getArrivalAirportCoords(flightNumber) as destinationCoordinates " +
                "from LHStateVFlightNumberStream");

        //get city for destination seems to produce a lot of "none" cities,...don't know how to easily access
        // coordData's lat and lon as join attributes for destinationWeatherSight from OWM
        EPStatement lhDestinationCity = cepAdm.createEPL("insert into DestinationCityStream select " +
                "flightNumber, " +
                "cities.Cities.getCity(cast(destinationCoordinates[0],double), cast(destinationCoordinates[1],double)) as destinationCity " +
                "from LHStateVFlightNumberDestiCoordStream " +
                "where destinationCoordinates is not null");

        /*----------------Reduced amount of Streams cause of Read TimeOut ------ have to figure out why this happens
        or reduce amount of simultaneous streams*/
        //obsolete//EPStatement currentWeather = cepAdm.createEPL("insert into WeatherStream select coordData,
        // weatherList, cityName from CurrentWeather");
        EPStatement destinationWeatherSight = cepAdm.createEPL("insert into DestinationWeatherSightInfoStream select " +
                "destination.flightNumber, destination.destinationCity, weather.weatherList, " +
                "cities.Cities.getSight(cast(destination.destinationCity,String),cast(weather.weatherList,String)) as destinationSights " +
                "from DestinationCityStream#length(100) as destination join CurrentWeather#length(100) as weather " +
                "where weather.cityName = destination.destinationCity and destination.destinationCity !='none'");

        // WIP for coordinates //EPStatement destinationWeatherSight = cepAdm.createEPL("insert into
        // DestinationWeatherSightInfoStream select destination.flightNumber, destination.destinationCity, weather
        // .weatherList, cities.Cities.getSight(cast(destination.destinationCity,String),cast(weather.weatherList,
        // String)) as destinationSights from DestinationCityStream#length(100) as destination join
        // CurrentWeather#length(100) as weather where cast(weather.coordData,String) = cast(lufthansa.Lufthansa
        // .getArrivalAirportCoords(destination.flightNumber),String)");

        EPStatement distance = cepAdm.createEPL("insert into DistanceVelocityStream select " +
                "flightNumber, velocity, utils.GeoUtils.distance(latitude, longitude," +
                    "cast(destinationCoordinates[0],double), cast(destinationCoordinates[1],double) )as distance " +
                "from LHStateVFlightNumberDestiCoordStream " +
                "where destinationCoordinates is not null ");
        EPStatement speed = cepAdm.createEPL("insert into DistanceAvgVelocityStream select " +
                "distance, flightNumber, distance, utils.GeoUtils.msToKmh(cast(avg(velocity),double))as speed " +
                "from DistanceVelocityStream#groupwin(flightNumber)#length(2) " +
                "where velocity is not null ");
        EPStatement eta = cepAdm.createEPL("insert into ETAStream select " +
                "flightNumber, utils.GeoUtils.eta(distance, speed) as ETA " +
                "from DistanceAvgVelocityStream ");
        /*----------------Reduced amount of Streams cause of Read TimeOut ------ have to figure out why this happens
        or reduce amount of simultaneous streams*/

        //Obsolete//EPStatement bookingAll = cepAdm.createEPL("insert into BookingAllStream select * from Booking");
        //not requested Verification stream of arrival/departure gates and time implementation
        //EPStatement gates = cepAdm.createEPL("insert into GatesStream select flightNumber, lufthansa.Lufthansa
        // .getDepartureAirportGate(flightNumber) as DepartureGate, lufthansa.Lufthansa.getDepartureTime
        // (flightNumber) as LocalDepartureTime, lufthansa.Lufthansa.getArrivalAirportGate(flightNumber) as
        // ArrivalGate, lufthansa.Lufthansa.getArrivalTime(flightNumber) as LocalArrivalTime from
        // LHStateVFlightNumberStream");

        //do we really need the statevektor for this? All the information we also get from the bookingStream if we
        // actually need the stream, why can't we just use the Booking? Implementation of FlightStatuses currently does not allow updates.
        EPStatement connectionFlightGates = cepAdm.createEPL("insert into ConnectionFlightGatesStream select " +
                "booking.passengerName as passengerName, booking.flightNumber as flightNumber," +
                "booking.connectionFlightNumber as connectionFlightNumber, " +
                "lufthansa.Lufthansa.getDepartureAirportGate(booking.connectionFlightNumber) as DepartureGateConnectionFlight," +
                "lufthansa.Lufthansa.getDepartureTime(booking.connectionFlightNumber) as DepartureLocalTimeConnectionFlight," +
                "lufthansa.Lufthansa.getArrivalAirportGate(booking.flightNumber) as ArrivalGate," +
                "lufthansa.Lufthansa.getArrivalTime(booking.flightNumber) as ArrivalLocalTime " +
                "from LHStateVFlightNumberStream#length(10) as flight join Booking#length(10) as booking " +
                "where booking.flightNumber = flight.flightNumber and booking.connectionFlightNumber is not null");

        EPStatement onBoardSights = cepAdm.createEPL("insert into OnBoardSightsStream select " +
                "flightNumber, cities.Cities.getCity(cast(latitude,double),cast(longitude,double)) as sights " +
                "from LHStateVFlightNumberStream");
        /*EPStatement ifeOnBoardSights = cepAdm.createEPL("insert into FinalOnBoardSightsStream select * from " +
        "OnBoardSightsStream where sights != 'none'");*/

        //currently obsolete if loungeInfo calls for airportcode themselves still counts as ENRICH EPA
        /*EPStatement lhDestinationAirport = cepAdm.createEPL("insert into LHStateVFlightNumberDestiAirportStream select " +
                "*, lufthansa.Lufthansa.getArrivalAirportCode(flightNumber) as destinationAirport " +
                "from LHStateVFlightNumberStream");*/

        //get lounge info depending on the airport code no join needed
        EPStatement loungeInfo = cepAdm.createEPL("insert into LoungeInfoStream select " +
                "flightNumber, lufthansa.Lufthansa.getArrivalAirportCode(flightNumber) as destinationAirport," +
                "lufthansa.Lufthansa.getAirportLounges(lufthansa.Lufthansa.getArrivalAirportCode(flightNumber)) as lounges " +
                "from LHStateVFlightNumberStream#unique(flightNumber) as DestinationAirport");

        //joins lounge info per flightNumber with the respective Bookings above economy
        EPStatement loungeSelector = cepAdm.createEPL("insert into OutStream9 select " +
                "Booking.passengerName as passengerName, LoungeInfo.flightNumber as flightNumber, " +
                "LoungeInfo.destinationAirport as destinationAirport, " +
                "lounges[0].name as loungeName, lounges[0].showers as showers " +
                "from LoungeInfoStream#unique(flightNumber) as LoungeInfo " +
                "JOIN Booking(cabinClass.toString() != 'ECONOMY')#length(5) as Booking " +
                "where LoungeInfoStream.flightNumber = Booking.flightNumber");

//        EPStatement ife = cepAdm.createEPL("insert into FinalStream select * from OutStream9");

        // event listener
        lhFilter.addListener(new CEPListener("lhFilter"));
        callsignToFlightNumber.addListener(new CEPListener("callsignToFlightNumber"));
//        lhDestinationAirport.addListener(new CEPListener("lhDestinationAirport"));
        lhDestinationCoordinates.addListener(new CEPListener("lhDestinationCoordinates"));
        distance.addListener(new CEPListener("Distance"));
        speed.addListener(new CEPListener("Speed"));
        eta.addListener(new CEPListener("ETA"));
//        gates.addListener(new CEPListener("Gates"));
        lhDestinationCity.addListener(new CEPListener("lhDestinationCity"));
//        currentWeather.addListener(new CEPListener("CurrentWeather"));
        destinationWeatherSight.addListener(new CEPListener("DestinationWeatherSight"));
        loungeInfo.addListener(new CEPListener("loungeInfo"));
        connectionFlightGates.addListener(new CEPListener("connectionFlightGates"));
        onBoardSights.addListener(new CEPListener("OnBoardSights"));
        loungeSelector.addListener(new CEPListener("loungeSelector"));
//        ife.addListener(new CEPListener("ife"));
//        ifeOnBoardSights.addListener(new CEPListener("ifeOnBoardSights"));

        // send events to engine
        Thread thread1 = new Thread() {
            public void run() {
                OpenSkyApi opensky = new OpenSkyApi();
                sendOpenSkyEvents(opensky);
            }
        };
        Thread thread2 = new Thread() {
            public void run() {
                // Enter your OWM key here
                String owmKey = "aa00b89974868fc67ad23e7ff2267dff";
                OWM owm = new OWM(owmKey);
                owm.setUnit(OWM.Unit.METRIC);
                sendWeatherEvents(owm);
            }
        };
        thread1.start();
        thread2.start();
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sendOpenSkyEvents(OpenSkyApi opensky) {
        for (int i = 0; i < 100; i++) {
            OpenSkyStates os = null;
            try {
                os = opensky.getStates(0, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (StateVector flight : os.getStates()) {
                if (flight.getLatitude() != null) {
                    cepRT.sendEvent(flight);
                    String flightNumber = "";
                    try {
                        flightNumber = Callsign.icaoToIata(flight.getCallsign());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sendBookingEvents(flightNumber);
                }
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static void sendBookingEvents(String flightNumber) {
        for (int i = 0; i < 10; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, 4);
            String connectionFlightNumber = null;
            // just one person has a connection flight if available // regex change to match IATA
            if (i == 0) {
                if (flightNumber.matches("[ \t\n\f\r]*(EW|LH|OS|LX)[0-9]{1,4}[ \t\n\f\r]*")) {
                    connectionFlightNumber = lufthansa.Lufthansa.getConnectionFlight(flightNumber);
                }
            }
            cepRT.sendEvent(new Booking(flightNumber, CabinClass.values()[randomNum], faker.name().fullName(),
                    connectionFlightNumber));

        }
    }

    public static void sendWeatherEvents(OWM owm) {

        ArrayList<String[]> cities = Cities.getCities();
        for (int j = 0; j < 100; j++) {
            for (int i = 0; i < cities.size(); i++) {
                String[] city = cities.get(i);
                String cityName = city[0];
                CurrentWeather cwd = null;
                try {
                    cwd = owm.currentWeatherByCityName(cityName);
                } catch (APIException e) {
                    e.printStackTrace();
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
                if (cwd != null) {
                    cepRT.sendEvent(cwd);
                }
            }
            try {
                Thread.sleep(120000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
