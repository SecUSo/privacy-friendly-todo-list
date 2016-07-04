package org.secuso.privacyfriendlytodolist.view.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import org.secuso.privacyfriendlytodolist.R;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;


public class PinDialog extends FullScreenDialog {

    PinCallback callback = null;

    public PinDialog(Context context) {
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
                    Toast.makeText(PinDialog.this.getContext(), PinDialog.this.getContext().getResources().getString(R.string.wrong_pin), Toast.LENGTH_SHORT).show();
                    textEditPin.setText("");
                    textEditPin.setActivated(true);
                }
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

    public interface PinCallback {
        void accepted();
        void declined();
    }

    public void setCallback(PinCallback callback) {
        this.callback = callback;
    }

}
