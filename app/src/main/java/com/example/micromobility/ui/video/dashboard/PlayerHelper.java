package com.example.micromobility.ui.video.dashboard;


import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.micromobility.R;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ClippingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.IOException;

import androidx.fragment.app.Fragment;


public class PlayerHelper implements Player.EventListener{

    private View mcontainer;

    PlayerView mPlayerView;
    private SimpleExoPlayer mPlayer;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";

    private String relative_path;
    private String title;
    private String type;
    private File mediafile;
    private Boolean playWhenReady = Boolean.FALSE;
    private Float speed_value = Float.valueOf(1);

    private ImageButton play, pause, restart, more_speed, less_speed;
    private TextView speed;
    private EndOption callback;


    public PlayerHelper(String relative_path, String title, String type, EndOption endOption) {
        this.relative_path=relative_path;
        this.title=title;
        this.type=type;
        this.callback=endOption;

    }

    public void initializePlayer(View container) {
        mcontainer = container;

        mPlayerView = mcontainer.findViewById(R.id.player_view);

        mediafile = new File(mcontainer.getContext().getExternalFilesDir(relative_path), title);
        // Instantiate the player.
        mPlayer = new SimpleExoPlayer.Builder(mcontainer.getContext()).build();
        // Attach player to the view.
        mPlayerView.setPlayer(mPlayer);
        // Prepare the player with the media source.
        mPlayer.prepare(createMediaSource());
        mPlayer.setPlayWhenReady(playWhenReady);
        mPlayer.addListener(this);
        mPlayer.setVolume(0);
    }

    public void setCallback(EndOption callback){
        this.callback=callback;
    }

    public void setControls(){
        play = mPlayerView.findViewById(R.id.exo_play);
        play.setOnClickListener(v -> {
                playWhenReady=true;
                mPlayer.setPlayWhenReady(playWhenReady);
                play.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
        });
        pause = mPlayerView.findViewById(R.id.exo_pause);
        pause.setOnClickListener(v -> {
            playWhenReady=false;
            mPlayer.setPlayWhenReady(playWhenReady);
            pause.setVisibility(View.GONE);
            play.setVisibility(View.VISIBLE);
        });
        restart = mPlayerView.findViewById(R.id.restart);
        restart.setOnClickListener(v -> {
            restart();
        });
        speed = mPlayerView.findViewById(R.id.speed_counter);
        more_speed = mPlayerView.findViewById(R.id.more_speed);
        more_speed.setOnClickListener(v -> {

            speed_value = speed_value + 0.5F;
            speed.setText(String.format("%.1f", speed_value));
            changeSpeed(speed_value);

        });
        less_speed = mPlayerView.findViewById(R.id.less_speed);
        less_speed.setOnClickListener(v -> {
            if (speed_value>1) {
                speed_value = speed_value - 0.5F;
                speed.setText(String.format("%.1f", speed_value));
            }
            else{
                speed_value = speed_value/2;
                speed.setText(String.format("%.2f", speed_value));
            }
            changeSpeed(speed_value);
        });

    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        if (isPlaying) {
            // Active playback.
        } else {
            if(mPlayer.getPlaybackState()==Player.STATE_ENDED){
                System.out.println("Display has ended");
                callback.onEndOption(this.type);

            }
        }
    }


    @Override
    public void onPlayerError(ExoPlaybackException error) {
        if (error.type == ExoPlaybackException.TYPE_SOURCE) {
            IOException cause = error.getSourceException();
            Toast.makeText(mcontainer.getContext(), "Error reviewing video", Toast.LENGTH_SHORT).show();
            cause.printStackTrace();
        }
    }

    private MediaSource createMediaSource(){
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mcontainer.getContext(),
                Util.getUserAgent(mcontainer.getContext(), "MicroMobility"));

        // This is the MediaSource representing the media to be played.
        Uri uri = Uri.fromFile(mediafile);
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        // Clip to start at 5 seconds and end at 10 seconds.
        ClippingMediaSource clippingSource =new ClippingMediaSource(videoSource,/* startPositionUs= */ 0_000_000,/* endPositionUs= */ C.TIME_END_OF_SOURCE);
        return videoSource;
    }

    public Long getTimePosition(){
        return mPlayer.getCurrentPosition();
    }

    public void finish(){
        mPlayer.release();
    }

    public void restart(){
        mPlayer.seekTo(0);
        this.playWhenReady=false;
        mPlayer.setPlayWhenReady(this.playWhenReady);
        pause.setVisibility(View.GONE);
        play.setVisibility(View.VISIBLE);
    }

    // @param speed The factor by which playback will be sped up. Must be greater than zero.
    public void changeSpeed(Float speed){
        mPlayer.setPlaybackParameters(new PlaybackParameters(speed));
    }


    public interface EndOption{
        void onEndOption(String type);
    }
}