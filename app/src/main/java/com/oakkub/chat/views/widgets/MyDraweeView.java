package com.oakkub.chat.views.widgets;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.oakkub.chat.managers.BundleSavedState;
import com.oakkub.chat.utils.FrescoUtil;

/**
 * Created by OaKKuB on 2/16/2016.
 */
@SuppressWarnings("SpellCheckingInspection")
public class MyDraweeView extends SimpleDraweeView {

    private static final String URI_STATE = "state:uriImage";

    Uri uriImage;

    public MyDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
    }

    public MyDraweeView(Context context) {
        super(context);
    }

    public MyDraweeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyDraweeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MyDraweeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setImageURI(Uri uri) {
        this.uriImage = uri;
        super.setImageURI(uri);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        BundleSavedState savedState = new BundleSavedState(superState);

        Bundle args = new Bundle();
        args.putParcelable(URI_STATE, uriImage);

        savedState.bundle = args;

        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        BundleSavedState savedState = (BundleSavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        Uri uri = savedState.bundle.getParcelable(URI_STATE);
        if (uri != null) {
            setMatchedSizeImageURI(uri);
        }
    }

    public void setMatchedSizeImageURI(Uri uri) {
        this.uriImage = uri;

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                DraweeController controller = FrescoUtil.getResizeController(
                        getMeasuredWidth(), getMeasuredHeight(),
                        uriImage, getController());
                setController(controller);
                getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
    }

    public Uri getUriImage() {
        return uriImage;
    }
}
