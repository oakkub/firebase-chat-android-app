package com.oakkub.chat.views.listener;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.oakkub.chat.views.widgets.ToolbarCommunicator;

/**
 * Created by OaKKuB on 10/15/2015.
 */
public class ScrollingToolbarRecyclerViewListener extends RecyclerView.OnScrollListener {

    private ToolbarCommunicator toolbarCommunicator;

    @Override
    public void onScrolled(RecyclerView recyclerView, int deltaX, int deltaY) {
        super.onScrolled(recyclerView, deltaX, deltaY);

       /* final int toolbarHeight = toolbarCommunicator.getHeight();

        Log.e("SCROLLED DX", String.valueOf(deltaX));
        Log.e("SCROLLED DY", String.valueOf(deltaY));

        if (deltaY > 0) {
            toolbarCommunicator.setTranslationY(
                    Math.max(-toolbarHeight,
                            toolbarCommunicator.getTranslationY() - deltaY / 2));
        } else {
            toolbarCommunicator.setTranslationY(
                    Math.min(0, toolbarCommunicator.getTranslationY() - deltaY / 2));
        }*/
    }
}
