package net.androidcart.simpleeventbus;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Amin Amini on 6/13/18.
 */

abstract public class BaseActivity extends AppCompatActivity {

    public EventBus bus = EventBus.getInstance();
    private EventBusCallbackMapper busMapper;
    abstract protected EventBusCallbackMethods getEventBusCallback();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        busMapper = new EventBusCallbackMapper(getEventBusCallback());
        bus.subscribe(busMapper);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bus.unregister(busMapper);
    }
}
