/*
   Copyright 2016 André Vieira

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.outsystemscloud.andrevieira;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

import java.io.File;


public class secureDevice extends CordovaPlugin {

    public static final String DIALOG_CLOSE_LABEL = "SecurePluginDialogCloseLabel";
    public static final String ROOTED_DEVICE_STRING = "SecurePluginRootedDeviceString";
    public static final String NO_LOCK_DEVICE_STRING = "SecurePluginNoLockSafetyString";
    public static final String CHECK_PATTERN = "CheckPattern";

    CordovaInterface cordova;
    CordovaWebView view;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.cordova = cordova;
        this.view = webView;
        checkDevice();
    }

    @Override
    public void onResume(boolean multiTasking) {
        checkDevice();
    }

    private void checkDevice() {
        boolean _isDeviceRooted = isDeviceRooted();
        boolean _isPasscodeSet = doesDeviceHaveSecuritySetup(this.cordova.getActivity());
        boolean _checkPattern = this.preferences.getBoolean(CHECK_PATTERN, true);

        if (_isDeviceRooted || (_checkPattern && !_isPasscodeSet)) {
            // Remove View
            View v = this.view.getView();
            if (v != null) {
                ViewGroup viewParent = (ViewGroup) v.getParent();
                if (viewParent != null) {
                    viewParent.removeView(v);
                }
            }

            String message = "This application does not run on a device that is rooted.";
            String dialogCloseLabel = this.preferences.getString(DIALOG_CLOSE_LABEL, "Close");

            if (_isDeviceRooted) {
                message = this.preferences.getString(ROOTED_DEVICE_STRING, message);
            } else if (!_isPasscodeSet) {
                message = this.preferences.getString(NO_LOCK_DEVICE_STRING, "This application does not run on a device that does not have a passcode set.");
            }
            this.alert(message, dialogCloseLabel);
        }
    }

    /**
     * Detect weather device is rooted or not
     *
     * @author trykov
     * @source https://github.com/trykovyura/cordova-plugin-root-detection
     */
    private boolean isDeviceRooted() {
        return checkBuildTags() || checkSuperUserApk() || checkFilePath();
    }

    private boolean checkBuildTags() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private boolean checkSuperUserApk() {
        return new File("/system/app/Superuser.apk").exists();
    }

    private boolean checkFilePath() {
        String[] paths = {"/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                "/system/bin/failsafe/su", "/data/local/su"};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }


    /**
     * <p>Checks to see if the lock screen is set up with either a PIN / PASS / PATTERN</p>
     * <p>
     * <p>For Api 16+</p>
     *
     * @return true if PIN, PASS or PATTERN set, false otherwise.
     * @author doridori
     * @source https://gist.github.com/doridori/54c32c66ef4f4e34300f
     */
    public static boolean doesDeviceHaveSecuritySetup(Context context) {
        return isPatternSet(context) || isPassOrPinSet(context);
    }

    /**
     * @param context
     * @return true if pattern set, false if not (or if an issue when checking)
     */
    private static boolean isPatternSet(Context context) {
        ContentResolver cr = context.getContentResolver();
        try {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
                int lockPatternEnable = Settings.Secure.getInt(cr, Settings.Secure.LOCK_PATTERN_ENABLED);
                return lockPatternEnable == 1;
            } else {
                KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                if (keyguardManager != null) {
                    return keyguardManager.isKeyguardSecure();
                } else {
                    return false;
                }
            }
        } catch (Settings.SettingNotFoundException e) {

            return false;
        }
    }

    /**
     * @param context
     * @return true if pass or pin set
     */
    @SuppressLint("NewApi")
    private static boolean isPassOrPinSet(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE); //api 16+
        return keyguardManager.isKeyguardSecure();
    }

    /**
     * Builds and shows a native Android alert with given Strings
     *
     * @param message         The message the alert should display
     * @param buttonLabel     The label of the button
     * @param callbackContext The callback context
     */
    private synchronized void alert(final String message, final String buttonLabel) {
        final CordovaInterface cordova = this.cordova;

        Runnable runnable = new Runnable() {
            public void run() {

                AlertDialog.Builder dlg = createDialog(cordova);
                dlg.setMessage(message);
                dlg.setCancelable(true);
                dlg.setPositiveButton(buttonLabel,
                        new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                System.exit(0);
                            }
                        });
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        public void onDismiss(final DialogInterface dialog) {
                            System.exit(0);
                        }
                    });
                }
                changeTextDirection(dlg);
            }
        };
        this.cordova.getActivity().runOnUiThread(runnable);
    }

    @SuppressLint("NewApi")
    private AlertDialog.Builder createDialog(CordovaInterface cordova) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            return new AlertDialog.Builder(cordova.getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        } else {
            return new AlertDialog.Builder(cordova.getActivity());
        }
    }

    @SuppressLint("NewApi")
    private void changeTextDirection(Builder dlg) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        dlg.create();
        AlertDialog dialog = dlg.show();
        if (currentapiVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            TextView messageview = (TextView) dialog.findViewById(android.R.id.message);
            messageview.setTextDirection(android.view.View.TEXT_DIRECTION_LOCALE);
        }
    }
}
