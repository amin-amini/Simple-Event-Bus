package net.androidcart.simpleeventbus;

import android.widget.Button;
import android.widget.EditText;

import net.androidcart.simpleeventbusschema.SimpleEventBusSchema;

/**
 * Created by Amin Amini on 6/13/18.
 */

@SimpleEventBusSchema("EventBus")
public interface EventBusSchema {
    void ButtonClick(Button b);
    void TextChanged(EditText e, String text);
}
