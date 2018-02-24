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

package com.wowza.gocoder.sdk.sampleapp.graphics;

import android.Manifest;
import android.os.Bundle;

import com.wowza.gocoder.sdk.api.android.graphics.WZText;
import com.wowza.gocoder.sdk.api.android.graphics.WZTextManager;
import com.wowza.gocoder.sdk.sampleapp.CameraActivityBase;
import com.wowza.gocoder.sdk.sampleapp.R;

import java.util.UUID;

/**
 * This activity class demonstrates use of the WText API to display a text string
 * as an overlay within the GoCoder SDK camera preview display and resulting video stream
 */
public class TextOverlayActivity extends CameraActivityBase {
    private final static String TAG = TextOverlayActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overlay);

        mRequiredPermissions = new String[] {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };

        if (sGoCoderSDK != null) {
            // Get the GoCoder SDK text manager
            WZTextManager wzTextManager = WZTextManager.getInstance();

            // Load a True Type font file from the app's assets folder
            UUID fontId = wzTextManager.loadFont("njnaruto.ttf", 100, 15, 0);

            // Display the text centered on the screen
            WZText textObject = wzTextManager.createTextObject(fontId, "Become a Wowza Ninja", 0.84f, 0.47f, 0f);
            textObject.setPosition(WZText.CENTER, WZText.CENTER);
            textObject.setAlignment(WZText.CENTER);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (sGoCoderSDK != null)
            WZTextManager.getInstance().clear();
    }
}
