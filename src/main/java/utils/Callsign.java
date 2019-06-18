package utils;

import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Callsign {
    private static ArrayList<String[]> airlines;

    static {
        airlines = new ArrayList<>();
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader("./src/main/resources/callsigns.csv"));
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                airlines.add(nextLine);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String icaoToIata(String icao) throws IOException {
        String icaoFull = icao.replaceAll("\\s+", "");
        if (icaoFull.length() < 3) return icaoFull;
        icao = icaoFull.substring(0, 3);
        for (String[] airline : airlines) {
            if (airline[1].equalsIgnoreCase(icao) && !airline[0].isEmpty())
                return icaoFull.replace(icao, airline[0]);
        }
        return icaoFull;
    }
}
