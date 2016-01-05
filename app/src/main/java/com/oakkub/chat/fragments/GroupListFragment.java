package com.oakkub.chat.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oakkub.chat.R;
import com.oakkub.chat.views.adapters.ListAdapter;
import com.oakkub.chat.views.adapters.presenter.OnAdapterItemClick;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.State;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupListFragment extends BaseFragment implements OnAdapterItemClick {

    private static final String ARGS_MY_ID = "args:myId";
    private static final String TAG = GroupListFragment.class.getSimpleName();

    @Bind(R.id.recyclerview)
    RecyclerView groupRecyclerView;

    @State
    String myId;

    private ListAdapter listAdapter;

    public static GroupListFragment newInstance(String myId) {
        Bundle args = new Bundle();
        args.putString(ARGS_MY_ID, myId);

        GroupListFragment groupListFragment = new GroupListFragment();
        groupListFragment.setArguments(args);
        return groupListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDataFromArgs(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.recyclerview, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setGroupRecyclerView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        listAdapter.onSaveInstanceState("a", outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) return;

        listAdapter.onRestoreInstanceState("a", savedInstanceState);
    }

    private void getDataFromArgs(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;

        Bundle args = getArguments();
        myId = args.getString(ARGS_MY_ID);
    }

    private void setGroupRecyclerView() {

        listAdapter = new ListAdapter(getTempData(), this);

        groupRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        groupRecyclerView.setAdapter(listAdapter);

    }

    private ArrayList<String> getTempData() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(String.valueOf(i));
        }
        return list;
    }

    @Override
    public void onAdapterClick(View itemView, int position) {

    }

    @Override
    public boolean onAdapterLongClick(View itemView, int position) {
        Log.e(TAG, "onAdapterLongClick: " + listAdapter.isSelected(position) );
        listAdapter.toggleSelection(position);

        return false;
    }
}
