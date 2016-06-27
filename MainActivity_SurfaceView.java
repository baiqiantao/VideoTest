// 在音乐播放器基础上加***部分的内容即可播放视频；加上===部分即可控制拖动栏；加上————部分可解决MediaPlayer与SurfaceHolder同步的问题
public class MainActivity extends Activity implements OnClickListener, OnCompletionListener, OnPreparedListener, OnErrorListener,
        OnSeekBarChangeListener {
    private EditText et_path, et_Url;
    private Button bt_play, bt_playUrl, bt_pause, bt_stop, bt_replay;
    private MediaPlayer mediaPlayer;//多媒体播放器
    private static final String STATE_CONTINUE = "继续";
    private static final String STATE_PAUSE = "暂停";
    private SurfaceView sv;//****************SurfaceView是一个在其他线程中显示、更新画面的组件，专门用来完成在单位时间内大量画面变化的需求
    private SurfaceHolder holder;//****************SurfaceHolder接口为一个显示界面内容的容器
    private SeekBar seekBar;//===============进度条
    private static int savedPosition;//===============记录当前播放文件播放的进度
    private static String savedFilepath;//===============记录当前播放文件的位置
    private Timer timer;//===============定义一个计时器，每隔100ms更新一次进度条
    private TimerTask task;//===============计时器所执行的任务
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_path = (EditText) findViewById(R.id.et_path);
        et_Url = (EditText) findViewById(R.id.et_Url);
        bt_play = (Button) findViewById(R.id.bt_play);
        bt_playUrl = (Button) findViewById(R.id.bt_playUrl);
        bt_pause = (Button) findViewById(R.id.bt_pause);
        bt_stop = (Button) findViewById(R.id.bt_stop);
        bt_replay = (Button) findViewById(R.id.bt_replay);
        bt_play.setOnClickListener(this);
        bt_playUrl.setOnClickListener(this);
        bt_pause.setOnClickListener(this);
        bt_stop.setOnClickListener(this);
        bt_replay.setOnClickListener(this);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        sv = (SurfaceView) findViewById(R.id.sv);//****************
        holder = sv.getHolder();//****************得到显示界面内容的容器
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); //设置surfaceView自己不管理缓存区。虽然提示过时，但最好还是设置下
        seekBar = (SeekBar) findViewById(R.id.seekBar);//===============
        seekBar.setOnSeekBarChangeListener(this);//===============
        //在界面【最小化】时暂停播放，并记录holder播放的位置——————————————————————————————
        holder.addCallback(new Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {//holder被销毁时回调。最小化时都会回调
                if (mediaPlayer != null) {
                    Log.i("bqt", "销毁了--surfaceDestroyed" + "--" + mediaPlayer.getCurrentPosition());
                    savedPosition = mediaPlayer.getCurrentPosition();//当前播放位置
                    mediaPlayer.stop();
                    timer.cancel();
                    task.cancel();
                    timer = null;
                    task = null;
                }
            }
            @Override
            public void surfaceCreated(SurfaceHolder holder) {//holder被创建时回调
                Log.i("bqt", "创建了--" + savedPosition + "--" + savedFilepath);
                if (savedPosition > 0) {//如果记录的数据有播放进度。
                    try {
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(savedFilepath);
                        mediaPlayer.setDisplay(holder);
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            //holder宽高发生变化（横竖屏切换）时回调
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }
        });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.bt_play:
            play();
            break;
        case R.id.bt_playUrl:
            playUrl();
            break;
        case R.id.bt_pause:
            pause();
            break;
        case R.id.bt_stop:
            stop();
            break;
        case R.id.bt_replay:
            replay();
            break;
        default:
            break;
        }
    }
    /**
     * 播放本地多媒体
     */
    public void play() {
        String filepath = et_path.getText().toString().trim();
        File file = new File(filepath);
        if (file.exists() && mediaPlayer != null) {
            try {
                savedFilepath = filepath;
                mediaPlayer.setDataSource(filepath);
                mediaPlayer.setDisplay(holder);//****************在哪个容器里显示内容
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepare();
                bt_play.setEnabled(false);
                bt_playUrl.setEnabled(false);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请检查是否有写SD卡权限", 0).show();
            }
        } else {
            Toast.makeText(this, "文件不存在", 0).show();
        }
    }
    /**
     * 播放网络多媒体
     */
    public void playUrl() {
        String filepath = et_Url.getText().toString().trim();
        if (!TextUtils.isEmpty(filepath)) {
            try {
                savedFilepath = filepath;
                mediaPlayer.setDataSource(filepath);
                mediaPlayer.setDisplay(holder);//****************
                mediaPlayer.prepareAsync();//异步准备
                bt_playUrl.setEnabled(false);
                bt_play.setEnabled(false);
                Toast.makeText(MainActivity.this, "准备中，可能需要点时间……", 1).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "播放失败，请检查是否有网络权限", 0).show();
            }
        } else {
            Toast.makeText(this, "路径不能为空", 0).show();
        }
    }
    /**
     * 暂停
     */
    public void pause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                bt_pause.setText(STATE_CONTINUE);
            } else {
                mediaPlayer.start();
                bt_pause.setText(STATE_PAUSE);
                return;
            }
        }
    }
    /**
     * 停止
     */
    public void stop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();
        bt_play.setEnabled(true);
        bt_playUrl.setEnabled(true);
        bt_pause.setText("暂停");
    }
    /**
     * 重播
     */
    public void replay() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.seekTo(0);
        }
        bt_pause.setText("暂停");
    }
    //********************************************************************************************************************
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(MainActivity.this, "报错了--" + what + "--" + extra, 0).show();
        return false;
    }
    @Override
    public void onPrepared(MediaPlayer mp) {//只有准备好以后才能处理很多逻辑
        mediaPlayer.start();
        //=============
        mediaPlayer.seekTo(savedPosition);//开始时是从0开始播放，恢复时是从指定位置开始播放
        seekBar.setMax(mediaPlayer.getDuration());//将进度条的最大值设为文件的总时长
        timer = new Timer();
        task = new TimerTask() {
            public void run() {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());//将媒体播放器当前播放的位置赋值给进度条的进度
            }
        };
        timer.schedule(task, 0, 100);//0秒后执行，每隔100ms执行一次
        Toast.makeText(MainActivity.this, "准备好了！", 0).show();
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        Toast.makeText(MainActivity.this, "播放完毕！", 0).show();
        mediaPlayer.reset();
        bt_playUrl.setEnabled(true);
        bt_play.setEnabled(true);
    }
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {//进度发生变化时
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {//停止拖拽时回调
        mediaPlayer.seekTo(seekBar.getProgress());//停止拖拽时进度条的进度
    }
}