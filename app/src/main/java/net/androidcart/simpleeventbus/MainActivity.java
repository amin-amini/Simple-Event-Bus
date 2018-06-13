package net.androidcart.simpleeventbus;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    protected EventBusCallbackMethods getEventBusCallback() {
        return new EventBusCallbackMethods(){
            @Override
            public void onButtonClick(Button b) {
                showToast("Button Clicked : " + b.getText() );
            }

            @Override
            public void onTextChanged(EditText e, String text) {
                showToast("Text Changed : " + text);
            }

            void showToast(CharSequence str){
                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
            }
        };
    }

}
