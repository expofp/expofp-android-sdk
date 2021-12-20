# ExpoFP Fplan


### Usage

Add Android permissions:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.expofp.myapplication">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
    ...
```

Add FplanView to layout:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <com.expofp.fplan.FplanView
        android:id="@+id/fplanView"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
  
</androidx.constraintlayout.widget.ConstraintLayout>
```

Init FplanView:

```java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FplanView fplanView = findViewById(R.id.fplanView);
        fplanView.init("https://developer.expofp.com/examples/autumnfair.html", null, null, null);
    }
}
```
