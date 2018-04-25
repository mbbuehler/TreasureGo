package ch.mbuehler.eth.mgis.treasurego;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.opengl.Matrix;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.List;

/**
 * Created by vanagnos on 03.04.2017. Updated by goebelf on 18.04.2018.
 * We extend the ViewGroup Class of Android (see also: https://developer.android.com/reference/android/view/ViewGroup.html)
 * so we could visualize the camera of our smartphone. We are using a ViewGroup, so we could
 * also paint the ARPoints over the cameraView.
 */

/*
 * From Android 5.0(API Level 21) the new Camera2 API(android.hardware.Camera2) is introduced
 * which now gives full manual control over Android device cameras. With previous
 * Camera API(android.hardware.Camera), manual controls for the camera were only accessible
 * by making changes to OS and existing APIs which wasn't friendly.
 * The old Camera API (android.hardware.Camera) is now deprecated on Android 5.0 but only the latest
 * devices support it. For that reason we will use the old API, but eventually someone will also
 * have to rewrite his application to make it compatible with the new API. For that reason we are
 * using the SuppressWarnings, so we won't have any messages from the IDE, warning us, that we are
 * using an API that is deprecated.
 */
@SuppressWarnings("deprecation")
public class ARCameraView extends ViewGroup implements SurfaceHolder.Callback {

    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    Camera.Size previewSize;
    List<Camera.Size> supportedPreviewSizes;
    public Camera camera;

    Camera.Parameters parameters;
    Activity activity;

    float[] projectionMatrix = new float[16];

    int cameraWidth;
    int cameraHeight;
    private final static float Z_NEAR = 0.5f;
    private final static float Z_FAR = 2000;

    /**
     * Constructor of the ARCameraView. It takes the context and a SurfaceView as parameters.
     * The SurfaceView is used to draw the AR content on the Camera arActivityView.
     *
     * @param context     context of the ARCameraView
     * @param surfaceView drawing surface for drawing on the camera arActivityView
     */
    public ARCameraView(Context context, SurfaceView surfaceView) {
        super(context);

        this.surfaceView = surfaceView;
        this.activity = (Activity) context;
        surfaceHolder = this.surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * Sets the camera for the ARCamera.
     *
     * @param camera the camera object
     */
    public void setCamera(Camera camera) {
        this.camera = camera;
        if (this.camera != null) {
            supportedPreviewSizes = this.camera.getParameters().getSupportedPreviewSizes();
            Camera.Parameters params = this.camera.getParameters();

            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                this.camera.setParameters(params);
            }

            requestLayout();
        }
    }

    /**
     * Measure the arActivityView and its content to determine the measured width and the measured height.
     * See https://developer.android.com/guide/topics/ui/how-android-draws.html
     *
     * @param widthMeasureSpec  horizontal space requirements as imposed by the parent.
     * @param heightMeasureSpec vertical space requirements as imposed by the parent.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (supportedPreviewSizes != null) {
            previewSize = supportedPreviewSizes.get(0);
            // Here we are using the first supported preview size
            // You can also implement a function (e.g. getOptimalPreviewSize())
            // that will retrieve the optimal size for the app
        }
    }

    /**
     * Called from layout when this arActivityView should assign a size and position to each of its children.
     *
     * @param changed Indicates if this is a new size or position for this arActivityView
     * @param left    Left position, relative to parent
     * @param top     Top position, relative to parent
     * @param right   Right position, relative to parent
     * @param bottom  Bottom position, relative to parent
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = right - left;
            final int height = bottom - top;

            int previewWidth = width;
            int previewHeight = height;
            if (previewSize != null) {
                previewWidth = previewSize.width;
                previewHeight = previewSize.height;
            }

            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2,
                        width, (height + scaledChildHeight) / 2);
            }
        }
    }

    /**
     * The callback method which is called immediately after the surface is first created.
     *
     * @param holder the SurfaceHolder whose surface is being created.
     */
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (camera != null) {

                parameters = camera.getParameters();

                // in the android manifest, we explicity said that the orientation of our Activity
                // will be "portrait". For that reason we do not have to check the orientation of the
                // device. Otheriwise, we would have to write a new function (e.g. getCameraOrientation())
                // that checks the orientation of the device.
                int orientation = 90;
                camera.setDisplayOrientation(90);
                camera.getParameters().setRotation(90);

                camera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e("ARCameraView", "IOException caused by setPreviewDisplay()", exception);
        }
    }

    /**
     * The callback method which is called immediately before a surface is being destroyed.
     * After returning from this call, you should no longer try to access this surface.
     *
     * @param holder the SurfaceHolder whose surface is being destroyed.
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    /**
     * The callback method which gets called immediately after any structural changes (format or size) have been made to the surface.
     *
     * @param holder The SurfaceHolder whose surface has changed.
     * @param format The new PixelFormat of the surface.
     * @param width  The new width of the surface.
     * @param height The new height of the surface.
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (camera != null) {
            this.cameraWidth = width;
            this.cameraHeight = height;

            Camera.Parameters params = camera.getParameters();

            params.setPreviewSize(previewSize.width, previewSize.height);
            requestLayout();

            camera.setParameters(params);
            camera.startPreview();

            // generate the Projection Matrix using frustum matrix
            // see also: http://www.songho.ca/opengl/gl_projectionmatrix.html
            float ratio = (float) this.cameraWidth / this.cameraHeight;
            final int OFFSET = 0;
            final float LEFT = -ratio;
            final float RIGHT = ratio;
            final float BOTTOM = -1;
            final float TOP = 1;
            Matrix.frustumM(projectionMatrix, OFFSET, LEFT, RIGHT, BOTTOM, TOP, Z_NEAR, Z_FAR);
        }
    }

    /**
     * Returns the projection matrix.
     *
     * @return The ProjectionMatrix
     */
    public float[] getProjectionMatrix() {
        return projectionMatrix;
    }
}