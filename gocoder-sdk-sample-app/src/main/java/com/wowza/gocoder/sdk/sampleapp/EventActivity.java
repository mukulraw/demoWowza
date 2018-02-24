/**
 *  This is sample code provided by Wowza Media Systems, LLC.  All sample code is intended to be a reference for the
 *  purpose of educating developers, and is not intended to be used in any production environment.
 *
 *  IN NO EVENT SHALL WOWZA MEDIA SYSTEMS, LLC BE LIABLE TO YOU OR ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL,
 *  OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION,
 *  EVEN IF WOWZA MEDIA SYSTEMS, LLC HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  WOWZA MEDIA SYSTEMS, LLC SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. ALL CODE PROVIDED HEREUNDER IS PROVIDED "AS IS".
 *  WOWZA MEDIA SYSTEMS, LLC HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 *  Copyright © 2015 Wowza Media Systems, LLC. All rights reserved.
 */

package com.wowza.gocoder.sdk.sampleapp;

import android.Manifest;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.wowza.gocoder.sdk.api.data.WZDataEvent;
import com.wowza.gocoder.sdk.api.data.WZDataMap;
import com.wowza.gocoder.sdk.api.data.WZDataScope;
import com.wowza.gocoder.sdk.api.devices.WZCamera;
import com.wowza.gocoder.sdk.api.logging.WZLog;
import com.wowza.gocoder.sdk.sampleapp.ui.MultiStateButton;
import com.wowza.gocoder.sdk.sampleapp.ui.TimerView;

public class EventActivity extends CameraActivityBase {
    private final static String TAG = EventActivity.class.getSimpleName();

    // UI controls
    protected MultiStateButton      mBtnSwitchCamera  = null;
    protected MultiStateButton      mBtnTorch         = null;
    protected MultiStateButton      mBtnPing          = null;
    protected TimerView             mTimerView        = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        mRequiredPermissions = new String[] {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };

        // Initialize the UI controls
        mBtnTorch           = (MultiStateButton) findViewById(R.id.ic_torch);
        mBtnSwitchCamera    = (MultiStateButton) findViewById(R.id.ic_switch_camera);
        mBtnPing            = (MultiStateButton) findViewById(R.id.ic_ping);
        mTimerView          = (TimerView) findViewById(R.id.txtTimer);

        if (mWZBroadcast != null) {
            //
            // Registering event listeners
            //
            mWZBroadcast.registerDataEventListener("onClientConnected", new WZDataEvent.EventListener() {
                @Override
                public WZDataMap onWZDataEvent(String eventName, WZDataMap eventParams) {
                    WZLog.info(TAG, "onClientConnected data event received:\n" + eventParams.toString(true));

                    final String result = "A client connected with the IP address " + eventParams.get("clientIp");
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EventActivity.this, result, Toast.LENGTH_LONG).show();
                        }
                    });

                    // this demonstrates how to return a function result back to the original Wowza Streaming Engine
                    // function call request
                    WZDataMap functionResult = new WZDataMap();
                    functionResult.put("greeting", "Hello New Client!");

