package jp.tanaka.ex.opencv_face;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

public class Menu extends Activity {

    ImageButton Camera_Button;
    ImageButton Add_Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Camera_Button = (ImageButton) findViewById(R.id.button1);
        Add_Button = (ImageButton) findViewById(R.id.button2);

        Camera_Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Menu.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Add_Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Menu.this, Add_Image.class);
                startActivity(intent);
            }
        });


    }
}