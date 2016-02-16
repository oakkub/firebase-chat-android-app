package com.oakkub.chat.views.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oakkub.chat.R;
import com.oakkub.chat.managers.BundleSavedState;

/**
 * Created by OaKKuB on 1/18/2016.
 */
public class TextImageView extends FrameLayout {

    private LinearLayout root;
    private ImageView image;
    private TextView text;

    public TextImageView(Context context) {
        super(context);
        initInflate();
        initInstance();
    }

    public TextImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initInflate();
        initInstance();
        initAttrs(attrs, 0, 0);
    }

    public TextImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initInflate();
        initInstance();
        initAttrs(attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public TextImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initInflate();
        initInstance();
        initAttrs(attrs, defStyleAttr, defStyleRes);
    }

    private void initInflate() {
        inflate(getContext(), R.layout.text_image, this);
    }

    private void initInstance() {
        root = (LinearLayout) findViewById(R.id.text_image_root);
        image = (ImageView) findViewById(R.id.text_image_imageview);
        text = (TextView) findViewById(R.id.text_image_textview);
    }

    private void initAttrs(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.TextImageView, defStyleAttr, defStyleRes);

        try {

            String textName = typedArray.getString(R.styleable.TextImageView_textName);
            Drawable drawable = typedArray.getDrawable(R.styleable.TextImageView_image);

            if (drawable != null) {
                image.setImageDrawable(drawable);
            }

            if (textName != null) {
                text.setText(textName);
            } else {
                text.setVisibility(View.GONE);
            }

        } finally {
            typedArray.recycle();
        }
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        root.setOnClickListener(onClickListener);
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        Bundle childrenStates = new Bundle();
        for (int i = 0, size = getChildCount(); i < size; i++) {
            View child = getChildAt(i);
            int id = child.getId();

            if (id != 0) {
                SparseArray<Parcelable> childrenState = new SparseArray<>();
                child.saveHierarchyState(childrenState);
                childrenStates.putSparseParcelableArray(String.valueOf(id), childrenState);
            }
        }

        Bundle bundle = new Bundle();
        bundle.putBundle("childrenStates", childrenStates);

        BundleSavedState savedState = new BundleSavedState(superState);
        savedState.bundle = bundle;

        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        BundleSavedState savedState = (BundleSavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        Bundle childrenStates = savedState.bundle.getBundle("childrenStates");
        if (childrenStates == null) return;

        for (int i = 0, size = getChildCount(); i < size; i++) {
            View child = getChildAt(i);
            int id = child.getId();

            if (id != 0) {
                if (childrenStates.containsKey(String.valueOf(id))) {
                    SparseArray<Parcelable> childrenState = childrenStates.getSparseParcelableArray(String.valueOf(id));
                    child.restoreHierarchyState(childrenState);
                }
            }
        }
    }

    public void setText(String text) {
        this.text.setText(text);
    }

    public void setImage(Drawable drawable) {
        image.setImageDrawable(drawable);
    }

}
