package com.example.android.speaker_seeker;

import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;

import android.annotation.SuppressLint;
import java.lang.reflect.Field;
import android.support.design.bottomnavigation.LabelVisibilityMode;

// 利用反射，改变 item 中 mShiftingMode 的值
public class BottomNavigationViewHelper {

    @SuppressLint("RestrictedApi")
    public static void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        menuView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        menuView.buildMenuView();
    }
}
