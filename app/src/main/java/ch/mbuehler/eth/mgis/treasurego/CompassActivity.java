package ch.mbuehler.eth.mgis.treasurego;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

/**
 * This activity guides the user to find Treasures.
 * <p>
 * There are a lot of things happening in this Activity.
 * In order to make this class as lightweight as possible,
 * most business logic has been extracted into other classes.
 * However, this class still has to manage the interplay between a number of components.
 * <p>
 * In order to make navigation within this class as easy as possible,
 * it has been structured into the following sections:
 * <p>
 * 1. Sensor Updates Section
 * Sensor updates need to be processed.
 * This section deals with the business logic for Sensor updates.
 * <p>
 * 2. Getters and Setters Section:
 * Getters and Setters for this class.
 * <p>
 * 3. Event Section:
 * This section has methods that deal with events, e.g. onCreate() or onResume().
 * <p>
 * 4. LocationUpdates Section:
 * This section deals with Location updates.
 */
public class CompassActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    /**
     * Managers for sensor data
     */
    private LocationManager locationManager;
    private SensorManager sensorManager;

    /**
     * Current Location of the user.
     */
    private Location currentLocation;

    /**
     * The treasure we want to find
     */
    private Treasure targetTreasure;

    /**
     * Responsible for updating the View elements, e.g. distance, time, etc.
     */
    CompassViewUpdater viewUpdater;

    /**
     * Max time between location updates
     */
    private static final long TIME_BW_UPDATES = 1000; // in milliseconds
    /**
     * Distance threshold for location updates
     */
    private static final long DIST_BW_UPDATES = 5; // in meters
    /**
     * When the user gets closer to the target Treasure than this value,
     * we consider the Treasure as "found"
     */
    private static final int DIST_TARGET_REACHED = 200; // in meters

    /**
     * Once the target Treasure has been reached, we want to stop updating View
     * and halt calculations. This variable is set to true when the user first
     * finds a Treasure.
     */
    private boolean targetReached = false; // ignore Location updates after reaching target

    /**
     * locationTracker keeps track of Location measurements. This allows for
     * more efficient computations of averageSpeed and distances.
     */
    public LocationTracker locationTracker;

    /**
     * Sensor values for orientation. Collected by RotationVector.
     * Elements of array:
     * - Azimuth (degrees of rotation about z-axis)
     * - Pitch (degrees of rotation about the x-axis)
     * - Roll (degrees of rotation about the y axis)
     * Check this link for further info:
     * https://developer.android.com/guide/topics/sensors/sensors_motion.html#sensors-motion-rotate
     */
    private float[] orientation = new float[3];
    private final float[] mRotationMatrix = new float[9];

    /**
     * Current temperature in degrees Celsius
     */
    private float currentTemperature;

    /**
     * timeHandler posts the timerRunnable regularly and makes sure
     * that we keep updating the View.
     */
    private Handler timerHandler = new Handler();


    /* ================== Handling Sensor Updates Section  ================== */

    /**
     * Update the values for the rotationMatrix and orientation.
     * See
     * https://developer.android.com/guide/topics/sensors/sensors_position.html
     * for more information
     *
     * @param sensorValues values from the sensor fusion for orientation.
     */
    private void handleRotationUpdate(float[] sensorValues) {
        SensorManager.getRotationMatrixFromVector(mRotationMatrix, sensorValues);
        SensorManager.getOrientation(mRotationMatrix, orientation);
    }

    /**
     * Distance to target Treasure has changed. If we are close enough to the target Treasure,
     * we will call onTargetReached(). If not we will update the view with the current distance.
     */
    private void handleDistanceToTargetUpdate() {

        String distanceText = "n.a.";

        // Calculate the distance to the target
        double distance = this.locationTracker.calculateSmoothedDistanceTo(getTargetLocation());
        if (distance < DIST_TARGET_REACHED) {
            // We have reached the target location
            onTargetLocationReached();
        } else if (distance > 1000) {
            // Display the distance in km
            distanceText = Formatter.formatDouble(distance / 1000, 1) + " " + getString(R.string.kilometers);
        } else {
            // Display the distance in m
            distanceText = Formatter.formatDouble(distance, 1) + " " + getString(R.string.meters);
        }
        // Update View
        viewUpdater.updateDistance(distanceText);
    }


    /* ================== Getters and Setters Section  ================== */

    public Location getTargetLocation() {
        return this.targetTreasure.getLocation();
    }

    /**
     * @return Current Location
     * @throws LocationNotFoundException if not Location is available
     */
    public Location getCurrentLocation() throws LocationNotFoundException {
        if (LocationServiceChecker.areLocationServicesEnabled(locationManager, this)) {
            viewUpdater.updateLocationNotFoundVisibility(View.GONE);
            if (this.currentLocation != null) {
                return this.currentLocation;
            }
        } else {
            viewUpdater.updateLocationNotFoundVisibility(View.VISIBLE);
        }
        throw new LocationNotFoundException("Location was not found.");
    }

    /**
     * @return Average speed since start of Quest
     */
    double getAverageSpeed() {
        return this.locationTracker.getAverageSpeed();
    }

    /**
     * Returns the last measured temperature or 0 if temperature is not available.
     *
     * @return
     */
    float getCurrentTemperature() {
        return this.currentTemperature;
    }

    Treasure getTargetTreasure() {
        return this.targetTreasure;
    }

    /**
     * Calculates the heading between current device orientation and target Location
     *
     * @return float 0 <= angle <= 359 to target Location
     * @throws LocationNotFoundException if not Location was available
     */
    float getHeading() throws LocationNotFoundException {
        Location currentLocation = getCurrentLocation();
        Location targetLocation = getTargetLocation();
        return HeadingCalculator.calculateHeading(orientation, currentLocation, targetLocation);
    }


    /* ================== Event Section ================== */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        // Instantiate instances for view updates and locationTracking
        viewUpdater = new CompassViewUpdater(this);
        locationTracker = new LocationTracker();

        // Obtain target Treasure from Intent
        targetTreasure = Treasure.unserializeTreasureFromIntent(getIntent());

        // Instantiate mangers for location and sensors
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);

        // Update the field that names the target Treasure
        // We only need to do that once so we do it here.
        viewUpdater.updateSearchingFor();

        // Create the runnable responsible for regular updates
        CompassActivityRunnable runnable = new CompassActivityRunnable(timerHandler, viewUpdater);
        // Start the game
        timerHandler.postDelayed(runnable, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Listeners for Orientation
        this.sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
        // Listener for Temperature
        this.sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE), SensorManager.SENSOR_DELAY_NORMAL);

        //
        enableLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Save battery
        this.sensorManager.unregisterListener(this);
    }

    @Override
    public void onLocationChanged(Location currentLocation) {
        // Set instance variable such that other methods can access it
        this.currentLocation = currentLocation;
        // Keep track of all Locations measured
        this.locationTracker.addSample(currentLocation);
        // Update View and check if we have reached the target Location
        handleDistanceToTargetUpdate();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    /**
     * User has clicked the back button.
     * Will redirect the user to MainActivity.
     *
     * @param view
     */
    public void onAbort(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onMapButtonClicked(View view) {
        view.setEnabled(false);

        Intent intent = new Intent(this, MapsActivity.class);
        // Serialize and send the target Treasure with the Intent.
        String serializedTreasure = getTargetTreasure().serialize();
        intent.putExtra(MainActivity.TREASURE_KEY, serializedTreasure);
        startActivity(intent);

        Toast.makeText(this, R.string.hintInfo, Toast.LENGTH_LONG).show();
    }

    /**
     * Handles what has to happen when the user has reached the target Location.
     * Will redirect the user to TreasureFoundActivity.
     */
    private void onTargetLocationReached() {
        // This check is necessary to prevent that
        // this method is called several times for the same Quest
        if (!this.targetReached) {
            // Prevents the code bellow to be called more than once
            this.targetReached = true;

            // Save Quest such that we can access it later
            Quest completedQuest = new Quest(getTargetTreasure(), QuestStatus.COMPLETED);
            GameStatus.Instance().addQuest(completedQuest);

            Intent intent = new Intent(this, ARActivity.class);
            intent.putExtra(MainActivity.TREASURE_KEY, getTargetTreasure().serialize());
            startActivity(intent);


//
//            // Go to next Activity
//            Intent intent = new Intent(this, TreasureFoundActivity.class);
//            intent.putExtra(MainActivity.TREASURE_KEY, getTargetTreasure().getUuid());
//            startActivity(intent);
        }
    }

    /**
     * Listener for Sensor updates.
     * Handles each Sensor separately.
     *
     * @param sensorEvent can be from any sensor
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ROTATION_VECTOR:
                // Update device orientation
                handleRotationUpdate(sensorEvent.values);
                break;
            default:
                break;
        }
    }


    /* ================== LocationUpdates Section ================== */

    /**
     * Requests Location updates from the locationManager. If there are missing permissions,
     * the user is asked to provide them.
     */
    void enableLocationUpdates() {
        // Check if we have the required permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_BW_UPDATES, DIST_BW_UPDATES, this);
        }
    }

}
