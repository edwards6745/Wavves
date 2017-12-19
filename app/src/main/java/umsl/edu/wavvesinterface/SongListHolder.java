package umsl.edu.wavvesinterface;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import umsl.edu.wavvesinterface.database.Audio;
import umsl.edu.wavvesinterface.database.Song;

/**
 * Created by Austin on 5/9/2017.
 */

public class SongListHolder extends RecyclerView.ViewHolder {

    private TextView mSongName;
    private ImageButton mPlayButton;
    private int mPosition;

    public SongListHolder(View itemView) {
        super(itemView);
        mSongName = (TextView) itemView.findViewById(R.id.song_text_view);
        mPlayButton = (ImageButton) itemView.findViewById(R.id.play_button);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               //playAudio();
           }
        });
    }

    public void bindSongName(Song s) {
        mSongName.setText(s.getTitle());
    }

    public void setmPosition(int mPosition) {
        this.mPosition = mPosition;
    }
}
