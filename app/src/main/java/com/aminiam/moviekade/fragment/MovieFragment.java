package com.aminiam.moviekade.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aminiam.moviekade.R;
import com.aminiam.moviekade.activity.MainActivity;
import com.aminiam.moviekade.adapter.MovieAdapter;
import com.aminiam.moviekade.databinding.FragmentMovieBinding;
import com.aminiam.moviekade.other.Callback;
import com.aminiam.moviekade.other.GridSpacingItemDecoration;
import com.aminiam.moviekade.other.MovieStructure;
import com.aminiam.moviekade.other.UiUpdaterListener;
import com.aminiam.moviekade.utility.JsonUtility;
import com.aminiam.moviekade.utility.NetworkUtility;
import com.aminiam.moviekade.utility.Utility;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

public class MovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<String>,
        MovieAdapter.MovieClickListener, SharedPreferences.OnSharedPreferenceChangeListener{
    private static final String LOG_TAG = MovieFragment.class.getSimpleName();

    private static final String RECYCLER_POSTION_KEY = "recycler_position_key";

    private MovieAdapter mAdapter;
    private String mPath;
    private int mLoaderId;
    private Toast mToast;
    private boolean mShowInfo;
    private Parcelable mListState;

    private FragmentMovieBinding mBinding;
    private UiUpdaterListener mListener;
    private GridLayoutManager mGridLayoutManager;

    public MovieFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (UiUpdaterListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().getName() + " must implements UiUpdaterListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mPath = args.getString(MainActivity.PATH_KEY);
        mLoaderId = args.getInt(MainActivity.LOADER_ID_KEY);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentMovieBinding.inflate(inflater, container, false);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mShowInfo = preferences.getBoolean(getString(R.string.pref_info_poster_data_key), true);
        preferences.registerOnSharedPreferenceChangeListener(this);

        mAdapter = new MovieAdapter(getActivity(),mShowInfo, this);
        int spanCount = Utility.calculateNoOfColumns(getActivity());

        mGridLayoutManager = new GridLayoutManager(getContext(), spanCount);
        mBinding.recPlayingMovies.setLayoutManager(mGridLayoutManager);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_layout_margin);
        mBinding.recPlayingMovies.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacingInPixels, true, 0));

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(RECYCLER_POSTION_KEY);
        }

        if(NetworkUtility.isNetworkAvailable(getContext())) {
            getActivity().getSupportLoaderManager().initLoader(mLoaderId, null, this);
        } else {
            mListener.error(getString(R.string.error_message_internet));
        }
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<String>(getActivity()) {
            @Override
            protected void onStartLoading() {
                mListener.updateViews(true);
                forceLoad();
            }

            @Override
            public String loadInBackground() {
                try {
                    final URL url = NetworkUtility.getMoviesUrl(mPath);
                    return NetworkUtility.getResponseFromHttpUrl(url);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if (data == null) {
            String errorMessage = getResources().getString(R.string.error_message_failed);
            if(!NetworkUtility.isNetworkAvailable(getActivity())) {
                getString(R.string.error_message_internet);
            }
            mListener.error(errorMessage);
            showToast(errorMessage);
        } else {
            try {
                mListener.updateViews(false);
                MovieStructure[] movieStructures = JsonUtility.getMoviesDataFromJson(data);
                mAdapter.populateDate(movieStructures);
                mBinding.recPlayingMovies.setAdapter(mAdapter);
                if(mListState != null) {
                    mGridLayoutManager.onRestoreInstanceState(mListState);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }

    @Override
    public void onMovieClick(long movieId,String movieTitle, String posterPath, String backdropPath) {
        ((Callback) getActivity()).onItemSelected(movieId,movieTitle, posterPath, backdropPath);
    }

    private void showToast(String message) {
        if(mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    // TODO: Probably Delete
    public void initLoader() {
        getActivity().getSupportLoaderManager().initLoader(mLoaderId, null, MovieFragment.this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_info_poster_data_key))) {
            mShowInfo = sharedPreferences.getBoolean(key, true);
            mAdapter = new MovieAdapter(getActivity(),mShowInfo, this);
            if(NetworkUtility.isNetworkAvailable(getContext())) {
                getActivity().getSupportLoaderManager().initLoader(mLoaderId, null, this);
            } else {
                mListener.error(getString(R.string.error_message_internet));
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        mListState =  mBinding.recPlayingMovies.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(RECYCLER_POSTION_KEY, mListState);
        super.onSaveInstanceState(outState);
    }
}
