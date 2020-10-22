/*
 * Copyright 2018 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.vr.sdk.samples.homerunner;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import com.google.vr.ndk.base.Properties;
import com.google.vr.ndk.base.Properties.PropertyType;
import com.google.vr.ndk.base.Value;
import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * A Google VR sample application.
 *
 * <p>This app presents a scene consisting of a room and a floating object. When the user finds the
 * object, they can invoke the trigger action, and a new object will be randomly spawned. When in
 * Cardboard mode, the user must gaze at the object and use the Cardboard trigger button. When in
 * Daydream mode, the user can use the controller to position the cursor, and use the controller
 * buttons to invoke the trigger action.
 */
public class   HelloVrActivity extends GvrActivity implements GvrView.StereoRenderer, SensorEventListener {
  private static final String TAG = "HelloVrActivity";

  private static final float Z_NEAR = 0.01f;
  private static final float Z_FAR = 10.0f;

  // Convenience vector for extracting the position from a matrix via multiplication.
  private static final float[] POS_MATRIX_MULTIPLY_VEC = {0.0f, 0.0f, 0.0f, 1.0f};
  private static final float[] FORWARD_VEC = {0.0f, 0.0f, -1.0f, 1.f};

  private static final float DEFAULT_FLOOR_HEIGHT = -1.6f;

  private static final float ANGLE_LIMIT = 0.2f;

  // The maximum yaw and pitch of the target object, in degrees. After hiding the target, its
  // yaw will be within [-MAX_YAW, MAX_YAW] and pitch will be within [-MAX_PITCH, MAX_PITCH].
//  private static final float MAX_YAW = 100.0f;
//  private static final float MAX_PITCH = 25.0f;

  private static final String[] OBJECT_VERTEX_SHADER_CODE =
          new String[] {
                  "uniform mat4 u_MVP;",
                  "attribute vec4 a_Position;",
                  "attribute vec2 a_UV;",
                  "varying vec2 v_UV;",
                  "",
                  "void main() {",
                  "  v_UV = a_UV;",
                  "  gl_Position = u_MVP * a_Position;",
                  "}",
          };
  private static final String[] OBJECT_FRAGMENT_SHADER_CODE =
          new String[] {
                  "precision mediump float;",
                  "varying vec2 v_UV;",
                  "uniform sampler2D u_Texture;",
                  "",
                  "void main() {",
                  "  // The y coordinate of this sample's textures is reversed compared to",
                  "  // what OpenGL expects, so we invert the y coordinate.",
                  "  gl_FragColor = texture2D(u_Texture, vec2(v_UV.x, 1.0 - v_UV.y));",
                  "}",
          };

  protected int objectProgram;

  protected int objectPositionParam;
  protected int objectUvParam;
  protected int objectModelViewProjectionParam;

  protected TexturedMesh room;
  protected Texture roomTex;

  private float[] camera;
  private float[] view;
  private float[] headView;
  private float[] modelViewProjection;
  private float[] modelView;

  private float[] modelRoom;

  private float[] tempPosition;
  private float[] headRotation;

  private Properties gvrProperties;
  // This is an opaque wrapper around an internal GVR property. It is set via Properties and
  // should be shutdown via a {@link Value#close()} call when no longer needed.
  private final Value floorHeight = new Value();

  private SensorManager sensorManager;
  private Sensor stepDetector;


  private StreetViewLoader sVLoader;

  private Bitmap sVBitmap;

  private static final int DISTANCE_PER_STEP = 10;

  private ArrayList<JSONObject> arrSteps;

  private int distanceElapsed;
  private int curStepIndex;
  private int curStepDistance;

