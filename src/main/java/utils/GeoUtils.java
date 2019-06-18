package utils;

public class GeoUtils {

    public static double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180;
    }

    // calculates the distance between two coordinates in km
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        long earthRadiusKm = 6371;

        double dLat = degreesToRadians(lat2 - lat1);
        double dLon = degreesToRadians(lon2 - lon1);

        lat1 = degreesToRadians(lat1);
        lat2 = degreesToRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(earthRadiusKm * c);
    }

    public static double msToKmh(double ms) {
        return ms * 3.6;
    }

    public static String eta(double distance, double speed) {
        double time = distance / speed;
        int hour = (int) time;
        int minutes = ((int) ((time - hour) * 60)) + 1;
        return String.valueOf(hour) + ":" + String.valueOf(minutes);

    }

}