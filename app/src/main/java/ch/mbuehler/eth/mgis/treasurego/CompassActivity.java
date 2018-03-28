package ch.mbuehler.eth.mgis.treasurego;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * This activity helps the user find Treasures.
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
     * Location of the target Treasure. This is where the user should be heading to.
     */
    private Location targetLocation;
    /**
     * The treasure we want to find
     */
    private Treasure targetTreasure;


    /**
     * Time between location updates
     */
    private static final long TIME_BW_UPDATES = 1000; // in milliseconds
    /**
     * Distance threshold for location updates
     */
    private static final long DIST_BW_UPDATES = 5; // in meters
    /**
     * When the user gets closer to the target Treasure than this value, we consider the Treasure as "found"
     */
    private static final int DIST_TARGET_REACHED = 20; // in meters TODO: adjust

    private boolean hasUserDeniedPermissions = false;

    private long startTime = 0;
    private long lastMeasuredTime = 0;
    private boolean targetReached = false; // ignore Location updates after reaching target

    private final int SENSOR_DELAY = 500; // 500ms
    private final int ARROW_UPDATE_DELAY = 500 * 1000000;  // in nanoseconds. 500ms


    public static final int REQUEST_CODE_ASK_PERMISSION = 1;

    public LocationTracker locationTracker;

    private float[] orientation = new float[3];
    private final float[] mRotationMatrix = new float[9];
    /**
     * Azimuth (degrees of rotation about z-axis), Pitch (degrees of rotation about the x-axis), Roll (degrees of rotation about the y axis)
     */

    private float currentTemperature;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        // https://stackoverflow.com/questions/4597690/android-timer-how-to
        @Override
        public void run() {
            updateAverageSpeed();
            updateCurrentSpeed();
            updateCurrentReward();
            updateTime();

            if (System.nanoTime() - lastMeasuredTime > 1000000000) {
                updateArrow();
                lastMeasuredTime = System.nanoTime();
            }
            timerHandler.postDelayed(this, SENSOR_DELAY);
        }
    };


    /* ================== Helper Methods Section  ================== */

    /**
     * Gets the state of Airplane Mode.
     *
     * @param context
     * @return true if enabled.
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }

    private boolean areLocationServicesEnabled() {
        boolean gps_enabled = false;
        try {
            gps_enabled = this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean airplainModeIsOff = !this.isAirplaneModeOn(getApplicationContext());
        return airplainModeIsOff && gps_enabled;

    }

    /**
     * Azimuth (degrees of rotation about the -z axis). This is the angle between the device's current compass direction and magnetic north. If the top edge of the device faces magnetic north, the azimuth is 0 degrees; if the top edge faces south, the azimuth is 180 degrees. Similarly, if the top edge faces east, the azimuth is 90 degrees, and if the top edge faces west, the azimuth is 270 degrees.
     *
     * @return
     */

    private double normalizeDegree(double value) {
        double normalized;
        // https://stackoverflow.com/questions/4308262/calculate-compass-bearing-heading-to-location-in-android
        if (value >= 0.0d && value <= 180.0d) {
            normalized = value;
        } else {
            normalized = 180 + (180 + value);
        }
        normalized = Math.min(360, normalized);
        normalized = Math.max(0, normalized);
        return normalized;
    }


    private Treasure unserializeTreasureFromIntent() {
        Intent intent = getIntent();
        String treasureSerialized = intent.getStringExtra(MainActivity.TREASURE_KEY);
        return new Treasure(treasureSerialized);
    }




    /* ================== Permissions Section  ================== */

    /**
     * Checks the required permissions and requests them if needed.
     * Required permissions:
     * - Manifest.permission.ACCESS_FINE_LOCATION
     * - Manifest.permission.ACCESS_COARSE_LOCATION
     */
    private void checkPermissions() {
        // All required permissions
        String[] permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                // We don't need this because we store the CSV file with the app itself.
                // Uncomment this file if you want to read the CSV file from external storage.
//                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        // Holds permissions that we need to ask permissions for.
        final List<String> permissionsList = new ArrayList<>();
        // Add permissions that we don't have yet
        for(String permission: permissions){
            addPermission(permissionsList, permission);
        }

        if (permissionsList.size() > 0) {
            // We have permissions to ask for
            ActivityCompat.requestPermissions(this, permissionsList.toArray(new String[permissionsList.size()]), REQUEST_CODE_ASK_PERMISSION);
        }
    }

    /**
     * Requests Location updates from the locationManager. If there are missing permissions,
     * the user is asked to provide them.
     */
    private void enableLocationUpdates() {
        if(hasUserDeniedPermissions){
            // The user has already denied permissions. Go back to MainActivity.
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            // Inform the user why we went back.
            Toast.makeText(this, R.string.pleasePermissions, Toast.LENGTH_LONG).show();
            return;
        }

        // Check if we have the required permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_BW_UPDATES, DIST_BW_UPDATES, this);
        } else {
            // Ask user for permission
            checkPermissions();
        }
    }

    /**
     * Checks if permission has been granted. If not the permission code is added to permissionList
     * https://inthecheesefactory.com/blog/things-you-need-to-know-about-android-m-permission-developer-edition/en
     * @param permissionsList
     * @param permission
     * @return true if permission has been granted and false otherwise
     */
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            // Permission has not been granted. Add it to the list.
            permissionsList.add(permission);

            if (!shouldShowRequestPermissionRationale(permission)) {
                return false;
            }
        }
        return true;
    }

    /* ================== Handling Sensor Updates Section  ================== */

    /**
     * Extracts the measured temperature from the sensor values and updates the View.
     * @param sensorValues
     */
    private void handleTemperatureUpdate(float[] sensorValues) {
        this.currentTemperature = sensorValues[0];
        this.updateTemperature();
    }

    /**
     * Update the values for the rotationMatrix and orientation.
     * See
     * https://developer.android.com/guide/topics/sensors/sensors_position.html
     * for more information
     * @param sensorValues
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

        //
        double distance = this.locationTracker.calculateSmoothedDistanceTo(this.targetLocation);
        if (distance < DIST_TARGET_REACHED) {
            // We have reached the target location
            onTargetLocationReached();
        } else if (distance > 1000) {
            // Display the distance in km
            distanceText = Formatter.formatDouble(distance / 1000, 1) + " km";
        } else {
            // Display the distance in m
            distanceText = Formatter.formatDouble(distance, 1) + " m";
        }
        updateDistance(distanceText);
    }



    /* ================== Getters and Setters Section  ================== */

    public Location getCurrentLocation() throws LocationNotFoundException {
        if (this.areLocationServicesEnabled()) {
            updateLocationNotFoundVisibility(View.GONE);
            if (this.currentLocation != null) {
                return this.currentLocation;
            }
        } else {
            updateLocationNotFoundVisibility(View.VISIBLE);
        }
        throw new LocationNotFoundException("Location was not found.");
    }

    private double getAverageSpeed() {
        return this.locationTracker.getAverageSpeed();
    }

    /**
     * Returns the last measured temperature or 0 if temperature is not available.
     *
     * @return
     */
    private float getCurrentTemperature() {
        return this.currentTemperature;
    }

    private Treasure getTargetTreasure() {
        return this.targetTreasure;
    }

    public float getDirection() throws LocationNotFoundException {
        // https://stackoverflow.com/questions/5479753/using-orientation-sensor-to-point-towards-a-specific-location

        double azimuthRadians = orientation[0];
        double azimuthDegrees = Math.toDegrees(azimuthRadians);

        GeomagneticField geoField = new GeomagneticField(
                Double.valueOf(this.getCurrentLocation().getLatitude()).floatValue(),
                Double.valueOf(this.getCurrentLocation().getLongitude()).floatValue(),
                Double.valueOf(this.getCurrentLocation().getAltitude()).floatValue(),
                System.currentTimeMillis()
        );

        // Converting the magnetic North to the true North
        float declination = geoField.getDeclination();
        azimuthDegrees += declination;

        // Get the bearing to the target location
        float bearing = this.getCurrentLocation().bearingTo(this.targetLocation);

        // Bearing gives us the angle to the destination in Degrees East of true North
        double heading = (bearing - azimuthDegrees) * -1;
        double normalizedHeading = normalizeDegree(heading);

//        azimuthDegrees = (azimuthDegrees + 360) % 360;


        boolean debug = false;
        if (debug) {
            Log.v("loc", String.format("Azimuth: %f", azimuthDegrees));
            return (float) -azimuthDegrees;
        }

        // Calculate the direction for the arrow
//        float direction = (float) azimuthDegrees - bearing;

//        Log.v("loc", String.format("Bearing: %f", bearing));
//        Log.v("loc", String.format("Azimuth: %f", azimuthDegrees));
//        Log.v("loc", String.format("Heading: %f", heading));
//        Log.v("loc", String.format("normalizedHeading: %f", normalizedHeading));
//        Log.v("loc", String.format("Direction: %f", direction));
        return -Double.valueOf(normalizedHeading).floatValue();

    }



    /* ================== View Updates Section  ================== */

    private void updateArrow() {
        try {
            ImageView arrowView = findViewById(R.id.arrow);

            float direction = this.getDirection();

//                Log.v("loc", String.format("old Rot: %f / dir: %f", oldRotation, direction));
//                RotateAnimation rotate = new RotateAnimation(arrowRotation, direction, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, .5f );
//
//                arrowRotation = direction;
////                        new RotateAnimation(oldRotation, 90, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//                rotate.setDuration(ARROW_UPDATE_DELAY * 2 / 1000000); //Math.round(Math.abs(arrowRotation - direction)/360*500));
//                rotate.setInterpolator(new LinearInterpolator());
//                arrowView.startAnimation(rotate);
//
            arrowView.setRotation(direction);


        } catch (LocationNotFoundException e) {
            updateLocationNotFoundVisibility(View.VISIBLE);
        }

    }

    private void updateSearchingFor() {
        TextView searchingForView = (TextView) findViewById(R.id.searchingForValue);
        searchingForView.setText(this.targetTreasure.toString());
    }

    private void updateAverageSpeed() {
        double averageSpeed = this.getAverageSpeed();
        TextView avgSpeedView = (TextView) findViewById(R.id.averageSpeedValue);
        avgSpeedView.setText(Formatter.formatDouble(averageSpeed, 1) + " m/s");
    }

    private void updateCurrentSpeed() {
        TextView currentSpeedView = (TextView) findViewById(R.id.currentSpeedValue);
        String text = "";

        try {
            float currentSpeed = this.getCurrentLocation().getSpeed();
            text = Formatter.formatDouble(currentSpeed, 1)+ " m/s";
        } catch (LocationNotFoundException e) {
            text = "n.a.";
        } finally {
            currentSpeedView.setText(text);
        }
    }

    private void updateDistance(String text) {
        TextView distanceView = (TextView) findViewById(R.id.distanceValue);
        distanceView.setText(text);
    }

    private void updateTemperature() {
        // TODO: handle case if temperature is not available.
        float currentTemperature = this.currentTemperature;
        TextView temperatureView = (TextView) findViewById(R.id.currentTemperatureValue);
        String temperatureString = Formatter.formatDouble(currentTemperature, 1) + " \u2103";
        temperatureView.setText(temperatureString);
    }

    private void updateTime() {
        long millis = System.currentTimeMillis() - startTime;
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;

        TextView timerTextView = findViewById(R.id.timePassedValue);
        timerTextView.setText(String.format("%d:%02d:%02d", hours, minutes, seconds));
    }

    private void updateCurrentReward() {
        int currentReward = RewardCalculator.calculateReward(getTargetTreasure(), getAverageSpeed(), getCurrentTemperature());
        TextView currentRewardView = findViewById(R.id.CurrentRewardValue);
        String text = String.format("%d coins", currentReward);
        currentRewardView.setText(text);
    }

    private void updateLocationNotFoundVisibility(int visibility) {
        TextView locationNotFound = findViewById(R.id.errorText);
        if (locationNotFound.getVisibility() != visibility) {
            locationNotFound.setVisibility(visibility);
        }
    }


    /* ================== Event Section ================== */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        // Obtain target Treasure from Intent
        this.targetTreasure = this.unserializeTreasureFromIntent();

        this.targetLocation = this.targetTreasure.getLocation();

        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        this.startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);

        this.locationTracker = new LocationTracker();

        // maybe add try / catch clause

        this.sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);

        updateSearchingFor();
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
        this.currentLocation = currentLocation;
        this.locationTracker.addSample(currentLocation);
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

    public void onAbort(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * This method is called after the user has responded to a permission request
     *
     * @param requestCode  requestCode from requestPermissions()
     * @param permissions  not used
     * @param grantResults tells us if the user has granted permissions or not
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Thank the user for granting permissions
                    Toast.makeText(this, "Thank you and have fun!", Toast.LENGTH_SHORT).show();
                    enableLocationUpdates();
                    hasUserDeniedPermissions = false;
//                    }
                } else if(grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_DENIED){
                    // We did not get the permission.
                    // Memorize this such that we don't ask again right now.
                    hasUserDeniedPermissions = true;
                }
                break;
            }
        }
    }

    private void onTargetLocationReached() {
        // This check is necessary to prevent that
        // this method is called several times for the same Quest
        if(!this.targetReached){
            this.targetReached = true;

            Quest completedQuest = new Quest(getTargetTreasure(), getAverageSpeed(), getCurrentTemperature(), QuestStatus.COMPLETED);
            GameStatus.Instance().addQuest(completedQuest);

            Intent intent = new Intent(this, TreasureFoundActivity.class);
            intent.putExtra(MainActivity.TREASURE_KEY, getTargetTreasure().getUuid());
            startActivity(intent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
//            case Sensor.TYPE_ACCELEROMETER:
//                System.arraycopy(sensorEvent.values, 0, this.mAccelerometerReading, 0, this.mAccelerometerReading.length);
//                updateDirection = true;
//                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                handleRotationUpdate(sensorEvent.values);
                break;
//            case Sensor.TYPE_GAME_ROTATION_VECTOR:
//                SensorManager.getRotationMatrixFromVector(mRotationMatrix, sensorEvent.values);
//                updateDirection = true;
//                break;
//            case Sensor.TYPE_MAGNETIC_FIELD:
//                System.arraycopy(sensorEvent.values, 0, this.mMagnetometerReading, 0, this.mMagnetometerReading.length);
//                updateDirection = true;
//                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                this.handleTemperatureUpdate(sensorEvent.values);
                break;
            default:
                break;
        }
    }


}
