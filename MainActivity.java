import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;
public class MainActivity extends Activity implements OnCompletionListener, OnErrorListener, OnPreparedListener, OnTouchListener, OnClickListener,
        OnGestureListener {
    private Button btn_switch;
    private Button btn_start;
    private Button btn_fullscreen;
    private VideoView mVideoView;
    //在 VidioView 外层套一个容器，以在切换屏幕方向的时候对 rl_vv 进行拉伸，而内部的 mVideoView 会依据视频尺寸重新计算宽高。
    //若是直接具体指定了view的宽高，则视频会被拉伸。
    private RelativeLayout rl_vv;
    private ProgressBar progressBar; //加载进度条
    private int positionWhenPause = 0;//标记当视频暂停时的播放位置
    private boolean isPlayingWhenPause = false;//标记最小化或横竖屏时是否正在播放
    private GestureDetector mGestureDetector;//手势识别器，用户控制屏幕亮度和音量
    private Uri mVideoUri;
    public static final String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/b.rmvb";//本地路径
    public static final String url = "http://7xt0mj.com1.z0.glb.clouddn.com/lianaidaren.v.640.480.mp4";//网络路径
    public static final String url2 = "http://7xt0mj.com1.z0.glb.clouddn.com/xia.v.1280.720.f4v";
    public static final String url3 = "http://112.253.22.157/17/z/z/y/u/zzyuasjwufnqerzvyxgkuigrkcatxr/hc.yinyuetai.com   /D046015255134077DDB3ACA0D7E68D45.flv";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        rl_vv = (RelativeLayout) findViewById(R.id.rl_vv);
        mVideoView = (VideoView) findViewById(R.id.mVideoView);
        btn_switch = (Button) findViewById(R.id.btn_switch);
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_fullscreen = (Button) findViewById(R.id.btn_fullscreen);
        //设置显示控制条
        mVideoView.setMediaController(new MediaController(this));
        mVideoUri = Uri.parse(filePath);
        mVideoView.setVideoURI(mVideoUri);//mVideoView.setVideoPath(url);//这两个方法都可以用来播放网络视频或本地视频
        btn_switch.setOnClickListener(this);
        btn_start.setOnClickListener(this);
        btn_fullscreen.setOnClickListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnTouchListener(this);
        mVideoView.setOnClickListener(this);
        mGestureDetector = new GestureDetector(this, this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        //如果有保存位置，则跳转到暂停时所保存的那个位置
        if (positionWhenPause > 0) {
            mVideoView.seekTo(positionWhenPause);
            //如果暂停前正在播放，则继续播放，并将播放位置置为0
            if (isPlayingWhenPause) {
                mVideoView.start();
                mVideoView.requestFocus();
                positionWhenPause = 0;
            }
        }
    }
    @Override
    protected void onPause() {
        //如果当前页面暂定，则保存当前播放位置，并记录之前mVideoView是否正在播放
        isPlayingWhenPause = mVideoView.isPlaying();
        positionWhenPause = mVideoView.getCurrentPosition();
        //停止回放视频文件，先获取再stopPlayback()
        mVideoView.stopPlayback();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mVideoView) mVideoView = null;
    }
    @Override
    //横竖屏切换时更改mVideoView的大小
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mVideoView == null) {
            return;
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {//横屏
            VideoUtils.setActivityFullScreenMode(this);//设置为全屏模式
            getWindow().getDecorView().invalidate();
            //rl_vv.getLayoutParams().width = VideoUtils.getScreenWidth(this);
            rl_vv.getLayoutParams().height = VideoUtils.getScreenHeight(this);
        } else {//竖屏
            VideoUtils.setActivityWindowScreenMode(this);
            rl_vv.getLayoutParams().width = VideoUtils.getScreenWidth(this);
            //rl_vv.getLayoutParams().height = VideoUtils.dp2px(this, 400);
        }
    }
    //***************************************************************************************************************************
    @Override
    public void onPrepared(MediaPlayer mp) {
        try {
            long timeLong = Long.valueOf(VideoUtils.getVideoLength(mVideoUri.toString()));
            Toast.makeText(this, "准备好了，时长为 " + VideoUtils.long2Time(timeLong), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
        }
        //如果文件加载成功,隐藏加载进度条
        progressBar.setVisibility(View.GONE);
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
    }
    @Override
    //视频播放发生错误时回调。如果未指定回调， 或回调函数返回false，mVideoView 会通知用户发生了错误。
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
        case MediaPlayer.MEDIA_ERROR_UNKNOWN:
            Log.e("text", "发生未知错误");
            break;
        case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
            Log.e("text", "媒体服务器死机");
            break;
        default:
            Log.e("text", "onError+" + what);
            break;
        }
        switch (extra) {
        case MediaPlayer.MEDIA_ERROR_IO:
            Log.e("text", "文件或网络相关的IO操作错误");
            break;
        case MediaPlayer.MEDIA_ERROR_MALFORMED:
            Log.e("text", "比特流编码标准或文件不符合相关规范");
            break;
        case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
            Log.e("text", "操作超时");
            break;
        case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
            Log.e("text", "比特流编码标准或文件符合相关规范,但媒体框架不支持该功能");
            break;
        default:
            Log.e("text", "onError+" + extra);
            break;
        }
        return true;//经常会碰到视频编码格式不支持的情况，若不想弹出提示框就返回true
    }
    //************************************************************************************************************************************
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);//把Touch事件传递给手势识别器，这一步非常重要！
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_start:
            if (mVideoView.isPlaying()) {
                mVideoView.pause();
            } else {
                mVideoView.start();//启动视频播放
                mVideoView.requestFocus();//获取焦点
            }
            break;
        case R.id.btn_switch:
            //横竖屏切换
            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            //            btn_switch.setVisibility(View.INVISIBLE);
            break;
        case R.id.btn_fullscreen:
            VideoUtils.setVideoViewLayoutParams(this, mVideoView, VideoUtils.FULL_SCREEN);
            break;
        }
    }
    //************************************************************************************************************************************
    @Override
    //e1代表触摸时的事件，是不变的，e2代表滑动过程中的事件，是时刻变化的
    //distance是当前event2与上次回调时的event2之间的距离，代表上次回调之后到这次回调之前移动的距离
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        float FLING_MIN_BRIGHTNESS = 1.0f * VideoUtils.dp2px(MainActivity.this, 1f);//调节亮度时的敏感度，
        float FLING_MIN_VOICE = 1.0f * VideoUtils.dp2px(MainActivity.this, 5f);//调节声音时的敏感度
        float distance = Math.abs(distanceY);//竖直方向移动范围
        //在屏幕左侧滑动调节亮度，在屏幕右侧滑动调节声音
        if (e1.getX() < VideoUtils.getScreenWidth(this) / 2) {//左侧
            if (distance > FLING_MIN_BRIGHTNESS) {//移动范围满足
                if (e1.getY() - e2.getY() > 0) {//上滑
                    VideoUtils.setScreenBrightness(this, 15);//亮度增加，第二参数的大小代表着敏感度
                } else {//下滑
                    VideoUtils.setScreenBrightness(this, -15);
                }
            }
        } else {//右侧
            if (distance > FLING_MIN_VOICE) {//移动范围满足
                if (e1.getY() - e2.getY() > 0) {//上滑
                    //VideoUtils.setVoiceVolume(this, 1);//音量增加
                    VideoUtils.setVoiceVolumeAndShowNotification(this, true);//系统自动控制音量增加减小级别
                } else {//下滑
                    //    VideoUtils.setVoiceVolume(this, -1);
                    VideoUtils.setVoiceVolumeAndShowNotification(this, false);
                }
            }
        }
        return true;
    }
    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }
    @Override
    public void onLongPress(MotionEvent e) {
    }
    @Override
    public void onShowPress(MotionEvent e) {
    }
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
}