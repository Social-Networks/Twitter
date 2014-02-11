/**
 * © 2014 Ifraag Campaign. All rights reserved. This code is only licensed and owned by Ifraag Campaign.
 * Please keep this copyright information if you are going to use this code.
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.example.twitterlogin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;


public class MyAlertDialog {

    public static void show (Context context, String title,String message ){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Setting Dialog Title
        builder.setTitle(title).
                setMessage(message).
                setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        /* TODO: Open appropriate Settings Menu to help user to get his internet connection. */
                    }
                }).show();


        /* TODO: Review Writing Style Guidelines presented by Android for the message that will be displayed in case Network
        * connection is not available. */
        /* TODO: Use apprpriate icon in case network connection is not available. */
        /*alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);*/
    }
}

