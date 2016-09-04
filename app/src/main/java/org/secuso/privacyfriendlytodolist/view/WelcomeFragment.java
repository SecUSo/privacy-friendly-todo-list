package org.secuso.privacyfriendlytodolist.view;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.secuso.privacyfriendlytodolist.R;


public class WelcomeFragment extends Fragment {

    private final static String TAG = WelcomeFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View baseView = inflater.inflate(R.layout.fragment_welcome, container, false);
        Button buttonBegin = (Button) baseView.findViewById(R.id.b_welcome_begin);
        buttonBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("firstStart", false);
                editor.commit();
                ((MainActivity)getActivity()).setFragment(new TodoListsFragment());
            }
        });

        return baseView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }
}