  /**
   * Sets the view to our GvrView and initializes the transformation matrices we will use
   * to render our scene.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.i(TAG,"onCreate");

    super.onCreate(savedInstanceState);

    sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
    stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

    sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_FASTEST);

    sVLoader = new StreetViewLoader(this);

    distanceElapsed = 0;
    curStepIndex = 0;
    curStepDistance = 0;

    arrSteps = new ArrayList<>();

    loadJSONDir();

    initializeGvrView();

    camera = new float[16];
    view = new float[16];
    modelViewProjection = new float[16];
    modelView = new float[16];
    // Target object first appears directly in front of user.
    tempPosition = new float[4];
    headRotation = new float[4];
    modelRoom = new float[16];
    headView = new float[16];
  }

  public void initializeGvrView() {
    setContentView(R.layout.common_ui);

    GvrView gvrView = findViewById(R.id.gvr_view);
    gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

    gvrView.setRenderer(this);
    gvrView.setTransitionViewEnabled(true);

    // Enable Cardboard-trigger feedback with Daydream headsets. This is a simple way of supporting
    // Daydream controller input for basic interactions using the existing Cardboard trigger API.
    gvrView.enableCardboardTriggerEmulation();

    if (gvrView.setAsyncReprojectionEnabled(true)) {
      // Async reprojection decouples the app framerate from the display framerate,
      // allowing immersive interaction even at the throttled clockrates set by
      // sustained performance mode.
      AndroidCompat.setSustainedPerformanceMode(this, true);
    }

    setGvrView(gvrView);
    gvrProperties = gvrView.getGvrApi().getCurrentProperties();
  }

  @Override
  public void onPause() {
    super.onPause();
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onRendererShutdown() {
    Log.i(TAG, "onRendererShutdown");
    floorHeight.close();
  }

  @Override
  public void onSurfaceChanged(int width, int height) {
    String filepath = getIntent().getStringExtra("bitmap_texture");
    try {
      room = new TexturedMesh(this, "Room.obj", objectPositionParam, objectUvParam);
      roomTex = new Texture(this, filepath);
    } catch (IOException e) {
      Log.e(TAG, "Unable to initialize objects", e);
    }
    drawRoom();
    Log.i(TAG, "onSurfaceChanged");
  }



  /**
   * Creates the buffers we use to store information about the 3D world.
   *
   * <p>OpenGL doesn't use Java arrays, but rather needs data in a format it can understand.
   * Hence we use ByteBuffers.
   *
   * @param config The EGL configuration used when creating the surface.
   */
  @Override
  public void onSurfaceCreated(EGLConfig config) {
    Log.i(TAG, "onSurfaceCreated");
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    objectProgram = Util.compileProgram(OBJECT_VERTEX_SHADER_CODE, OBJECT_FRAGMENT_SHADER_CODE);

    objectPositionParam = GLES20.glGetAttribLocation(objectProgram, "a_Position");
    objectUvParam = GLES20.glGetAttribLocation(objectProgram, "a_UV");
    objectModelViewProjectionParam = GLES20.glGetUniformLocation(objectProgram, "u_MVP");

    Util.checkGlError("Object program params");

    Matrix.setIdentityM(modelRoom, 0);
    Matrix.translateM(modelRoom, 0, 0, DEFAULT_FLOOR_HEIGHT, 0);

    String filepath = getIntent().getStringExtra("bitmap_texture");

    try {
      room = new TexturedMesh(this, "Room.obj", objectPositionParam, objectUvParam);
      roomTex = new Texture(this, filepath);
    } catch (IOException e) {
      Log.e(TAG, "Unable to initialize objects", e);
    }
  }

  /**
   * Prepares OpenGL ESm before we draw a frame.
   *
   * @param headTransform The head transformation in the new frame.
   */
  @Override
  public void onNewFrame(HeadTransform headTransform) {
    // Build the camera matrix and apply it to the ModelView.
    Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f);

    if (gvrProperties.get(PropertyType.TRACKING_FLOOR_HEIGHT, floorHeight)) {
      // The floor height can change each frame when tracking system detects a new floor position.
      Matrix.setIdentityM(modelRoom, 0);
      Matrix.translateM(modelRoom, 0, 0, floorHeight.asFloat(), 0);
    } // else the device doesn't support floor height detection so DEFAULT_FLOOR_HEIGHT is used.

    headTransform.getHeadView(headView, 0);

