import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.MediaStore.Video;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.VideoView;
public class VideoUtils {
    /** 
     * 根据手机的分辨率从 dp或 sp 的单位 转成为 px
     */
    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    /** 
    * 根据手机的分辨率从 px 的单位转成为 dp 或 sp
    */
    public static int px2dp(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
    /**
    * 获取屏幕高度
    */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }
    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }
    /**
     * 设置Activity为全屏模式
     */
    public static void setActivityFullScreenMode(Activity context) {
        context.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    /**
     * 设置Activity为窗口模式???
     */
    public static void setActivityWindowScreenMode(Activity context) {
        WindowManager.LayoutParams attrs = context.getWindow().getAttributes();
        attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        context.getWindow().setAttributes(attrs);
        context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }
    //********************************************************************************************************************
    public static final int FULL_SCREEN = 1;//全屏拉伸模式
    public static final int SCREEN_WINDOW = 2;//指定大小模式
    public static final int SCREEN_WINDOW_ADAPT = 3;//自适应宽高模式
    /**
     * 设置VideoView的全屏和窗口模式。<br>
     * 全屏拉伸模式 : {@link #FULL_SCREEN} <br>
     * 指定大小模式: {@link #SCREEN_WINDOWE}<br>
     * 自适应宽高模式 : {@link #SCREEN_WINDOW_ADAPT}
     * 这里有很多bug******************************************************************************************************************************************************
     */
    public static void setVideoViewLayoutParams(Activity context, VideoView mVideoView, int paramsType) {
        int width, height;
        switch (paramsType) {
        case FULL_SCREEN:
            setActivityFullScreenMode(context);
            RelativeLayout.LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            ((View) mVideoView.getParent()).setLayoutParams(layoutParams);//布局里我们给VideoView设置了一个父布局
            mVideoView.setLayoutParams(layoutParams);
            break;
        case SCREEN_WINDOW:
            width = getScreenWidth(context) * 2 / 3;
            height = VideoUtils.getScreenHeight(context) * 2 / 3;
            RelativeLayout.LayoutParams LayoutParams = new LayoutParams(width, height);
            LayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            mVideoView.setLayoutParams(LayoutParams);
            break;
        default://先获取VideoView中资源的大小，然后分别和VideoView控件的大小作对比，当资源宽高大于控件时，缩小，否则放大。
            //和加载图片一个样，但是VideoView没有scaleType塑性
            int videoWidth = mVideoView.getWidth();
            int videoHeight = mVideoView.getHeight();
            break;
        }
    }
    //********************************************************************************************************************
    /**
     * 获取屏幕亮度模式，返回-1代表没有获取到
     * 自动调节：Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1
     * 手动调节：SCREEN_BRIGHTNESS_MODE_MANUAL=0
     */
    public static int getScreenBrightnessMode(Context context) {
        int screenMode = -1;
        try {
            screenMode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return screenMode;
    }
    /**
     * 设置屏幕亮度模式
     * 自动调节：Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1
     * 手动调节：SCREEN_BRIGHTNESS_MODE_MANUAL=0
     */
    public static void setScreenBrightnessMode(Context context, int value) {
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, value);
    }
    /**
     * 获取屏幕亮度，获取失败返回-1
     */
    public static int getScreenBrightness(Context context) {
        int bright = -1;
        try {
            bright = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return bright;
    }
    /**
     * 增加或减小当前屏幕亮度，并在整个系统上生效
    */
    public static void setScreenBrightness(Activity context, float value) {
        //设置当前【activity】的屏幕亮度
        Window mWindow = context.getWindow();
        WindowManager.LayoutParams mParams = mWindow.getAttributes();//注意：它的值是从0到1，亮度从暗到全亮
        mParams.screenBrightness += value / 255.0F;
        if (mParams.screenBrightness > 1) {//设置大于1的值后，虽然获取到的此参数的值被改了，但系统并不使用此值而是使用某一指定值设置亮度
            mParams.screenBrightness = 1;
        } else if (mParams.screenBrightness < 0.01) {
            mParams.screenBrightness = 0.01f;
        }
        mWindow.setAttributes(mParams);
        // 保存设置为【系统】屏幕亮度值。注意它的值是0-255
        int newValue = (int) (mParams.screenBrightness * 255.0F + value);// 或者= (int) (getScreenBrightness(context) + value);
        if (newValue > 255) newValue = 255;
        else if (newValue < 0) newValue = 0;
        Log.i("bqt", "亮度=" + mParams.screenBrightness + "，亮度=" + newValue);
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, newValue);
    }
    //********************************************************************************************************************
    /**
     * 设置铃声模式
     * @param mode 声音模式：AudioManager.RINGER_MODE_NORMAL；静音模式：_SILENT；震动模式：_VIBRATE
     */
    public static void setRingerMode(Context context, int mode) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setRingerMode(mode);
    }
    /**
     * 增加或减小媒体音量
     */
    public static void setVoiceVolume(Context context, int value) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        // 注意：分为静音(0)，震动(0)，1--7 共九个等级。从静音调为1时，需要调大两个等级；从1调为0时，手机将调整为“震动模式”
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //尼玛，我获得的最大值是15
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int newVolume = currentVolume + value;
        if (newVolume > maxVolume) {
            newVolume = maxVolume;
        } else if (newVolume < 0) {
            newVolume = 0;
        }
        Log.i("bqt", "音量=" + newVolume);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
    }
    /**
     * 增加或减小媒体音量，并且显示系统音量控制提示条
     */
    public static void setVoiceVolumeAndShowNotification(Context context, boolean isAddVolume) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (isAddVolume) {
            //参数：声音类型，调整音量的方向（只能是增加、减小、不变），可选的标志位（不知道有卵用）
            //音乐：AudioManager.STREAM_MUSIC；通话：_VOICE_CALL；系统：_SYSTEM；铃声：_RING；提示音：_NOTIFICATION；闹铃：_ALARM
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);
        } else {
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
        Log.i("bqt", "音量=" + mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }
    //********************************************************************************************************************
    /**
     * 获取视频长度，获取网络视频长度时异常！
     */
    public static String getVideoLength(String filePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        String length = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return length;
    }
    /**
     * 毫秒值装换为时分秒形式
     */
    @SuppressWarnings("deprecation")
    public static String long2Time(long timeLong) {
        Date date = new Date(timeLong);
        date.setHours(date.getHours() - 8); //沃日，我们这里比标准时长快了八个小时
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss ");
        return format.format(date);
    }
    /**
     * 获取视频缩略图
     * @param filePath 视频文件的路径
     */
    public static Bitmap createVideoThumbnail(String filePath) {
        return ThumbnailUtils.createVideoThumbnail(filePath, Video.Thumbnails.MINI_KIND);
    }
    /**
    * 获取视频的缩略图
    * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
    * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
    * @param videoPath 视频的路径
    * @param width 指定输出视频缩略图的宽度
    * @param height 指定输出视频缩略图的高度度
    * @param kind 参照Thumbnails类中的常量MINI_KIND(512 x 384) 和 MICRO_KIND(96 x 96)
    */
    public static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
        // 获取视频的缩略图
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }
    /**
     * 利用反射的方式获取视频缩略图，不建议使用
     * @param filePath 视频文件的路径
     */
    public static Bitmap createVideoThumbnail2(String filePath) {
        Class<?> clazz = null;
        Object instance = null;
        try {
            clazz = Class.forName("android.media.MediaMetadataRetriever");
            instance = clazz.newInstance();
            Method method = clazz.getMethod("setDataSource", String.class);
            method.invoke(instance, filePath);
            if (Build.VERSION.SDK_INT <= 9) {
                return (Bitmap) clazz.getMethod("captureFrame").invoke(instance);
            } else {
                byte[] data = (byte[]) clazz.getMethod("getEmbeddedPicture").invoke(instance);
                if (data != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    if (bitmap != null) return bitmap;
                }
                return (Bitmap) clazz.getMethod("getFrameAtTime").invoke(instance);
            }
        } catch (Exception ex) {
        } finally {
            try {
                if (instance != null) {
                    clazz.getMethod("release").invoke(instance);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
    /**
     * 将Drawable转化为Bitmap
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        // 取 drawable 的长宽，颜色格式  
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        // 创建对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }
    //********************************************************************************************************************
    public static final int NETWORK_TYPE_INVALID = 0;
    public static final int NETWORK_TYPE_MOBILE = 1;
    public static final int NETWORK_TYPE_WIFI = 2;
    /**
     * 获取当前网络连接状况
     * 网络不可用 : {@link #NETWORK_TYPE_INVALID} <br>
     * 蜂窝网络(手机流量) : {@link #NETWORK_TYPE_MOBILE}<br>
     * WIFI连接 : {@link #NETWORK_TYPE_WIFI}
     */
    public static int getNetworkType(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            String type = networkInfo.getTypeName();
            if (type.equalsIgnoreCase("WIFI")) {
                return NETWORK_TYPE_WIFI;
            } else if (type.equalsIgnoreCase("MOBILE")) {
                return NETWORK_TYPE_MOBILE;
            }
        }
        return NETWORK_TYPE_INVALID;
    }
}