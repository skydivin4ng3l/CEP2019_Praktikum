package utils;


public class WeatherUtils {
    public static Object[] getWeatherCoords(String coordsString){
        String delims = "[a-zA-Z(=,) ]+";
        String[] tokens = coordsString.split(delims);
        if (tokens.length>1){
            Object[] latlong = new Object[]{tokens[1], tokens[2]};
            return latlong;
        }
        return null;
    }

    //WIP not working
    public static String getWeatherInfo(String weatherInfoString){
        String delims = "[\\[\\]= ,()]+";
        String[] tokens = weatherInfoString.split("\\[Weather\\(conditionalId=|, mainInfo=|, iconCode=|");
        if (tokens.length>1){
            String weatherInfo = tokens[1].toString()+" "+tokens[2].toString();
            return weatherInfo;
        }
        return null;
    }

    public static boolean hasGoodWeather(String weatherInfo) {
        if(!weatherInfo.isEmpty()){
            if (weatherInfo.toUpperCase().contains("RAIN") || weatherInfo.toUpperCase().contains("DRIZZLE") || weatherInfo.toUpperCase().contains("THUNDERSTORM")) {
                return false;
            }
            return true;
        }
        return false;
    }
}
