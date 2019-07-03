package cities;

import com.opencsv.CSVReader;
import utils.GeoUtils;

import java.io.*;
import java.util.ArrayList;

public class Cities {
    private static ArrayList<String[]> cities;

    static {
        cities = new ArrayList<>();
        CSVReader reader = null;
        try {
            reader = new CSVReader(new InputStreamReader(new FileInputStream("./src/main/resources/cities.csv"), "ISO-8859-1"   ));
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                cities.add(nextLine);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String[]> getCities() {
        return cities;
    }

    public static String getSight(String cityName, String weatherInfo) {
        for (String[] cityData : cities) {
            if (cityData[0].equalsIgnoreCase(cityName)) {
                if (cityData.length > 3) {
                    if (weatherInfo.toUpperCase().contains("RAIN") || weatherInfo.toUpperCase().contains("DRIZZLE") || weatherInfo.toUpperCase().contains("THUNDERSTORM")) {
                        return cityData[4];
                    }
                    return cityData[3];
                }
            }
        }
        return null;
    }

    public static boolean hasGoodWeather(String cityName, String weatherInfo) {
        for (String[] cityData : cities) {
            if (cityData[0].equalsIgnoreCase(cityName)) {
                if (cityData.length > 3) {
                    if (weatherInfo.toUpperCase().contains("RAIN") || weatherInfo.toUpperCase().contains("DRIZZLE") || weatherInfo.toUpperCase().contains("THUNDERSTORM")) {
                        return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static String getCity(double lat, double lon) {
        for (String[] cityData : cities) {
                if (cityData.length > 5) {
                    if (GeoUtils.distance(Double.parseDouble(cityData[5]), Double.parseDouble(cityData[6]), lat, lon) < 50)
                        return cityData[0];
                }
        }
        for (String[] cityData : cities) {
            if (cityData.length > 5) {
                if (GeoUtils.distance(Double.parseDouble(cityData[5]), Double.parseDouble(cityData[6]), lat, lon) < 100)
                    return cityData[0];
            }
        }
        return "none";
    }
}
