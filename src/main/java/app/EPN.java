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

        // event queries (EPAs)
        EPStatement lhFilter = cepAdm.createEPL("insert into OutStream1 select * from StateVector(callsign regexp '[ \\t\\n\\f\\r]*(EWG|DLH|AUA|SWR)[0-9]{1,4}[ \\t\\n\\f\\r]*')");

        EPStatement callsignToFlightNumber = cepAdm.createEPL("insert into OutStream2 select *, utils.Callsign.icaoToIata(callsign) as flightNumber from OutStream1");

        EPStatement lhDestinationAirport = cepAdm.createEPL("insert into OutStream3 select *, lufthansa.Lufthansa.getArrivalAirportCode(flightNumber) as destinationAirport from OutStream2");

        EPStatement bookingFilter = cepAdm.createEPL("insert into OutStream4 select * from Booking(cabinClass.toString() != 'ECONOMY')");

        //EPStatement loungeInfo = cepAdm.createEPL("insert into OutStream8 select *, lufthansa.Lufthansa.getAirportLounges(destinationAirport) as lounges from OutStream3");
        EPStatement loungeInfo = cepAdm.createEPL("insert into OutStream8 select *, lufthansa.Lufthansa.getAirportLounges(destinationAirport) as lounges from OutStream3 JOIN OutStream4 where OutStream3.flightNumber = OutStream4.flightNumber");

        EPStatement loungeSelector = cepAdm.createEPL("insert into OutStream9 select flightNumber, destinationAirport, " +
                "lounges[0].name as loungeName, lounges[0].showers as showers from OutStream8");

        EPStatement ife = cepAdm.createEPL("insert into FinalStream select * from OutStream9");

        // event listener
        lhFilter.addListener(new CEPListener("lhFilter"));
        callsignToFlightNumber.addListener(new CEPListener("callsignToFlightNumber"));
        lhDestinationAirport.addListener(new CEPListener("lhDestinationAirport"));
        loungeInfo.addListener(new CEPListener("loungeInfo"));
        loungeSelector.addListener(new CEPListener("loungeSelector"));
        ife.addListener(new CEPListener("ife"));

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
                String owmKey = "bab296ceeacf70e2398b913ee2240566";
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
            cepRT.sendEvent(new Booking(flightNumber, CabinClass.values()[randomNum], faker.name().fullName()));
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
