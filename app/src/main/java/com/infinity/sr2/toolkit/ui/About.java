package com.infinity.sr2.toolkit.ui;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.infinity.sr2.toolkit.MainActivity;
import com.infinity.sr2.toolkit.R;

public class About extends Fragment {

    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (root == null) {
            root = inflater.inflate(R.layout.fragment_about, container, false);
        }

        /*TextView appVersion = root.findViewById(R.id.app_version);
        appVersion.setText(R.string.app_version_title);*/

        return root;
    }
}