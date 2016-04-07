package com.oakkub.chat.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.oakkub.chat.R;
import com.oakkub.chat.activities.BaseActivity;
import com.oakkub.chat.managers.icepick_bundler.RoomBundler;
import com.oakkub.chat.models.Room;
import com.oakkub.chat.utils.FileUtil;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.IntentUtil;
import com.oakkub.chat.utils.RoomUtil;
import com.oakkub.chat.utils.UriUtil;
import com.oakkub.chat.views.dialogs.ChooseImageDialog;
import com.oakkub.chat.views.widgets.MyDraweeView;
import com.oakkub.chat.views.widgets.MyToast;
import com.oakkub.chat.views.widgets.spinner.MySpinner;

import org.parceler.Parcels;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import icepick.State;

public class PublicRoomCreationFragment extends BaseFragment implements
        ChooseImageDialog.ChooseImageDialogListener,
        TextBitmapCreationFragment.OnTextBitmapCreationListener {

    private static final String TAG = PublicRoomCreationFragment.class.getSimpleName();
    private static final String CHOOSE_IMAGE_DIALOG_TAG = "tag:chooseImageDialog";
    private static final String TEXT_BITMAP_CREATION_TAG = "tag:textBitmapCreationFragment";
    private static final String INPUT_ROOM_STATE = "state:inputRoom";
    private static final String ARGS_ROOM = "args:room";
    private static final String ARGS_TOOLBAR_TITLE = "args:toolbarTitle";
    private static final int CAMERA_REQUEST_CODE = 0;
    private static final int IMAGE_VIEWER_REQUEST_CODE = 1;

    @Bind(R.id.simple_toolbar)
    Toolbar toolbar;

    @Bind(R.id.public_chat_image)
    MyDraweeView image;

    @Bind(R.id.public_chat_name_text_input_layout)
    TextInputLayout nameEditTextLayout;

    @Bind(R.id.public_chat_name_edittext)
    EditText nameEditText;

    @Bind(R.id.public_chat_optional_desc_text_input_layout)
    TextInputLayout descriptionTextLayout;

    @Bind(R.id.public_chat_optional_desc_edittext)
    EditText descriptionEditText;

    @Bind(R.id.public_chat_type_room_textview)
    TextView typeTextView;

    @Bind(R.id.public_chat_type_spinner)
    MySpinner tagSpinner;

    @State
    String absolutePath;

    @State
    int maxLengthName;

    @State
    int maxLengthDescription;

    @State
    Uri uriImage;

    @State
    String toolbarTitle;

    @State
    String[] spinnerKeys;

    @State
    String[] spinnerValues;

    @State
    boolean isPublicChat;

    @State
    boolean isImageUploaded;

    @State
    boolean useDefaultImage;

    @State(RoomBundler.class)
    Room inputRoom;

    @State(RoomBundler.class)
    Room argsRoom;

    private TextBitmapCreationFragment textBitmapCreationFragment;
    private OnRoomCreationListener onRoomCreationListener;

    public static PublicRoomCreationFragment newInstance(String toolbarTitle) {
        return newInstance(toolbarTitle, null);
    }

    public static PublicRoomCreationFragment newInstance(String toolbarTitle, Room room) {
        Bundle args = new Bundle();
        args.putString(ARGS_TOOLBAR_TITLE, toolbarTitle);

        if (room != null) {
            args.putParcelable(ARGS_ROOM, Parcels.wrap(room));
        }

        PublicRoomCreationFragment fragment = new PublicRoomCreationFragment();
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
        setHasOptionsMenu(true);

        textBitmapCreationFragment = (TextBitmapCreationFragment)
                getChildFragmentManager().findFragmentByTag(TEXT_BITMAP_CREATION_TAG);
        if (textBitmapCreationFragment == null) {
            textBitmapCreationFragment = new TextBitmapCreationFragment();
            getChildFragmentManager().beginTransaction()
                    .add(textBitmapCreationFragment, TEXT_BITMAP_CREATION_TAG)
                    .commit();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_public_chat_creation, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setToolbar();
        setSpinner();

        setArgsRoomData(savedInstanceState);
    }

    private void setArgsRoomData(Bundle savedInstanceState) {
        int visibility = isPublicChat ? View.VISIBLE : View.GONE;
        descriptionTextLayout.setVisibility(visibility);
        typeTextView.setVisibility(visibility);
        tagSpinner.setVisibility(visibility);

        if (argsRoom == null || savedInstanceState != null) return;
        image.setMatchedSizeImageURI(Uri.parse(argsRoom.getImagePath()));
        nameEditText.setText(argsRoom.getName());
        descriptionEditText.setText(argsRoom.getDescription());
        
        tagSpinner.setSelection(getSpinnerValuePosition(argsRoom.getTag()), true);
    }

    private int getSpinnerValuePosition(String value) {
        for (int i = 0, size = spinnerValues.length; i < size; i++) {
            if (spinnerValues[i].equals(value)) {
                tagSpinner.setSelection(i, true);
                return i;
            }
        }
        return 0;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        textBitmapCreationFragment.setOnTextBitmapCreationListener(this);
        sendResultBack();
    }

    private void sendResultBack() {
        if (onRoomCreationListener != null && inputRoom != null) {
            onRoomCreationListener.onInputSend(inputRoom, uriImage, absolutePath);
            inputRoom = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        isImageUploaded = true;
        onCameraResult(requestCode, resultCode);
        onImageViewerResult(requestCode, resultCode, data);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        textBitmapCreationFragment.setOnTextBitmapCreationListener(null);
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

        toolbarTitle = args.getString(ARGS_TOOLBAR_TITLE);
        argsRoom = Parcels.unwrap(args.getParcelable(ARGS_ROOM));
        isPublicChat = argsRoom == null || argsRoom.getType().equals(RoomUtil.PUBLIC_TYPE);
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
            actionBar.setTitle(toolbarTitle);
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

    @OnTextChanged(value = R.id.public_chat_name_edittext,
                   callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onRoomNameTyped(Editable editable) {
        if (argsRoom != null || isImageUploaded) return;

        if (textBitmapCreationFragment.isAdded()) {
            textBitmapCreationFragment.create(editable.toString().trim());
        }
    }

    @Override
    public void onTextBitmapBase64UriSend(String base64) {
        if (argsRoom != null || isImageUploaded) return;

        image.setMatchedSizeImageURI(Uri.parse(base64));
        useDefaultImage = true;
    }

    @Override
    public void onCameraClick() {
        File cameraStorage = FileUtil.getCameraStorageDirectory();
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

        getInputRoom(roomName, description);
        if (useDefaultImage) {
            inputRoom.setImagePath(image.getUriImage().toString());
        }

        sendResultBack();

        return true;
    }

    private void getInputRoom(String roomName, String description) {
        String roomType = argsRoom != null ? argsRoom.getType() : FirebaseUtil.VALUE_ROOM_TYPE_PUBLIC;
        String tagValue = spinnerValues[tagSpinner.getSelectedItemPosition()];

        inputRoom = new Room(argsRoom == null ? roomType : argsRoom.getType());
        inputRoom.setName(roomName);
        inputRoom.setTag(isPublicChat ? tagValue : null);
        
        if (!description.isEmpty()) {
            inputRoom.setDescription(description);
        }
    }
    private void onImageViewerResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != IMAGE_VIEWER_REQUEST_CODE || resultCode != Activity.RESULT_OK) return;

        useDefaultImage = false;
        uriImage = data.getData();
        absolutePath = UriUtil.getPath(uriImage);
        image.setMatchedSizeImageURI(uriImage);
    }

    private void onCameraResult(int requestCode, int resultCode) {
        if (requestCode != CAMERA_REQUEST_CODE || resultCode != Activity.RESULT_OK) return;
        useDefaultImage = false;
        image.setMatchedSizeImageURI(uriImage);
    }

    public interface OnRoomCreationListener {
        void onInputSend(Room room, Uri uriImage, String imageAbsolutePath);
    }

}
