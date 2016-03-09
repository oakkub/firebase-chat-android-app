package com.oakkub.chat.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;

import com.oakkub.chat.R;
import com.oakkub.chat.utils.Base64Util;

import icepick.State;

/**
 * Created by OaKKuB on 2/21/2016.
 */
public class TextBitmapCreationFragment extends BaseFragment implements
        LoaderManager.LoaderCallbacks<String> {

    private static final String TAG = TextBitmapCreationFragment.class.getSimpleName();
    private static final int WIDTH = 200;
    private static final int HEIGHT = 200;
    private static final int TEXT_BITMAP_LOADER_ID = 0;

    private String base64Result;
    private OnTextBitmapCreationListener onTextBitmapCreationListener;

    @State
    String previousTextImage;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (base64Result != null) {
            onTextBitmapCreationListener.onTextBitmapBase64UriSend(base64Result);
            base64Result = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        onTextBitmapCreationListener = null;
    }

    public void setOnTextBitmapCreationListener(OnTextBitmapCreationListener onTextBitmapCreationListener) {
        this.onTextBitmapCreationListener = onTextBitmapCreationListener;
    }

    public void create(String textImage) {
        if (textImage.isEmpty()) return;
        textImage = String.valueOf(textImage.charAt(0));

        if (previousTextImage == null || !previousTextImage.equals(textImage)) {
            Bundle args = new Bundle();
            args.putString(TextImageLoader.ARGS_TEXT, textImage);

            getLoaderManager().restartLoader(TEXT_BITMAP_LOADER_ID, args, this);
        }
        previousTextImage = textImage;
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new TextImageLoader(getActivity(), args.getString(TextImageLoader.ARGS_TEXT));
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if (onTextBitmapCreationListener != null) {
            onTextBitmapCreationListener.onTextBitmapBase64UriSend(data);
        } else {
            base64Result = data;
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    public static class TextImageLoader extends AsyncTaskLoader<String> {

        private static final String ARGS_TEXT = "args:text";

        private String text;
        private Paint paint;

        public TextImageLoader(Context context, String text) {
            super(context);
            this.text = text;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public String loadInBackground() {
            Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.RGB_565);

            initPaint();
            Canvas canvas = new Canvas(bitmap);
            drawText(text, canvas, paint);

            String base64 = Base64Util.toDataUri(Base64Util.toBase64(bitmap, 100));
            bitmap.recycle();

            return base64;
        }

        @Override
        public void deliverResult(String data) {
            if (isStarted()) {
                super.deliverResult(data);
            }
        }

        private void initPaint() {
            if (paint != null) return;

            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(ContextCompat.getColor(getContext(), android.R.color.white));
            paint.setTextSize(WIDTH * 0.4f);
        }

        private void drawText(String character, Canvas canvas, Paint paint) {
            Rect rect = getRectTextBound(character, paint);

            float xPos = (canvas.getClipBounds().width() / 2f) - (rect.width() / 2f) - rect.left;
            float yPos = (canvas.getClipBounds().height() / 2f) + (rect.height() / 2f) - rect.bottom;

            canvas.drawColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            canvas.drawText(character, xPos, yPos, paint);
        }

        private Rect getRectTextBound(String character, Paint paint) {
            Rect rect = new Rect();
            paint.getTextBounds(character, 0, character.length(), rect);
            return rect;
        }
    }

    public interface OnTextBitmapCreationListener {
        void onTextBitmapBase64UriSend(String base64);
    }

}
