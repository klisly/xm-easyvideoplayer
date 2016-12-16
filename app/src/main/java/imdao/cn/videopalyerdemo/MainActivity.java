package imdao.cn.videopalyerdemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.File;

import imdao.cn.library.VideoPlayerActivity;

public class MainActivity extends AppCompatActivity {
    Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        uri=Uri.fromFile(new File("/storage/emulated/0/DCIM/Camera/VID_20161107_121820.mp4"));
        findViewById(R.id.open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
                intent.putExtra(VideoPlayerActivity.PARAM_SRC, uri);
                startActivity(intent);
            }
        });

        findViewById(R.id.pick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
                intent.putExtra(VideoPlayerActivity.PARAM_SRC, uri);
                intent.putExtra(VideoPlayerActivity.PARAM_TYPE, VideoPlayerActivity.TYPE_PICK);
                startActivityForResult(intent, VideoPlayerActivity.REQUEST_PICK);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if (requestCode == VideoPlayerActivity.REQUEST_PICK) {
                Log.i(MainActivity.class.getSimpleName(), "result:"+data.toString());
            }
        }
    }
}
