package com.example.android.speaker_seeker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.android.speaker_seeker.models.User;
import java.util.ArrayList;



public class MyChatwindowRecyclerViewAdapter extends RecyclerView.Adapter<MyChatwindowRecyclerViewAdapter.myViewHolder> {

    private Context context;
    private ArrayList<User> speakers;


    public MyChatwindowRecyclerViewAdapter(Context context, ArrayList<User> speakers) {

        this.context = context;
        this.speakers = speakers;
    }

    @Override
    public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.fragment_chatwindow, null);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final myViewHolder holder, int position) {

        User speaker = speakers.get(position);
        holder.mUsername.setText(speaker.name);
    }

    @Override
    public int getItemCount() {
        return speakers.size();
    }


    //define the viewholder

    public class myViewHolder extends RecyclerView.ViewHolder {
        private TextView mUsername;


        public myViewHolder(View view) {
            super(view);
            mUsername = (TextView) view.findViewById(R.id.id_name);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (onItemClickListener != null) {
                        onItemClickListener.OnItemClick(v, speakers.get(getLayoutPosition()));
                    }
                }
            });
        }

    }


    public interface OnItemClickListener {

        public void OnItemClick(View view, User user);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
