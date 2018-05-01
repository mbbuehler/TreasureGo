package ch.mbuehler.eth.mgis.treasurego;

import android.app.Activity;
import android.hardware.Camera;
import android.location.Location;
import android.opengl.Matrix;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class manages View updates for the ARView
 */
class ARViewUpdater extends ViewUpdater {

    /**
     * Displays the passed time to the user.
     */
    private TextView timerTextView;

    //Variables for GUI
    private SurfaceView surfaceView;
    private FrameLayout cameraContainerLayout;
    private AROverlayView arOverlayView;
    private ARCameraView arCamera;
    private Camera camera;

    /**
     * @param activity Activity
     */
    ARViewUpdater(ARActivity activity) {
        // Bind Views
        timerTextView = activity.findViewById(R.id.timePassedValue);
        cameraContainerLayout = activity.findViewById(R.id.camera_container_layout);
        surfaceView = activity.findViewById(R.id.surface_view);
        arOverlayView = new AROverlayView(activity, new AROverlayViewUpdater(activity));

        // This listener handles onTouchEvents, e.g. collecting ARGems
        arOverlayView.setOnTouchListener(arOverlayView.getOnTouchListener());
    }

    /**
     * Updates the field for time
     */
    private void updateTime() {
        String formattedTimeDifference = this.getFormattedTimeDifference(getDeltaTimeMillis(ARGameStatus.Instance().getStartTime()));
        timerTextView.setText(formattedTimeDifference);
    }

    /**
     * Initialize the GUI elements to draw the AR Points
     */
    void initAROverlayView() {
        if (arOverlayView.getParent() != null) {
            ((ViewGroup) arOverlayView.getParent()).removeView(arOverlayView);
        }
        cameraContainerLayout.addView(arOverlayView);
    }

    /**
     * Initialize the GUI elements and the camera View
     */
    void initARCameraView(Activity activity) {
        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }
        cameraContainerLayout.addView(surfaceView);

        if (arCamera == null) {
            arCamera = new ARCameraView(activity, surfaceView);
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
                Toast.makeText(activity, R.string.cameraNotFound, Toast.LENGTH_LONG).show();
            }
        }
    }

    void onPause() {
        // We disable the camera to save battery.
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            arCamera.setCamera(null);
            camera.release();
            camera = null;
        }
    }

    void onSensorChanged(float[] projectionMatrix, float[] rotatedProjectionMatrix, float[] rotationMatrixFromVector) {
        if (arCamera != null) {
            projectionMatrix = arCamera.getProjectionMatrix();
        }

        Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0);
        arOverlayView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);

        // It is ok to update time here because we get Sensor input all the time.
        updateTime();
    }

    void onLocationChanged(Location location) {
        if (arOverlayView != null) {
            // Propagate event to arOverlayView
            arOverlayView.updateCurrentLocation(location);
        }
    }
}

