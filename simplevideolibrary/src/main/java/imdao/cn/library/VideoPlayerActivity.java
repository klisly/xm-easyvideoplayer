package imdao.cn.library;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class VideoPlayerActivity extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnVideoSizeChangedListener {
    private static String TAG = VideoPlayerActivity.class.getSimpleName();
    public static final int REQUEST_PICK = 5098;
    public static final String TYPE_PICK = "TYPE_PICK";
    public static final String TYPE_PREVIEW = "TYPE_PREVIEW";
    public static String PARAM_SRC = "PARAM_SRC";
    public static String PARAM_TYPE = "PARAM_TYPE";

    public Uri src;
    private ImageView playButton;
    private SurfaceView sv;
    private MediaPlayer mediaPlayer;
    private int svWidth;
    private int svHeight;
    private int vWidht;
    private int vHeight;
    private ImageView preview;
    private final Handler mHandler = new FixHandler(this);
    private static final int BITMAP_LOADED = 1;
    private String previewType = TYPE_PREVIEW;

    private static class FixHandler extends Handler {
        private final WeakReference<VideoPlayerActivity> mActivity;

        public FixHandler(VideoPlayerActivity activity) {
            mActivity = new WeakReference<VideoPlayerActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity.get() == null) {
                return;
            }
            switch (msg.what) {
                case BITMAP_LOADED:
                    mActivity.get().initThumbnail((Bitmap) msg.obj);
                    break;
                default:
                    return;
            }
        }
    }

    private void initThumbnail(Bitmap bitmap) {
        preview.setImageBitmap(bitmap);
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.video_preview);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        initToolBar();
        src = getIntent().getParcelableExtra(PARAM_SRC);
        Log.i(TAG, "src:" + src);
        if (src == null) {
            Toast.makeText(getApplicationContext(), R.string.no_video, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        previewType = getIntent().getStringExtra(PARAM_TYPE);
        if (previewType == null) {
            previewType = TYPE_PREVIEW;
        }
        playButton = (ImageView) findViewById(R.id.iv_play);
        preview = (ImageView) findViewById(R.id.preview);
        sv = (SurfaceView) findViewById(R.id.sv);
        sv.getHolder().addCallback(this);
//        sv.setBackgroundDrawable(getResources().getDrawable(R.drawable.video_texture));
        findViewById(R.id.activity_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    onPausePlay();
                } else {
                    playButton.setVisibility(View.INVISIBLE);
                    onstartPlay();
                }
            }
        });
        initSvSize();
        loadImagePreview();
        initChooseListener();
    }

    private void initChooseListener() {
        if (TYPE_PICK.equals(previewType)) {
            findViewById(R.id.choose)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, src);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    });
        } else {
            findViewById(R.id.choose).setVisibility(View.GONE);
        }
    }

    private void loadImagePreview() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Bitmap bm = ThumbnailUtils.createVideoThumbnail(src.getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
                if (bm != null) {
                    mHandler.obtainMessage(BITMAP_LOADED, bm).sendToTarget();
                }
            }
        });
    }

    private void initSvSize() {
        sv.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int tmpWidth = right - left;
                int tmpHeight = bottom - top;
                if (tmpHeight != svWidth || tmpHeight != svHeight) {
                    svWidth = tmpWidth;
                    svHeight = tmpHeight;
                    Log.i(VideoPlayerActivity.class.getSimpleName(), "sv size changed sw:" + svWidth + "sh:" + svHeight);
                    refreshSvSize();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        onStopPlay();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(VideoPlayerActivity.class.getSimpleName(), "svw:" + sv.getLayoutParams().height + "svh:" + sv.getLayoutParams().width);
        findViewById(R.id.activity_video).setBackgroundColor(getResources().getColor(R.color.black_p50));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(BITMAP_LOADED);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void onCompletePlay() {
        Log.i(TAG, "curl pos:" + mediaPlayer.getCurrentPosition() + " duration:" + mediaPlayer.getDuration());
        preview.setAlpha(0.2f);
        preview.animate()
                .alpha(1.0f)
                .setDuration(600)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        preview.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();


        playButton.setAlpha(0.2f);
        playButton.animate()
                .alpha(1.0f)
                .setDuration(600)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        playButton.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();
    }

    private void onPausePlay() {
        playButton.setVisibility(View.VISIBLE);
        mediaPlayer.pause();
    }

    private void onstartPlay() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            playButton.setVisibility(View.INVISIBLE);
            return;
        }
        try {

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(this, src);
            mediaPlayer.prepareAsync();
            mediaPlayer.setDisplay(sv.getHolder());
            mediaPlayer.setOnVideoSizeChangedListener(this);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    playButton.setVisibility(View.INVISIBLE);
                    preview.animate()
                            .alpha(0.8f)
                            .setDuration(300)
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    sv.setBackgroundDrawable(null);
                                    preview.setVisibility(View.INVISIBLE);
                                    preview.setAlpha(1.0f);
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            })
                            .start();
                    mediaPlayer.start();

                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.i(TAG, "position:" + mp.getCurrentPosition());
                    onCompletePlay();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            finish();
            Toast.makeText(getApplicationContext(), R.string.play_error, Toast.LENGTH_SHORT).show();
            return;
        }

    }

    protected void onStopPlay() {
        playButton.setVisibility(View.VISIBLE);
        preview.setVisibility(View.VISIBLE);
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaPlayer = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        onStopPlay();
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        if (vHeight != height || width != vWidht) {
            vHeight = height;
            vWidht = width;
            Log.i(TAG, "has changed video size w:" + width + "video size h:" + height);
            refreshSvSize();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }


    private void refreshSvSize() {
        if (svWidth == 0 || svHeight == 0 || vWidht == 0 || vHeight == 0) {
            return;
        }

        float rate = vWidht * 1.0f / vHeight;
        Log.i(TAG, "video w/h rate:" + rate);
        if (svHeight * rate > svWidth) {
            svHeight = (int) (svWidth / rate);
        } else {
            svWidth = (int) (svHeight * rate);
        }
        ViewGroup.LayoutParams params = sv.getLayoutParams();
        params.width = svWidth;
        params.height = svHeight;
        sv.setLayoutParams(params);
        params = preview.getLayoutParams();
        params.width = svWidth;
        params.height = svHeight;
        preview.setLayoutParams(params);
    }

    /**
     * 获取视频指定位置缩略图
     *
     * @param filePath
     * @param time     seconds
     * @return
     */
    private static Bitmap createVideoThumbnail(String filePath, int time) {
        Bitmap bitmap = null;
        if (filePath == null || filePath == "") {
            bitmap = Bitmap.createBitmap(480, 320, Bitmap.Config.ARGB_8888);
        } else {
            android.media.MediaMetadataRetriever retriever = new android.media.MediaMetadataRetriever();
            try {
                retriever.setDataSource(filePath);
                if (time == 0) {
                    bitmap = Bitmap.createBitmap(480, 320, Bitmap.Config.ARGB_8888);
                } else {
                    bitmap = retriever.getFrameAtTime(time * 1000); //position为全局变量，即视频播放到的具体位置，单位为毫秒
                }
            } catch (IllegalArgumentException ex) {
                // Assume this is a corrupt video file
            } catch (RuntimeException ex) {
                // Assume this is a corrupt video file.
            } finally {
                try {
                    retriever.release();
                } catch (RuntimeException ex) {
                    // Ignore failures while cleaning up.
                }
            }
        }
        return bitmap;
    }

}