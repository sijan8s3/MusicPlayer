package com.devs.musicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongsViewHolder> {

    String[] songNames;
    Context mContext;

    OnSongClickListener onSongClickListener;

    public SongsAdapter(String[] songNames, Context context) {
        this.songNames = songNames;
        mContext = context;
    }

    public void setOnSongClickListener(OnSongClickListener onSongClickListener) {
        this.onSongClickListener = onSongClickListener;
    }

    @NonNull
    @Override
    public SongsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item, parent, false);
        return new SongsViewHolder(view, onSongClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsViewHolder holder, int position) {
        holder.song_tv.setText(songNames[position]);
    }

    @Override
    public int getItemCount() {
        return songNames.length;
    }

    public interface OnSongClickListener {
        void onSongClick(int position);
    }

    public static class SongsViewHolder extends RecyclerView.ViewHolder {
        TextView song_tv;

        public SongsViewHolder(@NonNull View itemView, final OnSongClickListener onSongClickListener) {
            super(itemView);
            song_tv = itemView.findViewById(R.id.song_tv);

            song_tv.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (onSongClickListener != null) {
                                int position = getAdapterPosition();
                                if (position != RecyclerView.NO_POSITION) {
                                    onSongClickListener.onSongClick(position);
                                }
                            }
                        }
                    });
        }
    }

}
