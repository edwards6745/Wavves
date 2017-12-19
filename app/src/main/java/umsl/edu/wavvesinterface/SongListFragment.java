package umsl.edu.wavvesinterface;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import umsl.edu.wavvesinterface.database.Audio;
import umsl.edu.wavvesinterface.database.Song;

/**
 * Created by Austin on 5/9/2017.
 */

public class SongListFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private ArrayList<Song> mSongList;
    private Context mContext;

    public SongListFragment() {

    }

    int color;
    SimpleRecyclerAdapter adapter;

    private WeakReference<SongListingViewDataSource> mDataSource;

    public void setDataSource(SongListingViewDataSource dataSource) {
        mDataSource = new WeakReference<>(dataSource);
    }

    public interface SongListingViewDataSource {
        ArrayList<Song> getAudio();
    }

    public void setSongModel(ArrayList<Song> songList) {
        this.mSongList = songList;
    }

    public void setmContext(Context context) {
        this.mContext = context;
    }

    @SuppressLint("ValidFragment")
    public SongListFragment(int color) {
        this.color = color;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.song_list_frag, container, false);

        final FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.dummyfrag_bg);
        frameLayout.setBackgroundColor(color);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.song_list_recycler_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(new SimpleRecyclerAdapter(mSongList, mContext));

        return view;
    }

    public class SimpleRecyclerAdapter extends RecyclerView.Adapter<SongListHolder> {
        ArrayList<Song> mSongs;
        Context context;


        public SimpleRecyclerAdapter(ArrayList<Song> mSongs, Context context) {
            this.mSongs = mSongs;
            this.context = context;
        }

        @Override
        public SongListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.item_layout, parent, false);
            return new SongListHolder(view);
        }

        @Override
        public void onBindViewHolder(SongListHolder versionViewHolder, int i) {

                versionViewHolder.bindSongName(mSongs.get(i));
                Log.e("Song Name", "" + mSongs.get(i).getTitle());
                versionViewHolder.setmPosition(i);
                //versionViewHolder.subTitle.setText(homeActivitiesSubList.get(i));
        }

        @Override
        public int getItemCount() {
            if(mSongs != null) {
                return mSongs.size();
            }
            Log.e("NO SONGS", "There are no songs on this device");
            return 0;
        }


        /*public interface OnItemClickListener {
            public void onItemClick(View view, int position);
        }*/

    }
}
