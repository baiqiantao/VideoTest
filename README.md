# SurfaceViewMediaPlayer

实现的功能：
可播放本地视频或网络视频，可控制播放或暂停
最小化时保存播放位置及播放状态，resume时恢复所有状态；
横竖屏切换时保持切换前的位置及状态
在屏幕上竖直滑动可调节屏幕亮度和音量
可改变视频显示样式（有bug）
可获取视频缩略图及视频大小


![](http://images2015.cnblogs.com/blog/795730/201604/795730-20160422215427773-1587396398.png)


![](http://images2015.cnblogs.com/blog/795730/201604/795730-20160422215428445-2005366615.jpg)


![](http://images2015.cnblogs.com/blog/795730/201604/795730-20160422145223554-1716491344.png)

权限

    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <!-- 网络权限 -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <!-- 滑动改变屏幕亮度/音量 -->
    <uses-permission android:name="android.permission.WRITE_SETTINGS" />
    <uses-permission android:name="android.permission.VIBRATE" />
