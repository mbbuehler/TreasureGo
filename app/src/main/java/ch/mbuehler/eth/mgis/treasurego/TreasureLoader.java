package ch.mbuehler.eth.mgis.treasurego;

import android.content.Context;
import android.location.Location;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVReader;

/**
 * This class is responsible for loading the treasures from a file.
 * The CSV file with the treasures is stored a raw resource:
 * app/src/main/res/raw/treasures.csv
 * <p>
 * It should contain a header line, use ";" as separator and contain fields for
 * treasure name,longitude,latitude,maximum coins
 * <p>
 * Example:
 * <p>
 * treasure name;longitude;latitude;maximum coins
 * Noodle Soup;8.5085627239;47.410294559;3
 * Potion of Endless Awakening;8.507075;47.408369;15
 * Common Tavern Ale;8.506452;47.4075;1
 */
class TreasureLoader {
    /**
     * Context / Activity
     */
    Context context;

    public TreasureLoader(Context context) {
        this.context = context;
    }

    /**
     * Loads CSV File, creates Treasure instances and returns them as ArrayList.
     * <p>
     * The CSV File named "treasure.csv" may be located in the external storage directory.
     * If there is no File available in the external storage directory, a default
     * File will be loaded from the app's raw resources.
     *
     * @return ArrayList of Treasure
     */
    ArrayList<Treasure> loadTreasures() {
        // treasures will hold all Treasure objects
        ArrayList<Treasure> treasures = new ArrayList<>();

        InputStream stream;

        try {
            // Prepare reading the CSV file
            stream = getInputStream();
            InputStreamReader reader = new InputStreamReader(stream);
            CSVReader csvReader = new CSVReader(reader, ';');

            // line will contain the data for one Treasure
            String[] line;

            // Skip the header line
            csvReader.readNext();

            // Iterate through the CSV file and create one Treasure per line
            while ((line = csvReader.readNext()) != null) {
                Treasure treasure = this.createTreasure(line);
                treasures.add(treasure);
            }
        } catch (IOException e) {
            // There was a problem when reading the file
            e.printStackTrace();
        }
        return treasures;
    }

    /**
     * Creates an InputStream from a File in the root of the external Storage Directory
     * If not File is located, we throw a FileNotFoundException
     * The file name has to be "treasure.csv"
     *
     * @return InputStream
     * @throws FileNotFoundException if no File has been found
     */
    private InputStream getInputStreamFromExternalStorage() throws FileNotFoundException {
        File directory = Environment.getExternalStorageDirectory();
        File file = new File(directory, "treasure.csv");
        return new FileInputStream(file);
    }

    /**
     * Creates an InputStream from a File in the raw resource directory of this app.
     * Path:
     * app/src/main/res/raw/treasures.csv
     *
     * @return InputStream
     */
    private InputStream getInputStreamFromRawResource() {
        return context.getResources().openRawResource(R.raw.treasures);
    }

    /**
     * Creates an InputStream.
     * If a File could be located in the external storage directory, this file is loaded.
     * If not File could be located, the default File from the Apps raw resources is loaded.
     *
     * @return InputStream
     */
    private InputStream getInputStream() {
        InputStream stream;
        try {
            stream = getInputStreamFromExternalStorage();
        } catch (FileNotFoundException e) {
            stream = getInputStreamFromRawResource();
        }
        return stream;
    }

    /**
     * @param line Parsed line from CSV file.
     *             Expected fields: treasure name;longitude;latitude;maximum coins
     *             Example content: ["Bethselaminian", "Backpack", "8.50", "47.40", "50"]
     * @return Treasure
     */
    private Treasure createTreasure(String[] line) {
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
