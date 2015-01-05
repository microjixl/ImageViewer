/**
 *
 * copyright(C)2014- 
 *
 */
package com.mj.imageviewer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

/**
 * @author jixiaolong<microjixl@gmail.com>
 * @code: create：15-1-5下午12:52
 */
public class Main extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViewById(R.id.ib_thumb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Rect startBounds = new Rect();
                v.getGlobalVisibleRect(startBounds);
                Intent intent = new Intent(Main.this, PictureViewActivity.class);
                intent.putExtra("BOUND",startBounds);
                intent.putExtra("ORIENTATION",getResources().getConfiguration().orientation);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }
}
