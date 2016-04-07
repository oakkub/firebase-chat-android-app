package com.oakkub.chat.managers.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by OaKKuB on 2/26/2016.
 */
public abstract class MyAsyncLoader<D> extends AsyncTaskLoader<D> {

    protected D data;

    public MyAsyncLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (data != null) {
            deliverResult(data);
        }

        if (takeContentChanged() || data == null) {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(D data) {
        if (isReset()) {
            // An query came in while the loader is stopped
            return;
        }

        this.data = data;
        super.deliverResult(data);
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();

        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        stopLoading();

        data = null;
    }
}