    headTransform.getQuaternion(headRotation, 0);
    Util.checkGlError("onNewFrame");
  }

  /**
   * Draws a frame for an eye.
   *
   * @param eye The eye to render. Includes all required transformations.
   */
  @Override
  public void onDrawEye(Eye eye) {
    GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    // The clear color doesn't matter here because it's completely obscured by
    // the room. However, the color buffer is still cleared because it may
    // improve performance.
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    // Apply the eye transformation to the camera.
    Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

    // Build the ModelView and ModelViewProjection matrices
    // for calculating the position of the target object.
    float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);

    Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);

    // Set modelView for the room, so it's drawn in the correct location
    Matrix.multiplyMM(modelView, 0, view, 0, modelRoom, 0);
    Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
    drawRoom();
  }

  @Override
  public void onFinishFrame(Viewport viewport) {}

  /** Draw the room. */
  public void drawRoom() {
    GLES20.glUseProgram(objectProgram);
    GLES20.glUniformMatrix4fv(objectModelViewProjectionParam, 1, false, modelViewProjection, 0);
    roomTex.bind();
    room.draw();
    Util.checkGlError("drawRoom");
  }

  /**
   * Called when the Cardboard trigger is pulled.
   */
  @Override
  public void onCardboardTrigger() {
    Log.i(TAG, "onCardboardTrigger");
  }


  public void loadJSONDir(){
    File file = new File(getCacheDir(),"dir_route.json");

    String jsonTemp = "";

    JSONObject jsonDir;

    try {
      BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

      String curLine = bufferedReader.readLine();

      while (curLine != null){
        jsonTemp += curLine;
        curLine = bufferedReader.readLine();
      }

      jsonDir = new JSONObject(jsonTemp);

      JSONArray jsonArrRoute = jsonDir.getJSONArray("routes");

      for(int i = 0; i < jsonArrRoute.length(); i++){
        JSONObject jsonRoute = jsonArrRoute.getJSONObject(i);

        JSONArray jsonArrLegs = jsonRoute.getJSONArray("legs");

        for(int j = 0; j < jsonArrLegs.length(); j++){
          JSONObject jsonLegs = jsonArrLegs.getJSONObject(j);

          JSONArray jsonArrSteps = jsonLegs.getJSONArray("steps");

          for(int k = 0; k < jsonArrSteps.length(); k++){
            arrSteps.add(jsonArrSteps.getJSONObject(k));
          }
        }
      }
      curStepDistance = getDistanceValue();
    }catch (Exception ex){

    }
  }

  public int getDistanceValue() throws JSONException {
      return arrSteps.get(curStepIndex).getJSONObject("distance").getInt("value");
  }

  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
    Log.i("Sensor", "Step Detector");

    if(sensorEvent.sensor == this.stepDetector){
      if(curStepIndex < arrSteps.size()){
        distanceElapsed += DISTANCE_PER_STEP;

        if(distanceElapsed % 50 == 0) {

          sVLoader = new StreetViewLoader(this);

          int heading = 0;

          int svUrlLength = 4;

          String[] urlArr = new String[svUrlLength];

          String svTempURL;

          double lat = 0, lng = 0;

          try {
            lat = arrSteps.get(curStepIndex).getJSONObject("end_location").getDouble("lat");

            lng = arrSteps.get(curStepIndex).getJSONObject("end_location").getDouble("lng");

          } catch (Exception ex){

          }

          svTempURL = "https://maps.googleapis.com/maps/api/streetview?size=600x300&location=" + lat + ","+ lng  + "&key=" + getString(R.string.key)
                  + "&heading=";

          for (int j = 0; j < svUrlLength; j++) {
            urlArr[j] = svTempURL + heading;
            heading += 90;
          }

          sVLoader.execute(urlArr);

          curStepIndex++;

        } else if(distanceElapsed >= curStepDistance){
          curStepIndex++;
        }
      }

      Log.i("Distance Elapsed",distanceElapsed+"");
      Log.i("Current Step Distance", curStepDistance+"");

    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int i) {
  }
}



//Portions of this page are modifications based on work created and shared by Google
// and used according to terms described in the Creative Commons 4.0 Attribution License.