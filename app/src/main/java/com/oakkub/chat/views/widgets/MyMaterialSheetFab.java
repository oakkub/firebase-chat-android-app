package com.oakkub.chat.views.widgets;

import android.os.Bundle;
import android.view.View;

import com.gordonwong.materialsheetfab.AnimatedFab;
import com.gordonwong.materialsheetfab.MaterialSheetFab;

/**
 * Created by OaKKuB on 2/12/2016.
 */
public class MyMaterialSheetFab<FAB extends View & AnimatedFab> extends MaterialSheetFab {

    private static final String SHOW_STATE = "state:show";

    /**
     * Creates a MaterialSheetFab instance and sets up the necessary click listeners.
     *
     * @param view       The FAB view.
     * @param sheet      The sheet view.
     * @param overlay    The overlay view.
     * @param sheetColor The background color of the material sheet.
     * @param fabColor   The background color of the FAB.
     */
    @SuppressWarnings("unchecked")
    public MyMaterialSheetFab(View view, View sheet, View overlay, int sheetColor, int fabColor) {
        super(view, sheet, overlay, sheetColor, fabColor);
    }

    public void saveInstanceState(Bundle outState) {
        outState.putBoolean(SHOW_STATE, isSheetVisible());
    }

    public void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;

        if (savedInstanceState.getBoolean(SHOW_STATE)) {
            showFab();
        }
    }
}
