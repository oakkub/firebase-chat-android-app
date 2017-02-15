package com.oakkub.chat.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;

import com.oakkub.chat.managers.Contextor;
import com.oakkub.chat.utils.Base64Util;
import com.oakkub.chat.utils.BitmapUtil;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created by OaKKuB on 2/11/2016.
 */
public class Base64ConverterFragment extends BaseFragment {

    private String base64Result;
    private boolean isConverting;
    private OnBase64ConverterListener base64ConverterListener;

    public static Base64ConverterFragment newInstance() {
        Bundle args = new Bundle();

        Base64ConverterFragment fragment = new Base64ConverterFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        base64ConverterListener = (OnBase64ConverterListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (base64Result != null) {
            base64ConverterListener.onBase64Received(base64Result);
            base64Result = null;
        }

    }

    public void convert(final File file, final boolean isThumbnail) {
        checkConverting();

        new Thread(new Runnable() {
            @Override
            public void run() {
                convertBitmap(BitmapUtil.getResized(file, isThumbnail));
            }
        }).start();
    }

    public void convert(final Uri imageUri, final String absolutePath, final boolean isThumbnail) {
        checkConverting();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = Contextor.getInstance().getContext();

                    ParcelFileDescriptor parcelFileDescriptor = context
                            .getContentResolver().openFileDescriptor(imageUri, "r");
                    if (parcelFileDescriptor == null) return;

                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                    convertBitmap(BitmapUtil.getResized(fileDescriptor, absolutePath, isThumbnail));
                    parcelFileDescriptor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void checkConverting() {
        if (isConverting) return;
        isConverting = true;
    }

    void convertBitmap(Bitmap bitmap) {
        String base64 = Base64Util.toDataUri(Base64Util.toBase64(bitmap, 50));
        if (base64ConverterListener != null) {
            base64ConverterListener.onBase64Received(base64);
        } else {
            base64Result = base64;
        }

        isConverting = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        base64ConverterListener = null;
    }

    public interface OnBase64ConverterListener {
        void onBase64Received(String base64);
    }
}
