package org.secuso.privacyfriendlytodolist.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.view.MainActivity;

/**
 * Created by Simon on 20.09.2016.
 */

public class WelcomeDialog extends DialogFragment {
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater i = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(i.inflate(R.layout.welcome_dialog, null));
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(getActivity().getString(R.string.welcome_title));
        builder.setPositiveButton(getActivity().getString(R.string.ok), null);
        builder.setNegativeButton(getActivity().getString(R.string.welcome_viewhelp), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO switch to help view
                //((MainActivity) getActivity()).goToNavigationItem(R.id.nav_help);
            }
        });

        return builder.create();
    }
}
