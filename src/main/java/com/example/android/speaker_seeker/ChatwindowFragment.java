package com.example.android.speaker_seeker;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.speaker_seeker.dummy.DummyContent;
import com.example.android.speaker_seeker.dummy.DummyContent.DummyItem;
import com.example.android.speaker_seeker.models.User;

import java.util.ArrayList;
import java.util.List;

import static com.firebase.ui.auth.AuthUI.TAG;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ChatwindowFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private OnListFragmentInteractionListener mListener;

    private View view;
    private ArrayList<String> name;
    private RecyclerView mRecyclerView;
    private ArrayList<User> speakers = new ArrayList<User>();
    private MyChatwindowRecyclerViewAdapter mCollectRecyclerAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChatwindowFragment() {
    }

    public static ChatwindowFragment newInstance(String info) {
        Bundle args = new Bundle();
        ChatwindowFragment fragment = new ChatwindowFragment();
        args.putString("info", info);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       view = inflater.inflate(R.layout.fragment_chatwindow_list, container, false);

        initRecyclerView();
        name = getArguments().getStringArrayList("speaker");
        initData();

        return view;
    }



    private void initData() {
        if(name != null){
            for(String s: name){
                User speaker = new User();
                speaker.setName(s);
                speakers.add(speaker);
            }
        }
        else {
            User speaker = new User();
            speaker.setName("No Chats Yet");
            speakers.add(speaker);

        }

    }

    private void initRecyclerView() {
        mRecyclerView=(RecyclerView)view.findViewById(R.id.recycler_list);
        mCollectRecyclerAdapter = new MyChatwindowRecyclerViewAdapter(getActivity(), speakers);
        mRecyclerView.setAdapter(mCollectRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mCollectRecyclerAdapter.setOnItemClickListener(new MyChatwindowRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, User user) {
                ChatFragment chat = new ChatFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.speaker, chat);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }




    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(DummyItem item);
    }
}
