package saim.hassan.arfypserver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnsignn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnsignn = (Button)findViewById(R.id.btnsigninmain);
        btnsignn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent hm = new Intent(MainActivity.this,Home.class);
                startActivity(hm);
            }
        });
    }
}
