package ch.mbuehler.eth.mgis.treasurego;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Set;


public class ARActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    //Variables for GUI
    private SurfaceView surfaceView;
    private FrameLayout cameraContainerLayout;
    private AROverlayView arView;
    private Camera camera;
    private ARCameraView arCamera;
    /**
     * Responsible for updating the View elements, e.g. distance, time, etc.
     */
    ARViewUpdater viewUpdater;

    // variables for camera
    private SensorManager sensorManager;
    private final static int REQUEST_CAMERA_PERMISSIONS_CODE = 11;

    // variables for location manager
    public static final int REQUEST_LOCATION_PERMISSIONS_CODE = 0;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    private static final long MIN_TIME_BW_UPDATES = 0;

    private LocationManager locationManager;
    boolean isGPSEnabled;

    Treasure targetTreasure;
    HashMap<ARGem, Boolean> arGems;


    /**
     * Called when the activity is starting. Here we create the layout and initialize the arView, cameraContainerLayout and the TextView with the current location.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aractivity);

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        cameraContainerLayout = findViewById(R.id.camera_container_layout);
        surfaceView = findViewById(R.id.surface_view);

        // Obtain target Treasure from Intent
        targetTreasure = Treasure.unserializeTreasureFromIntent(getIntent());

        // Current altitude of user
        int currentAltitude = Integer.parseInt(getIntent().getStringExtra(Constant.ALTITUDE_KEY));

        // Create the ARGems that the user is supposed to collect
        arGems = new ARGemFactory().initializeRandomARGems(5, targetTreasure.getLocation(), 0.05, 0.15, currentAltitude);

        viewUpdater = new ARViewUpdater(this, arGems.keySet());

        arView = new AROverlayView(this, new AROverlayViewUpdater(this, arGems.keySet()));
        // This listener handles onTouchEvents, e.g. collecting ARGems
        arView.setOnTouchListener(arView.getOnTouchListener());
    }

    /**
     * Called after onCreate(), onRestoreInstanceState(Bundle), onRestart(), or onPause(), for our activity to start interacting with the user.
     * Here we check for the Permission to use FINE_LOCATION, the CAMERA and register rotational sensnor.
     */
    @Override
    public void onResume() {
        super.onResume();

        // request location permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSIONS_CODE);
        } else {
            initLocationService();
        }

        // request camera permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSIONS_CODE);
        } else {
            initARCameraView();
        }

        // register rotational sensnor
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_FASTEST);

        initAROverlayView();
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but has not (yet) been killed.
     * Here we stop listening to location updates, sensorUpdates and release the Camera to not drain the battery.
     */
    @Override
    public void onPause() {

        // Stop receiving locations from GPS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }

        // Unregister all sensors
        sensorManager.unregisterListener(this);


        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            arCamera.setCamera(null);
            camera.release();
            camera = null;
        }

        super.onPause();
    }

    /**
     * Callback for the result from requesting permissions. Checks if the user allowed to use the camera and the location.
     *
     * @param requestCode  The request code passed
     * @param permissions  The requested permissions.
     * @param grantResults The grant results for the corresponding permissions which is either PERMISSION_GRANTED or PERMISSION_DENIED.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            // This function is called when "dangerous" permissions are requested.
            case REQUEST_LOCATION_PERMISSIONS_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    initLocationService();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            case REQUEST_CAMERA_PERMISSIONS_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    initARCameraView();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    /**
     * Initialize the GUI elements to draw the AR Points
     */
    public void initAROverlayView() {

        if (arView.getParent() != null) {
            ((ViewGroup) arView.getParent()).removeView(arView);
        }
        cameraContainerLayout.addView(arView);
    }

    /**
     * Initiliaze the GUI elements and the camera View
     */
    public void initARCameraView() {

        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }

        cameraContainerLayout.addView(surfaceView);

        if (arCamera == null) {
            arCamera = new ARCameraView(this, surfaceView);
        }
        if (arCamera.getParent() != null) {
            ((ViewGroup) arCamera.getParent()).removeView(arCamera);
        }
        cameraContainerLayout.addView(arCamera);
        arCamera.setKeepScreenOn(true);

        //initCamera
        int numCams = Camera.getNumberOfCameras();
        if (numCams > 0) {
            try {
                camera = Camera.open();
                camera.startPreview();
                arCamera.setCamera(camera);
            } catch (RuntimeException ex) {
                Toast.makeText(this, R.string.cameraNotFound, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Initiliaze the LocationService
     */
    private void initLocationService() {

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

            // Get GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            }
        } catch (Exception ex) {
            Log.e("MainActivity", ex.getMessage());

        }
    }

    /**
     * Called when there is a new sensor event. We use this to recalculate our projectionMatrix according to the new phone orientation.
     *
     * @param event the SensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {

            float[] rotationMatrixFromVector = new float[16];
            float[] projectionMatrix = new float[16];
            float[] rotatedProjectionMatrix = new float[16];

            SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, event.values);

            if (arCamera != null) {
                projectionMatrix = arCamera.getProjectionMatrix();
            }

            Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0);
            this.arView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);
        }
    }

    /**
     * Called when the accuracy of the registered sensor has changed.
     * We do nothing when this event happens.
     *
     * @param sensor   the Sensor that has ne accuracy
     * @param accuracy The new accuracy of this sensor
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //do nothing
    }

    /**
     * Called when a new user location is known. If this happens, we need to update the currentLocation for the ARView and the TextView that displays the current location.
     *
     * @param location The new location
     */
    @Override
    public void onLocationChanged(Location location) {
        if (arView != null) {
            arView.updateCurrentLocation(location);
            viewUpdater.updateTVCurrentLocation(location);
            viewUpdater.updateTime();
        }
    }

    /**
     * Called when the provider status changes.
     * We do nothing when this event happens.
     *
     * @param provider The name of the location provider associated with this update.
     * @param status   OUT_OF_SERVICE if the provider is out of service, and this is not expected to change in the near future; TEMPORARILY_UNAVAILABLE if the provider is temporarily unavailable but is expected to be available shortly; and AVAILABLE if the provider is currently available.
     * @param extras   an optional Bundle which will contain provider specific status variables.
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //do nothing
    }

    /**
     * Called when the provider is enabled by the user.
     * We do nothing when this event happens.
     *
     * @param provider the name of the location provider associated with this update.
     */
    @Override
    public void onProviderEnabled(String provider) {
        //do nothing
    }

    /**
     * Called when the provider is disabled by the user. If requestLocationUpdates is called on an already disabled provider, this method is called immediately.
     * We do nothing when this event happens. However, we could tell the user to re-enable the provider as our app uses it.
     *
     * @param provider the name of the location provider associated with this update.
     */
    @Override
    public void onProviderDisabled(String provider) {
        //do nothing
    }

    public Set<ARGem> getARGems(){
        return arGems.keySet();
    }

    public void removeARGem(ARGem arGem){
        this.arGems.remove(arGem);
    }
}
