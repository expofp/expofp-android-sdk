# ExpoFP FplanView

![1024-1 — копия](https://user-images.githubusercontent.com/60826376/146822762-66188b40-54f4-49dd-9479-9166d8aec672.jpeg)

## Add to project

### Using Maven

Add Maven repository reference to build.gradle file(in root of your project):

```java
buildscript {
    repositories {
        maven { url "https://s01.oss.sonatype.org/content/repositories/releases" }
        ...
    }
    dependencies {
        ...
    }
}
```

Add dependency to build.gradle file(in module):

```java
dependencies {
    implementation 'com.expofp:fplan:1.0.0'
    ... 
}
```

### Manualy

Download: https://github.com/expofp/expofp-android-sdk/raw/main/fplan.aar

Build -> Edit Libraries And Dependencies

![1](https://user-images.githubusercontent.com/60826376/146797004-f32a33a7-15bd-4b89-ba18-714302d361fb.png)

Add JAR/AAR Dependency:

![2](https://user-images.githubusercontent.com/60826376/146797025-8802e787-4c2a-4de2-a6b5-17eccf3324a4.png)

Specify fplan.aar file path:

![3](https://user-images.githubusercontent.com/60826376/146797034-a36e1094-7eb3-449b-a27a-373bbeecf1ef.png)


## Usage

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


## Functions

Select booth:

```java
fplanView.selectBooth("1306");
```

Buid route:

```java
fplanView.buidRoute("1306", "2206");
```


## Events


### Handling the plan load event

Callback:

```java
class FpCallback implements FpConfiguredCallback{

    private FplanView _fplanView;

    public FpCallback(FplanView fplanView){
        _fplanView = fplanView;
    }

    @Override
    public void onFpConfigured() {
        //Some code
    }
}
```

Init:

```java
fplanView.init("https://developer.expofp.com/examples/autumnfair.html", null, new FpCallback(fplanView), null);
```


### Handling the booth selection event

Callback:

```java
class BoothCallback implements BoothSelectedCallback {

    private FplanView _fplanView;

    public BoothCallback(FplanView fplanView){
        _fplanView = fplanView;
    }

    @Override
    public void onBoothSelected(String boothName) {
        //Some code
    }
}
```

Init:

```java
fplanView.init("https://developer.expofp.com/examples/autumnfair.html", new BoothCallback(fplanView), null, null);
```


### Route creation event handling

Callback:

```java
class RouteCallback implements RouteCreatedCallback {

    private FplanView _fplanView;

    public RouteCallback(FplanView fplanView){
        _fplanView = fplanView;
    }

    @Override
    public void onRouteCreated(Route route) {
        //Some code
    }
}
```

Init:

```java
fplanView.init("https://developer.expofp.com/examples/autumnfair.html", null, null, new RouteCallback(fplanView));
```




