/*
 This file is part of Privacy Friendly To-Do List.

 Privacy Friendly To-Do List is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly To-Do List is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly To-Do List. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlytodolist.view.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import org.secuso.privacyfriendlytodolist.R;


public class PinDialog extends FullScreenDialog<PinDialog.PinCallback> {

    public interface PinCallback {
        void accepted();
        void declined();
        void resetApp();
    }

    private int wrongCounter = 0;
    private boolean disallowReset = false;

    public PinDialog(final Context context) {
        super(context, R.layout.pin_dialog);

        Button buttonOkay = (Button) findViewById(R.id.bt_pin_ok);
        buttonOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pinExpected = PreferenceManager.getDefaultSharedPreferences(PinDialog.this.getContext()).getString("pref_pin", "");
                EditText textEditPin = (EditText)findViewById(R.id.et_pin_pin);
                if(pinExpected.equals(textEditPin.getText().toString())) {
                    callback.accepted();
                    setOnDismissListener(null);
                    dismiss();
                }
                else {
                    wrongCounter++;
                    Toast.makeText(PinDialog.this.getContext(), PinDialog.this.getContext().getResources().getString(R.string.wrong_pin), Toast.LENGTH_SHORT).show();
                    textEditPin.setText("");
                    textEditPin.setActivated(true);

                    if (wrongCounter >= 3 && !disallowReset) {
                        Button buttonResetApp = (Button)findViewById(R.id.bt_reset_application);
                        buttonResetApp.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        Button buttonResetApp = (Button)findViewById(R.id.bt_reset_application);
        buttonResetApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnClickListener resetDialogListener = new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            callback.resetApp();
                        }
                    }
                };

                Context context = PinDialog.this.getContext();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                Resources resources = context.getResources();
                builder.setMessage(resources.getString(R.string.reset_application_msg));
                builder.setPositiveButton(resources.getString(R.string.yes), resetDialogListener);
                builder.setNegativeButton(resources.getString(R.string.no), resetDialogListener);
                builder.show();
            }
        });

        Button buttonNoDeadline = (Button) findViewById(R.id.bt_pin_cancel);
        buttonNoDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.declined();
                dismiss();
            }
        });

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                callback.declined();
            }
        });

        EditText textEditPin = (EditText)findViewById(R.id.et_pin_pin);
        textEditPin.setActivated(true);
    }

    public void setDisallowReset(Boolean disallow) {
        this.disallowReset = disallow;
    }

}
