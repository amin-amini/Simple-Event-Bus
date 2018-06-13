package net.androidcart.simpleeventbus;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Amin Amini on 6/13/18.
 */

public class EventEditText extends android.support.v7.widget.AppCompatEditText {
    public EventEditText(Context context) {
        super(context);
        init();
    }

    public EventEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EventEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init(){
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                EventBus.getInstance().broadcastTextChanged(EventEditText.this, charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

}
