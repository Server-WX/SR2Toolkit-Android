package com.infinity.sr2.toolkit.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.infinity.sr2.toolkit.R;

public class CraftOther extends Fragment {

    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (root == null) {
            root = inflater.inflate(R.layout.fragment_craft_other, container, false);
        }

        return root;
    }
}