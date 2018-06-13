# Simple Event Bus for Android

Simple Event Bus is An Android library to easily create and use an Event Bus .
It allows you to create different methods with different arguments and easily broadcast events and receive them. Using this library you don't need to include a switch case to detect which event have been raised and also you don't need to cast objects. On the other side because you are only registering one object for all events (instead of `n` same objects for different events) this library consumes RAM as low as possible. What you have to do is to create an interface and register/unregister your receiver and of course implement your callbacks.

# Installation
* Add jitpack.io to your root gradle file (project level) :
	```gradle
  allprojects {
  		repositories {
  			...
  			maven { url 'https://jitpack.io' }
  		}
  	}
	```

* Add the dependency in your app build.gradle
	```gradle
  dependencies {
      implementation 'com.github.amin-amini:Simple-Event-Bus:1.0.0'
      annotationProcessor 'com.github.amin-amini:Simple-Event-Bus:1.0.0'
  }
	```

# Create Event Bus
Create an interface to describe your events and annotate your desired event bus name e.g. `EventBus`:
```java

@SimpleEventBusSchema("EventBus")
public interface EventBusSchema {
    void ButtonClick(Button b);
    void TextChanged(EditText e, String text);
}

```

# <b>NOTE: after creation or any changes to your schema you have to rebuild your project so SimpleEventBus can generate required classes</b>


# Register/Unregister
You can register/unregister Event Bus in every single Activity but I personally prefer a BaseActivity to do this:

* **BaseActivity.java**

	```java
    abstract public class BaseActivity extends AppCompatActivity {

        public EventBus bus = EventBus.getInstance();
        private EventBusCallbackMapper busMapper;

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
        
        abstract protected EventBusCallbackMethods getEventBusCallback();
    }
	```
# Handle Receiving Events

* **MainActivity.java**

	```java
    public class MainActivity extends Base Activity {
        ...
	
        @Override
        protected EventBusCallbackMethods getEventBusCallback() {
            return new EventBusCallbackMethods(){
                @Override
                public void onButtonClick(Button b) {
                    Toast.makeText(getApplicationContext(), "Button Clicked : " + b.getText(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onTextChanged(EditText e, String text) {
                    Toast.makeText(getApplicationContext(), "Text Changed : " + text, Toast.LENGTH_SHORT).show();
                }
            };
        }
    }
	```

# Broadcast
you can easily broadcast your event e.g. in Button onClickListener:

```java
EventBus.getInstance().broadcastButtonClick(this);
```
or in EditText onTextChanged:

```java
EventBus.getInstance().broadcastTextChanged(this, charSequence.toString());
```

# That`s it !
Also for Fragments you can register bus in `onCreateView` and unregister it in `onDestroyView` and for Views you can register bus in `onAttachedToWindow` and unregister it in `onDetachedFromWindow`

<b>WARNING: It's obvious that if you forget to unregister Event Bus properly memory leaks might occur</b>

# Screenshots
![Screenshot 1](https://raw.githubusercontent.com/amin-amini/Simple-Event-Bus/master/images/img1.jpg)

![Screenshot 2](https://raw.githubusercontent.com/amin-amini/Simple-Event-Bus/master/images/img2.jpg)



# <b>NOTE: Once again I'm going to tell you that after any changes to your schema you have to rebuild your project so SimpleEventBus can generate required classes</b>



