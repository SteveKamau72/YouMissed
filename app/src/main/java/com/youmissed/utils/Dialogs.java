package com.youmissed.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.youmissed.R;

/**
 * Created by steve on 1/31/17.
 */

public class Dialogs {

    public static void showBlockDialog(Context context) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences("general_settings", Context.MODE_PRIVATE);
        // custom dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.myDialog));

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.block_user_info, null);
        dialogBuilder.setView(dialogView);

        final CheckBox checkBox = (CheckBox) dialogView.findViewById(R.id.checkbox);
        if (sharedPreferences.getBoolean("show_block_dialog", false)) {
            checkBox.setChecked(false);
        } else {
            checkBox.setChecked(true);
        }

        dialogBuilder.setPositiveButton("Aye aye", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Boolean checked;
                if (checkBox.isChecked()) {
                    checked = false;
                } else {
                    checked = true;
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("show_block_dialog", checked);
                editor.apply();
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

    }
}