                    return functionResult;
                }
            });

            mWZBroadcast.registerDataEventListener("onClientDisconnected", new WZDataEvent.EventListener() {
                @Override
                public WZDataMap onWZDataEvent(String eventName, WZDataMap eventParams) {
                    WZLog.info(TAG, "onClientDisconnected data event received:\n" + eventParams.toString(true));

                    final String result = "A client with the IP address " +  eventParams.get("clientIp") + " disconnected";
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EventActivity.this, result, Toast.LENGTH_LONG).show();
                        }
                    });

                    return null;
                }
            });

        }
    }

    /**
     * Click handler for the ping button
     */
    public void onPing(View v) {
        //
        // Sending an event to a server module method (with a result callback)
        //
        if (mWZBroadcast != null && mWZBroadcast.getStatus().isRunning()) {
            mBtnPing.setEnabled(false);

            mWZBroadcast.sendDataEvent(WZDataScope.MODULE, "onGetPingTime", new WZDataEvent.ResultCallback() {
                @Override
                public void onWZDataEventResult(final WZDataMap resultParams, boolean isError) {
                    if(resultParams!=null) {
                        final String result = isError ? "Ping attempt failed (" + resultParams.get("code").toString() + ")" : "Ping time: " + resultParams.get("pingTime") + "ms";
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(EventActivity.this, result, Toast.LENGTH_LONG).show();
                                mBtnPing.setEnabled(true);
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //
        // Sending an event to a server module method (without a result callback)
        //
        // If a broadcast is active and the user presses on the screen,
        // send a client data event to the server with the coordinates
        // and time
        //
        if (event.getAction() == MotionEvent.ACTION_DOWN &&
                mWZBroadcast != null && mWZBroadcast.getStatus().isRunning()) {

            WZDataMap dataEventParams = new WZDataMap();
            dataEventParams.put("x", event.getX());
            dataEventParams.put("y", event.getY());
            dataEventParams.put("occurred", event.getEventTime());

            mWZBroadcast.sendDataEvent(WZDataScope.MODULE, "onScreenPress", dataEventParams);
            Toast.makeText(this, "onScreenPress() event sent to server module", Toast.LENGTH_LONG).show();

            return true;
        } else
            return super.onTouchEvent(event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //
        // Sending an event to all stream subscribers
        //
        // If a broadcast is active and the device orientation changes,
        // send a stream data event containing the device orientation
        // and rotation
        //
        if (mWZBroadcast != null && mWZBroadcast.getStatus().isRunning()) {

            WZDataMap dataEventParams = new WZDataMap();
            dataEventParams.put("deviceOrientation",
                    newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? "landscape" : "portrait");

            Display display = ((WindowManager)
                    getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            int displayRotation = display.getRotation();

            switch (displayRotation) {
                case Surface.ROTATION_0:
                    dataEventParams.put("deviceRotation", 0);
                    break;
                case Surface.ROTATION_90:
                    dataEventParams.put("deviceRotation", 90);
                    break;
                case Surface.ROTATION_180:
                    dataEventParams.put("deviceRotation", 180);
                    break;
                case Surface.ROTATION_270:
                    dataEventParams.put("deviceRotation", 270);
                    break;
            }

            mWZBroadcast.sendDataEvent(WZDataScope.STREAM, "onDeviceOrientation", dataEventParams);
            Toast.makeText(this, "onDeviceOrientation() event sent to stream subscribers", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Click handler for the switch camera button
     */
    public void onSwitchCamera(View v) {
        if (mWZCameraView == null) return;

        mBtnTorch.setState(false);
        mBtnTorch.setEnabled(false);

        WZCamera newCamera = mWZCameraView.switchCamera();
        if (newCamera != null) {
            boolean hasTorch = newCamera.hasCapability(WZCamera.TORCH);
            if (hasTorch) {
                mBtnTorch.setState(newCamera.isTorchOn());
                mBtnTorch.setEnabled(true);
            }
        }
    }

    /**
     * Click handler for the torch/flashlight button
     */
    public void onToggleTorch(View v) {
        if (mWZCameraView == null) return;

        WZCamera activeCamera = mWZCameraView.getCamera();
        activeCamera.setTorchOn(mBtnTorch.toggleState());
    }

   /**
     * Update the state of the UI controls
     */
    @Override
    protected boolean syncUIControlState() {
        boolean disableControls = super.syncUIControlState();

        if (disableControls) {
            mBtnSwitchCamera.setEnabled(false);
            mBtnTorch.setEnabled(false);
            mBtnPing.setEnabled(false);
        } else {
            boolean isDisplayingVideo = (getBroadcastConfig().isVideoEnabled() && mWZCameraView.getCameras().length > 0);
            boolean isStreaming = getBroadcast().getStatus().isRunning();

            mBtnPing.setEnabled(isStreaming);

            if (isDisplayingVideo) {
                WZCamera activeCamera = mWZCameraView.getCamera();

                boolean hasTorch = (activeCamera != null && activeCamera.hasCapability(WZCamera.TORCH));
                mBtnTorch.setEnabled(hasTorch);
                if (hasTorch) {
                    mBtnTorch.setState(activeCamera.isTorchOn());
                }

                mBtnSwitchCamera.setEnabled(mWZCameraView.getCameras().length > 0);
            } else {
                mBtnSwitchCamera.setEnabled(false);
                mBtnTorch.setEnabled(false);
            }

            if (isStreaming && !mTimerView.isRunning()) {
                mTimerView.startTimer();
            } else if (getBroadcast().getStatus().isIdle() && mTimerView.isRunning()) {
                mTimerView.stopTimer();
            } else if (!isStreaming) {
                mTimerView.setVisibility(View.GONE);
            }
        }

        return disableControls;
    }
}
