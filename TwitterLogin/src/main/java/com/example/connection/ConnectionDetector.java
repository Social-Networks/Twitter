/**
 * © 2014 Ifraag Campaign. All rights reserved. This code is only licensed and owned by Ifraag Campaign.
 * Please keep this copyright information if you are going to use this code.
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.example.connection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionDetector {

    /* This method only checks if phone is connected to an internet connection or not. */
    public static boolean isConnectionAvailable(Context a_context) {

        ConnectivityManager connectivity = (ConnectivityManager) a_context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] infoArr = connectivity.getAllNetworkInfo();
            if (infoArr != null) {
                for (NetworkInfo netInfo : infoArr) {
                    if (netInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
