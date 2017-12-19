package umsl.edu.wavvesinterface;

import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import umsl.edu.wavvesinterface.database.Song;

/**
 * Created by Austin on 5/11/2017.
 */

public class SongAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    private ArrayList<Song> mSongList;
    private Context mContext;
    private FragmentManager mFMan;

    @Override
    public Fragment getItem(int position) {
            SongListFragment sFrag = new SongListFragment();
            sFrag.setSongModel(mSongList);
            sFrag.setmContext(mContext);
        Log.e("Adapter Songs", "" + mSongList.size());
            return sFrag;

    }

    public SongAdapter(FragmentManager fm) {
        super(fm);
    }

    public SongAdapter(ArrayList<Song> mSongList, Context mContext, FragmentManager fm) {
        super(fm);
        this.mSongList = mSongList;
        this.mContext = mContext;
        this.mFMan = fm;
    }

    public void addFrag(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public Parcelable saveState() {
        return super.saveState();
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        super.restoreState(state, loader);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
}
