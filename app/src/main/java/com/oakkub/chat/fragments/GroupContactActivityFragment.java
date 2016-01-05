package com.oakkub.chat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oakkub.chat.R;

import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class GroupContactActivityFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.recyclerview, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }
}
