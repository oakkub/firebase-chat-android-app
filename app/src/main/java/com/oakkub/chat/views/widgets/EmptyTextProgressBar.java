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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oakkub.chat.R;
import com.oakkub.chat.managers.BundleSavedState;

/**
 * Created by OaKKuB on 1/18/2016.
 */
public class EmptyTextProgressBar extends FrameLayout {

    private FrameLayout root;
    private ProgressBar progressBar;
    private TextView text;

    public EmptyTextProgressBar(Context context) {
        super(context);
        initInflate();
        initInstance();
    }

    public EmptyTextProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initInflate();
        initInstance();
        initAttrs(attrs, 0, 0);
    }

    public EmptyTextProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initInflate();
        initInstance();
        initAttrs(attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public EmptyTextProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initInflate();
        initInstance();
        initAttrs(attrs, defStyleAttr, defStyleRes);
    }

    private void initInflate() {
        inflate(getContext(), R.layout.empty_text_progress_bar, this);
    }

    private void initInstance() {
        root = (FrameLayout) findViewById(R.id.emptyTextProgressBarContainer);
        progressBar = (ProgressBar) findViewById(R.id.emptyTextProgressBar);
        text = (TextView) findViewById(R.id.emptyTextProgressbarText);

        text.setVisibility(View.GONE);
    }

    private void initAttrs(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.EmptyTextProgressBar, defStyleAttr, defStyleRes);

        try {

            String textName = typedArray.getString(R.styleable.EmptyTextProgressBar_text);
            Drawable drawable = typedArray.getDrawable(R.styleable.EmptyTextProgressBar_progressBarDrawable);

            if (drawable != null) {
                progressBar.setIndeterminateDrawable(drawable);
            }

            if (textName != null) {
                text.setText(textName);
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
        childrenStates.putInt("visibility", getVisibility());

        Bundle bundle = new Bundle();
        bundle.putBundle("childrenStates", childrenStates);

        BundleSavedState savedState = new BundleSavedState(superState);
        savedState.bundle = bundle;

        return savedState;
    }

    @SuppressWarnings({"WrongConstant", "ConstantConditions"})
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

        setVisibility(childrenStates.getInt("visibility"));
    }

    public void showProgressBar() {
        setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        text.setVisibility(View.GONE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    public void showEmptyText() {
        setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        text.setVisibility(View.VISIBLE);
    }

    public void setErrorText(String text) {
        this.text.setText(text);
    }

    public void setProgressBarDrawable(Drawable drawable) {
        progressBar.setIndeterminateDrawable(drawable);
    }

}
