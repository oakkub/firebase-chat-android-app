package com.oakkub.chat.views.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.oakkub.chat.R;

/**
 * Created by OaKKuB onAlertDialogListener 2/11/2016.
 */
public class ListDialogFragment extends DialogFragment implements ListView.OnItemClickListener {

    private static final String ARGS_TITLE = "args:title";
    private static final String ARGS_LIST_ITEMS = "args:listItems";

    private OnListDialogClickListener onListDialogClickListener;

    public static ListDialogFragment newInstance(@NonNull String title, String[] listItems) {
        Bundle args = new Bundle();
        args.putString(ARGS_TITLE, title);
        args.putStringArray(ARGS_LIST_ITEMS, listItems);

        ListDialogFragment dialogFragment = new ListDialogFragment();
        dialogFragment.setArguments(args);

        return dialogFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            if (getParentFragment() != null) {
                onListDialogClickListener = (OnListDialogClickListener) getParentFragment();
            } else if (getTargetFragment() != null) {
                onListDialogClickListener = (OnListDialogClickListener) getTargetFragment();
            } else {
                onListDialogClickListener = (OnListDialogClickListener) getActivity();
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        onListDialogClickListener = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = args.getString(ARGS_TITLE);
        String[] listItems = args.getStringArray(ARGS_LIST_ITEMS);

        int padding = getResources().getDimensionPixelSize(R.dimen.spacing_super_medium);

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(getContentView(listItems), 0, padding, 0, padding)
                .create();
    }

    private ListView getContentView(String[] listItems) {
        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1);
        itemsAdapter.addAll(listItems);

        ListView listView = new ListView(getActivity());
        listView.setDivider(null);
        listView.setAdapter(itemsAdapter);
        listView.setOnItemClickListener(this);

        return listView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dismiss();
        onListDialogClickListener.onDialogItemClick(getTag(), position);
    }

    public interface OnListDialogClickListener {
        void onDialogItemClick(String tag, int position);
    }
}
