package com.hawk.test.player;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hawk.aop.LogMethod;
import com.hawk.aop.Trace;
import com.hawk.test.R;
import com.hawk.test.util.LOG;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener,MediaPlayer.OnPreparedListener,
        MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnVideoSizeChangedListener,
        SurfaceHolder.Callback {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Display currDisplay;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private MediaPlayer player;
    private ImageView playBtn;

    private String url;
    private int vWidth, vHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        url = "http://Blackhawk:35e169d7359aa238489884fda7d21070@ss.99guard.com:80/media/video/2016/06/07/08_00_28_n.mp4";
        initView();
    }

    @LogMethod
    @Trace
    private void initView() {
        surfaceView = (SurfaceView)this.findViewById(R.id.surfaceView);
        playBtn = (ImageView) findViewById(R.id.playbtn);
        playBtn.setOnClickListener(this);
        //给SurfaceView添加CallBack监听
        holder = surfaceView.getHolder();
        holder.addCallback(this);
        //为了可以播放视频或者使用Camera预览，我们需要指定其Buffer类型
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //下面开始实例化MediaPlayer对象
        player = new MediaPlayer();
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setOnInfoListener(this);
        player.setOnPreparedListener(this);
        player.setOnSeekCompleteListener(this);
        player.setOnVideoSizeChangedListener(this);

        try {
            player.setDataSource(url);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //然后，我们取得当前Display对象
        currDisplay = this.getWindowManager().getDefaultDisplay();
    }

    @Override
    public void onClick(View v) {
        if(player.isPlaying()){
            player.pause();
        }else{
            player.start();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        LOG.E(TAG, "surfaceCreated");
        // 当SurfaceView中的Surface被创建的时候被调用
        //在这里我们指定MediaPlayer在当前的Surface中进行播放
        player.setDisplay(holder);
        //在指定了MediaPlayer播放的容器后，我们就可以使用prepare或者prepareAsync来准备播放了
        player.prepareAsync();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        LOG.E(TAG, "surfaceChanged");

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LOG.E(TAG, "surfaceDestroyed");

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        LOG.E(TAG, "onCompletion");
        Toast.makeText(this, "onCompletion", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        LOG.E(TAG, "onError what : " + what);
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                android.util.Log.v("Play Error:::", "MEDIA_ERROR_SERVER_DIED");
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                android.util.Log.v("Play Error:::", "MEDIA_ERROR_UNKNOWN");
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        LOG.E(TAG, "onInfo what : " + what);
        // 当一些特定信息出现或者警告时触发
        switch(what){
            case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                break;
            case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                break;
            case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                break;
            case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        LOG.E(TAG, "onPrepared");
        // 当prepare完成后，该方法触发，在这里我们播放视频

        //首先取得video的宽和高
        vWidth = player.getVideoWidth();
        vHeight = player.getVideoHeight();

        if(vWidth > currDisplay.getWidth() || vHeight > currDisplay.getHeight()){
            //如果video的宽或者高超出了当前屏幕的大小，则要进行缩放
            float wRatio = (float)vWidth/(float)currDisplay.getWidth();
            float hRatio = (float)vHeight/(float)currDisplay.getHeight();

            //选择大的一个进行缩放
            float ratio = Math.max(wRatio, hRatio);

            vWidth = (int)Math.ceil((float)vWidth/ratio);
            vHeight = (int)Math.ceil((float)vHeight/ratio);

            //设置surfaceView的布局参数
            surfaceView.setLayoutParams(new LinearLayout.LayoutParams(vWidth, vHeight));

            //然后开始播放视频

            player.start();
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        LOG.E(TAG, "onSeekComplete");

    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        LOG.E(TAG, "onVideoSizeChanged");

    }
}
