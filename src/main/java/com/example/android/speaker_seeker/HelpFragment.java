package com.example.android.speaker_seeker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;

/**
 * Created by bruce on 2016/11/1.
 * BaseFragment
 */

public class HelpFragment extends Fragment {
    //text info of bottom navigation
    public static HelpFragment newInstance(String info) {
        Bundle args = new Bundle();
        //info = info + "hah";
        HelpFragment fragment = new HelpFragment();
        args.putString("info", info);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Context context = getActivity();
        //inflate可用于将一个xml中定义的布局控件找出来
        View view = inflater.inflate(R.layout.fragment_help,container,false);
        TextView tvInfo = (TextView) view.findViewById(R.id.textView);
        tvInfo.setText(getArguments().getString("info"));
        tvInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Don't click me.please!.", Snackbar.LENGTH_SHORT).show();
            }
        });
        return view;
    }
}