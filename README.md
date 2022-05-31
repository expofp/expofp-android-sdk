# ExpoFP Fplan - Android library for displaying expo plans

Create your expo plan on the website https://expofp.com then use the URL of the created expo plan when you working with the library

![1024-1 — копия](https://user-images.githubusercontent.com/60826376/146822762-66188b40-54f4-49dd-9479-9166d8aec672.jpeg)

## Version for iOS

https://github.com/expofp/expofp-ios-sdk

## Usage example

https://github.com/expofp/expofp-java-example

## Add to project

Add Maven repository reference to settings.gradle file(in root of your project):

```java
repositories {
    maven { url "https://s01.oss.sonatype.org/content/repositories/releases" }
    ...
}
```

Add dependency to build.gradle file(in module):

```java
dependencies {
    implementation 'com.expofp:fplan:1.1.9'
    ... 
}
```

## Usage

Add Android permissions:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.expofp.myapplication">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>

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
package com.example.expofp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.expofp.fplan.FplanEventListener;
import com.expofp.fplan.FplanView;
import com.expofp.fplan.Route;

public class MainActivity extends AppCompatActivity {

    private FplanView _fplanView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _fplanView = findViewById(R.id.fplanView);
        _fplanView.init("https://demo.expofp.com", new FplanEventListener() {
            @Override
            public void onFpConfigured() {

            }

            @Override
            public void onBoothSelected(String boothName) {

            }

            @Override
            public void onRouteCreated(Route route) {

            }
        });
    }

    public void onSelectBoothClick(View view) {
        _fplanView.selectBooth("720");
    }

    public void onBuidDirectionClick(View view) {
        _fplanView.buildRoute("720", "751", false);
    }
}
```

## Functions

Select booth:

```java
fplanView.selectBooth("720");
```

Build route:

```java
fplanView.buildRoute("720", "751");
```

Set current position(Blue-dot):

```java
fplanView.setCurrentPosition(2875, 1734);
```

Clear floor plan:

```java
fplanView.clear();
```
