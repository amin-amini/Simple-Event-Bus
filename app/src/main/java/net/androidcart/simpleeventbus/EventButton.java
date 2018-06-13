package net.androidcart.simpleeventbus;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

/**
 * Created by Amin Amini on 6/13/18.
 */

public class EventButton extends android.support.v7.widget.AppCompatButton {
    public EventButton(Context context) {
        super(context);
        init();
    }

    public EventButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EventButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init(){
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getInstance().broadcastButtonClick(EventButton.this);
            }
        });
    }

}
