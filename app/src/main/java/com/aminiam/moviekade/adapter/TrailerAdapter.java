package com.aminiam.moviekade.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aminiam.moviekade.R;
import com.aminiam.moviekade.utility.NetworkUtility;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder>{
    private static final String LOG_TAG = TrailerAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<String> mTrailerKeys;

    public TrailerAdapter(Context context) {
        mContext = context;
    }

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.trailer_list_item,
                parent, false);
        return new TrailerViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        if(mTrailerKeys == null) return;

        String trailerKey = mTrailerKeys.get(position);
        Picasso.with(mContext).load(NetworkUtility.buildTrailerImagePath(trailerKey))
                .into(holder.mImgTrailer);
    }

    @Override
    public int getItemCount() {
        return mTrailerKeys != null ? mTrailerKeys.size() : 0;
    }

    public void populateData(ArrayList<String> trailerKeys) {
        Log.d(LOG_TAG, "populateData");
        mTrailerKeys = trailerKeys;
        notifyDataSetChanged();
    }

    public class TrailerViewHolder extends RecyclerView.ViewHolder {

        ImageView mImgTrailer;

        public TrailerViewHolder(View itemView) {
            super(itemView);

            mImgTrailer = (ImageView) itemView.findViewById(R.id.imgTrailer);
        }
    }
}