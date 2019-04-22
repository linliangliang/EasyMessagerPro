package com.zhengyuan.easymessengerpro.activity.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhengyuan.easymessengerpro.R;

/**
 * @author 林亮
 * @description:
 * @date :2019/1/31 15:09
 */

public class RightSubOther extends Fragment {
    /**
     * Fregmant根布局
     */
    public static View root;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_other, null);

        return root;
    }
}
