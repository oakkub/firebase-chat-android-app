package com.oakkub.chat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by OaKKuB on 2/4/2016.
 */
public class KeyFirebaseFetchingFragment extends BaseFragment {

    private static final String ARGS_KEY_TO_BE_FETCHED = "args:keyToBeFetched";
    private static final String TAG = KeyFirebaseFetchingFragment.class.getSimpleName();

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Firebase firebase;

    private OnItemReceivedListener onItemReceivedListener;
    private String keyToBeFetched;
    private ArrayList<HashMap<String, Object>> resultList;

    public static KeyFirebaseFetchingFragment newInstance(String keyToBeFetched) {
        Bundle args = new Bundle();
        args.putString(ARGS_KEY_TO_BE_FETCHED, keyToBeFetched);

        KeyFirebaseFetchingFragment fragment = new KeyFirebaseFetchingFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        onItemReceivedListener = (OnItemReceivedListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);
        getDataArgs();

        resultList = new ArrayList<>();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        onItemReceivedListener = null;
    }

    private void getDataArgs() {
        Bundle args = getArguments();

        keyToBeFetched = args.getString(ARGS_KEY_TO_BE_FETCHED);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (resultList != null) {
            onItemReceivedListener.onItemReceived(resultList);
            resultList = null;
        }

        fetch();
    }

    public void fetch() {
        firebase.child(keyToBeFetched)
                .limitToLast(20)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<HashMap<String, Object>> map =
                                new ArrayList<>((int) dataSnapshot.getChildrenCount());

                        for (DataSnapshot children : dataSnapshot.getChildren()) {
                            HashMap<String, Object> result = (HashMap<String, Object>) children.getValue();
                            map.add(result);
                        }

                        if (onItemReceivedListener != null) {
                            onItemReceivedListener.onItemReceived(resultList);
                        } else {
                            resultList = map;
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e(TAG, "onCancelled: " + firebaseError.getMessage() );
                    }
                });
    }

    public interface OnItemReceivedListener {
        void onItemReceived(List<HashMap<String, Object>> map);
    }
}
