package com.oakkub.chat.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.oakkub.chat.R;
import com.oakkub.chat.activities.BaseActivity;
import com.oakkub.chat.activities.ChatRoomActivity;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.utils.FileUtil;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.FrescoUtil;
import com.oakkub.chat.utils.IntentUtil;
import com.oakkub.chat.utils.UriUtil;
import com.oakkub.chat.views.dialogs.ChooseImageDialog;
import com.oakkub.chat.views.dialogs.ProgressDialogFragment;
import com.oakkub.chat.views.widgets.MyToast;
import com.oakkub.chat.views.widgets.spinner.MySpinner;

import org.parceler.Parcels;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.State;

public class RoomCreationFragment extends BaseFragment implements
        ChooseImageDialog.ChooseImageDialogListener, NewPublicChatFragment.NewPublicChatListener {

    private static final String TAG = RoomCreationFragment.class.getSimpleName();
    private static final String NEW_PUBLIC_CHAT_FRAGMENT_TAG = "tag:publicChatFragment";
    private static final String CHOOSE_IMAGE_DIALOG_TAG = "tag:chooseImageDialog";
    private static final String TAG_PROGRESS_DIALOG = "tag:progressDialogFragment";
    private static final String INPUT_ROOM_STATE = "state:inputRoom";
    private static final int CAMERA_REQUEST_CODE = 0;
    private static final int IMAGE_VIEWER_REQUEST_CODE = 1;

    @Bind(R.id.simple_toolbar)
    Toolbar toolbar;

    @Bind(R.id.public_chat_image)
    SimpleDraweeView publicChatImage;

    @Bind(R.id.public_chat_name_edittext)
    EditText nameEditText;

    @Bind(R.id.public_chat_optional_desc_edittext)
    EditText descriptionEditText;

    @Bind(R.id.public_chat_type_spinner)
    MySpinner tagSpinner;

    @State
    String myId;

    @State
    String absolutePath;

    @State
    int maxLengthName;

    @State
    int maxLengthDescription;

    @State
    Uri uriImage;

    @State
    String[] spinnerKeys;

    @State
    String[] spinnerValues;

    private Room inputRoom;

    private OnRoomCreationListener onRoomCreationListener;
    private NewPublicChatFragment newPublicChatFragment;

    public static RoomCreationFragment newInstance(String myId) {
        Bundle args = new Bundle();
        args.putString(ARGS_MY_ID, myId);

        RoomCreationFragment fragment = new RoomCreationFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        onRoomCreationListener = (OnRoomCreationListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDataArgs(savedInstanceState);
        getDataResources(savedInstanceState);

        if (savedInstanceState.containsKey(INPUT_ROOM_STATE)) {
            inputRoom = Parcels.unwrap(savedInstanceState.getParcelable(INPUT_ROOM_STATE));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_public_chat_creation, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setToolbar();
        setSpinner();

        FragmentManager fragmentManager = getChildFragmentManager();
        newPublicChatFragment = (NewPublicChatFragment) fragmentManager.findFragmentByTag(NEW_PUBLIC_CHAT_FRAGMENT_TAG);
        if (newPublicChatFragment == null) {
            newPublicChatFragment = NewPublicChatFragment.newInstance(myId);
            fragmentManager.beginTransaction()
                    .add(newPublicChatFragment, NEW_PUBLIC_CHAT_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (inputRoom != null) {
            outState.putParcelable(INPUT_ROOM_STATE, Parcels.wrap(inputRoom));
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sendResultBack();
    }

    private void sendResultBack() {
        if (onRoomCreationListener != null && inputRoom != null) {
            onRoomCreationListener.onInputSend(inputRoom, uriImage, absolutePath);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onCameraResult(requestCode, resultCode);
        onImageViewerResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (uriImage != null) {
            publicChatImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    showResizedImageByUri();
                    publicChatImage.getViewTreeObserver().removeOnPreDrawListener(this);
                    return true;
                }
            });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        onRoomCreationListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_ok, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_ok:
                return handleOkActionClick();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getDataArgs(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;
        Bundle args = getArguments();

        myId = args.getString(ARGS_MY_ID);
    }

    private void getDataResources(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;
        Resources res = getResources();

        maxLengthName = res.getInteger(R.integer.max_length_public_chat_name);
        maxLengthDescription = res.getInteger(R.integer.max_length_optional_description);
        spinnerKeys = res.getStringArray(R.array.public_chat_tag_keys);
        spinnerValues = res.getStringArray(R.array.public_chat_tag_values);
    }

    private void setToolbar() {
        BaseActivity activity = (BaseActivity) getActivity();
        activity.setSupportActionBar(toolbar);

        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.new_public_chat));
        }
    }

    private void setSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item, spinnerKeys);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        tagSpinner.setAdapter(spinnerAdapter);
    }

    @OnClick(R.id.public_chat_image)
    public void onPublicChatImageClick() {
        ChooseImageDialog chooseImageDialog = ChooseImageDialog.newInstance(this);
        chooseImageDialog.show(getChildFragmentManager(), CHOOSE_IMAGE_DIALOG_TAG);
    }

    @Override
    public void onCameraClick() {
        File cameraStorage = FileUtil.getCameraStorage();
        if (cameraStorage == null) {
            MyToast.make("Cannot use camera, no sdcard available.").show();
            return;
        }

        uriImage = Uri.fromFile(cameraStorage);

        Intent cameraIntent = IntentUtil.openCamera(getActivity(), uriImage);
        if (cameraIntent != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onGalleryClick() {
        Intent imageViewerIntent = IntentUtil.openImageViewer(getActivity(), false);
        if (imageViewerIntent != null) {
            startActivityForResult(imageViewerIntent, IMAGE_VIEWER_REQUEST_CODE);
        }
    }

    @Override
    public void onPublicChatCreated(Room room) {
        Intent roomIntent = ChatRoomActivity.getIntentPublicRoom(getActivity(), room, myId, true);
        startActivity(roomIntent);
        getActivity().finish();
    }

    @Override
    public void onPublicChatFailed() {
        ProgressDialogFragment progressDialog =
                (ProgressDialogFragment) getChildFragmentManager().findFragmentByTag(TAG_PROGRESS_DIALOG);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        MyToast.make(getString(R.string.error_creating_room)).show();
    }

    @SuppressWarnings("unchecked")
    private boolean handleOkActionClick() {
        String roomName = nameEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (roomName.isEmpty()) {
            nameEditText.setError(getString(R.string.error_room_name_empty));
            return false;
        }

        if (roomName.length() > maxLengthName) {
            nameEditText.setError(getString(R.string.error_room_name_exceeds_limit));
            return false;
        }

        if (description.length() > maxLengthDescription) {
            descriptionEditText.setError(getString(R.string.error_room_description_exceeds_limit));
            return false;
        }

        ProgressDialogFragment progressDialog = ProgressDialogFragment.newInstance();
        progressDialog.show(getChildFragmentManager(), TAG_PROGRESS_DIALOG);

        String tagValue = spinnerValues[tagSpinner.getSelectedItemPosition()];
        Room room = new Room(FirebaseUtil.VALUE_ROOM_TYPE_PUBLIC);
        room.setName(roomName);
        room.setTag(tagValue);
        if (!description.isEmpty()) {
            room.setDescription(description);
        }
        newPublicChatFragment.createPublicChat(room, uriImage, absolutePath);

        return true;
    }

    private void onImageViewerResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != IMAGE_VIEWER_REQUEST_CODE || resultCode != Activity.RESULT_OK) return;

        uriImage = data.getData();
        absolutePath = UriUtil.getPath(uriImage);
        showResizedImageByUri();
    }

    private void onCameraResult(int requestCode, int resultCode) {
        if (requestCode != CAMERA_REQUEST_CODE || resultCode != Activity.RESULT_OK) return;
        showResizedImageByUri();
    }

    private void showResizedImageByUri() {
        DraweeController controller = FrescoUtil.getResizeController(
                publicChatImage.getWidth(), publicChatImage.getHeight(),
                uriImage, publicChatImage.getController());
        publicChatImage.setController(controller);
    }

    public interface OnRoomCreationListener {
        void onInputSend(Room room, Uri uriImage, String imageAbsolutePath);
    }

}
