package ch.mbuehler.eth.mgis.treasurego;

import android.content.Context;
import android.location.Location;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVReader;

/**
 * This class is responsible for loading the treasures from a file.
 * The CSV file with the treasures is stored a raw resource:
 * app/src/main/res/raw/treasures.csv
 *
 * It should contain a header line, use ";" as separator and contain fields for
 * treasure name,longitude,latitude,maximum coins
 *
 * Example:
 *
 * treasure name;longitude;latitude;maximum coins
 * Noodle Soup;8.5085627239;47.410294559;3
 * Potion of Endless Awakening;8.507075;47.408369;15
 * Common Tavern Ale;8.506452;47.4075;1
 */
class TreasureLoader {

    /**
     * Loads CSV file, creates Treasure instances and returns them as ArrayList
     * @param context
     * @return ArrayList of Treasure
     */
    ArrayList<Treasure> loadTreasures(Context context){
        // treasures will hold all Treasure objects
        ArrayList<Treasure> treasures = new ArrayList<Treasure>();

        try{
            // Prepare reading the CSV file
            // The CSV file with the treasures is stored a raw resource:
            // app/src/main/res/raw/treasures.csv
            InputStream stream = context.getResources().openRawResource(R.raw.treasures2);
            InputStreamReader reader = new InputStreamReader(stream);
            CSVReader csvReader = new CSVReader(reader, ';');

            // line will contain the data for one Treasure
            String[] line;

            // Skip the header line
            csvReader.readNext();

            // Iterate through the CSV file and create one Treasure per line
            while((line = csvReader.readNext()) != null){
                Treasure treasure = this.createTreasure(line);
                treasures.add(treasure);
            }
        } catch(IOException e){
            // There was a problem when reading the file
            e.printStackTrace();
        }
        return treasures;
    }

    /**
     *
     * @param line Parsed line from CSV file.
     * Expected fields: treasure name;longitude;latitude;maximum coins
     * Example content: ["Bethselaminian", "Backpack", "8.50", "47.40", "50"]
     * @return Treasure
     */
    private Treasure createTreasure(String[] line){
        // Longitude and latitude need to be converted to Double
        double lng = Double.parseDouble(line[1]);
        double lat = Double.parseDouble(line[2]);
        Location location = new Location("");
        location.setLongitude(lng);
        location.setLatitude(lat);

        // Reward has to be an Integer
        int reward = Integer.parseInt(line[3]);

        // The first entry is the name of the Treasure
        String name = line[0];
        return new Treasure(name, reward, location);
    }
}
