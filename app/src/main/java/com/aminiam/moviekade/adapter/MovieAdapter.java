package com.aminiam.moviekade.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aminiam.moviekade.R;
import com.aminiam.moviekade.other.MovieStructure;
import com.aminiam.moviekade.utility.NetworkUtility;
import com.squareup.picasso.Picasso;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder>{
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    private MovieStructure[] mMovieStructures;
    private Context mContext;

    public MovieAdapter(Context context, MovieStructure[] movieStructures) {
        mMovieStructures = movieStructures;
        mContext = context;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_list_item, parent, false);
        return new MovieViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        String posterName = mMovieStructures[position].posterName;
        String title = mMovieStructures[position].title;
        double averageVote = mMovieStructures[position].averageVote;
        Picasso.with(mContext).load(NetworkUtility.buildPosterPath(posterName)).into(holder.imgPosterPath);
        holder.txtTitle.setText(title);
        holder.txtAverageVote.setText(String.format(mContext.getString(R.string.average_vote), averageVote));
    }

    @Override
    public int getItemCount() {
        return mMovieStructures != null ? mMovieStructures.length : 0;
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPosterPath;
        TextView txtTitle;
        TextView txtAverageVote;

        MovieViewHolder(View itemView) {
            super(itemView);
            imgPosterPath = (ImageView) itemView.findViewById(R.id.imgPosterPath);
            txtTitle = (TextView) itemView.findViewById(R.id.txtTitle);
            txtAverageVote = (TextView) itemView.findViewById(R.id.txtAverageVote);
        }
    }
}
