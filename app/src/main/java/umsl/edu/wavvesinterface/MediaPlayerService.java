package umsl.edu.wavvesinterface;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import umsl.edu.wavvesinterface.database.Audio;

/**
 * Created by Austin on 4/27/2017.
 */



public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
                MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
                MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener {

    public static final String ACTION_PLAY = "edu.umsl.wavves.audioplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "edu.umsl.wavves.audioplayer.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "edu.umsl.wavves.audioplayer.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "edu.umsl.wavves.audioplayer.ACTION_NEXT";
    public static final String ACTION_STOP = "edu.umsl.wavves.audioplayer.ACTION_STOP";

    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    private static final int NOTIFICATION_ID = 101;

    private final IBinder iBinder = new LocalBinder();
    private MediaPlayer mediaPlayer;
    private String mediaFile;
    private int resumePosition;
    private AudioManager audioManager;
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private ArrayList<Audio> audioList;
    private int audioIndex = -1;

    @Override
    public void onCreate() {
        super.onCreate();

        callStateListener();
        registerBecomingNoisyReceiver();
        registerPlayNewAudio();
    }

    private Audio activeAudio;

    public BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            audioIndex = new StorageUtility(getApplicationContext()).loadAudioIndex();
            if(audioIndex != -1 && audioIndex < audioList.size()) {
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }

            stopMedia();
            mediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackState.STATE_PLAYING);
        }
    };

    private void callStateListener() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch(state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if(mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if(mediaPlayer != null) {
                            if(ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMedia();
            buildNotification(PlaybackState.ACTION_PAUSE);
        }


    };

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            //Set the data source to the mediaFile location
            mediaPlayer.setDataSource(mediaFile);
        } catch(IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.prepareAsync();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        switch(focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if(mediaPlayer == null) {
                    initMediaPlayer();
                }else if(!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if(mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if(mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if(mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true;
        }
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //Invoked when playback of a media source has completed.
        stopMedia();
        //stop the service
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();

        if(phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();

        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);

        new StorageUtility(getApplicationContext()).clearCachedAudioPlaylist();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            StorageUtility storage = new StorageUtility(getApplicationContext());
            audioList = storage.loadAudio();
            audioIndex = storage.loadAudioIndex();

            if (audioIndex != -1 && audioIndex < audioList.size()) {
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            stopSelf();
        }

        if (requestAudioFocus() == false) {
            stopSelf();
        }

        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                initMediaPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }

        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch(what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //used when the media source is ready for playback
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    private void playMedia() {
        if(!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if(mediaPlayer == null) {
            return;
        }
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private void pauseMedia() {
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if(!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    private void buildNotification(long actionPause) {

    }

    private void registerBecomingNoisyReceiver() {
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    private void registerPlayNewAudio() {
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }

    private void initMediaSession() throws RemoteException {
        if(mediaSessionManager != null) {
            return;
        }

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        transportControls = mediaSession.getController().getTransportControls();
        mediaSession.setActive(true);

        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        updateMetaData();

        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                buildNotification(PlaybackState.STATE_PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                buildNotification(PlaybackState.STATE_PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
                updateMetaData();
                buildNotification(PlaybackState.STATE_PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                updateMetaData();
                buildNotification(PlaybackState.STATE_PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                stopSelf();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
            }
        });
    }

    private void updateMetaData() {
        Bitmap albumArt = BitmapFactory.decodeResource(getResources(), R.drawable.image);

        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio.getAudioSongs().get(resumePosition).getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio.getAudioSongs().get(resumePosition).getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio.getAudioSongs().get(resumePosition).getTitle())
                .build());
    }

    private void skipToNext() {
        if(audioIndex == audioList.size() - 1) {
            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
        } else {
            activeAudio = audioList.get(++audioIndex);
        }

        new StorageUtility(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        mediaPlayer.reset();
        initMediaPlayer();
    }

    private void skipToPrevious() {
        if(audioIndex == 0) {
            audioIndex = audioList.size();
            activeAudio = audioList.get(audioIndex);
        } else {
            activeAudio = audioList.get(--audioIndex);
        }

        new StorageUtility(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        mediaPlayer.reset();
        initMediaPlayer();
    }

    private void buildNotification(PlaybackStatus playbackStatus) {

        int notificationAction = android.R.drawable.ic_media_pause;
        PendingIntent play_pauseAction = null;

        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.image);

        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setShowWhen(false)
                .setStyle(new NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                .setContentText(activeAudio.getAudioSongs().get(resumePosition).getArtist())
                .setContentTitle(activeAudio.getAudioSongs().get(resumePosition).getAlbum())
                .setContentInfo(activeAudio.getAudioSongs().get(resumePosition).getTitle())
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaPlayerService.class);
        switch (actionNumber) {
            case 0:
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }


}
