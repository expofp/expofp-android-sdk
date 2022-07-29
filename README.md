# ExpoFP Fplan
> Android library for displaying expo plans.
> Create your expo plan on the website https://expofp.com then use the URL of the created expo plan when you working with the library.
> Live demo [_here_](https://github.com/expofp/expofp-example-android).
> Version for iOS [_here_](https://github.com/expofp/expofp-ios-sdk).

![1024-1 — копия](https://user-images.githubusercontent.com/60826376/146822762-66188b40-54f4-49dd-9479-9166d8aec672.jpeg)

## Table of Contents
* [2.0.0 version](#2.0.0)
  * [What's New](#2.0.0-what-is-new)
  * [Migration from 1.1.10](#2.0.0-migration-1.1.10)
  * [Setup](#2.0.0-setup)
  * [Permissions](#2.0.0-permissions)
  * [Usage](#2.0.0-usage)
  * [Functions](#2.0.0-functions)
  * [Events](#2.0.0-events)
  * [Navigation](#2.0.0-navigation)
  * [Crowdconnected location provider](#2.0.0-cc-navigation)
* [1.1.10 version](#1.1.10)
  * [Setup](#1.1.10-setup)
  * [Usage](#1.1.10-usage)
  * [Functions](#1.1.10-functions)
  * [Events](#1.1.10-events)


## 2.0.0 version<a id='2.0.0'></a>

### What's New in ExpoFP Fplan version 2.0.0<a id='2.0.0-what-is-new'></a>

In the new version of the library, all FplanView settings have been moved to the Settings class. Some function and event names have been changed to match the [JavaScript API Reference](https://developer.expofp.com/reference). Navigation from CrowdConnected has also been added.

### Migration from 1.1.10<a id='2.0.0-migration-1.1.10'></a>

All FplanView settings have been moved to a separate class:

```java
com.expofp.fplan.Settings settings = new com.expofp.fplan.Settings("https://demo.expofp.com", false, false)
                //.withLocationProvider(new CrowdConnectedProvider(getApplication(), new com.expofp.crowdconnected.Settings("APP_KEY","TOKEN","SECRET")))
                //.withGlobalLocationProvider()
                .withEventsListener(new FplanEventsListener() {
                    @Override
                    public void onFpConfigured() { }

                    @Override
                    public void onBoothClick(String boothName) { }

                    @Override
                    public void onDirection(Route route) { }
                });
                
fplanView.init(settings);
```

Some FplanView functions have been renamed:

```java
buildRoute -> selectRoute

setCurrentPosition -> selectCurrentPosition
```

Some FplanEventsListener methods have been renamed:

```java
onBoothSelected -> onBoothClick

onRouteCreated -> onDirection
```

### Setup<a id='2.0.0-setup'></a>

Add Maven repository reference to settings.gradle file(in root of your project):

```java
repositories {
    maven { url "https://s01.oss.sonatype.org/content/repositories/releases" }
    
    //If you want to use navigation from CrowdConnected, add a link to the repository
    //maven { url "https://maven2.crowdconnected.net/" }
    ...
}
```

Add dependency to build.gradle file(in module):

```java
dependencies {
    implementation 'com.expofp:common:2.0.0'
    implementation 'com.expofp:fplan:2.0.0'
    
    //If you want to use navigation from CrowdConnected, add a link to the package
    //implementation 'com.expofp:crowdconnected:2.0.0'
    ... 
}
```

### Permissions<a id='2.0.0-permissions'></a>

Now there is no need to specify permissions, now each package contains a manifest file with permissions.

The com.expofp.fplan package contains a manifest file with permissions:
```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
<uses-permission android:name="android.permission.INTERNET"/>
```

The com.expofp.crowdconnected package contains a manifest file with permissions:
```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
```

### Usage<a id='2.0.0-usage'></a>

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
//noOverlay - Hides the panel with information about exhibitors
Settings settings = new Settings("https://demo.expofp.com", false)
                //.withLocationProvider(new CrowdConnectedProvider(getApplication(), new com.expofp.crowdconnected.Settings("APP_KEY","TOKEN","SECRET")))
                //.withGlobalLocationProvider()
                .withEventsListener(new FplanEventsListener() {
                    @Override
                    public void onFpConfigured() {
                    }

                    @Override
                    public void onBoothClick(String s) {
                    }

                    @Override
                    public void onDirection(Route route) {
                    }
                });

_fplanView = findViewById(R.id.fplanView);
_fplanView.init(settings);
```

Stop FplanView.  
After you finish working with FplanView, you need to stop it.  
To do this, you need to call the 'destroy' function:  

```java
_fplanView = findViewById(R.id.fplanView);
_fplanView.destroy();
```

### Functions<a id='2.0.0-functions'></a>

Select booth:

```java
_fplanView.selectBooth("720");
```

Select exhibitor:

```java
_fplanView.selectExhibitor("ExpoPlatform");
```

Build route:

```java
_fplanView.selectRoute("720", "751");
```

Set current position(Blue-dot):

```java
_fplanView.selectCurrentPosition(2875, 1734);
```

Clear floor plan:

```java
_fplanView.clear();
```

### Events<a id='2.0.0-events'></a>

Floor plan ready event:

```java
@Override
public void onFpConfigured() {
}
```

Select booth event:

```java
@Override
public void onBoothClick(String boothName) {
}
```

Route create event:

```java
@Override
public void onDirection(Route route) {
}
```

### Navigation<a id='2.0.0-navigation'></a>

There are 2 ways to use navigation in FplanView. The first way is to explicitly specify the provider in the FplanView settings. In this case, FplanView will start and stop the LocationProvider on its own.

```java
Settings settings = new Settings("https://demo.expofp.com", false)
                .withLocationProvider(SOME_LOCATION_PROVIDER);

_fplanView = findViewById(R.id.fplanView);
_fplanView.init(settings);
```

The second way is to run the GlobalLocationProvider when the program starts:

```java
GlobalLocationProvider.init(SOME_LOCATION_PROVIDER);
GlobalLocationProvider.start();
```

When using the GlobalLocationProvider in the FplanView settings, you need to call the 'withGlobalLocationProvider' function:

```java
Settings settings = new Settings("https://demo.expofp.com", false)
                 .withGlobalLocationProvider();

_fplanView = findViewById(R.id.fplanView);
_fplanView.init(settings);
```

When the program terminates, the GlobalLocationProvider must also be stopped:

``0`java
GlobalLocationProvider.stop();
```


### Crowdconnected location provider<a id='2.0.0-cc-navigation'></a>

LocationProvider initialization:

```java
com.expofp.crowdconnected.Settings lpSettings = new com.expofp.crowdconnected.Settings("APP_KEY", "TOKEN", "SECRET", Mode.IPS_ONLY);
LocationProvider locationProvider = new CrowdConnectedProvider(getApplication(), lpSettings);
```

Aliases:

```java
com.expofp.crowdconnected.Settings lpSettings = new com.expofp.crowdconnected.Settings("APP_KEY", "TOKEN", "SECRET", Mode.IPS_ONLY);
lpSettings.setAlias("KEY_1", "VALUE_1");
lpSettings.setAlias("KEY_2", "VALUE_2");
LocationProvider locationProvider = new CrowdConnectedProvider(getApplication(), lpSettings);
```

Notification settings:

```java
com.expofp.crowdconnected.Settings lpSettings = new com.expofp.crowdconnected.Settings("APP_KEY", "TOKEN", "SECRET", Mode.IPS_ONLY);
lpSettings.setServiceNotificationInfo("NOTIFICATION_TEXT", SERVICE_ICON);
LocationProvider locationProvider = new CrowdConnectedProvider(getApplication(), lpSettings);
```

## 1.1.10 version<a id='1.1.10'></a>

### Setup<a id='1.1.10-setup'></a>

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
    implementation 'com.expofp:fplan:1.1.10'
    ... 
}
```

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

### Usage<a id='1.1.10-usage'></a>

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
_fplanView = findViewById(R.id.fplanView);

//noOverlay - Hides the panel with information about exhibitors
_fplanView.init("https://demo.expofp.com", false, new FplanEventListener() {
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
```

### Functions<a id='1.1.10-functions'></a>

Select booth:

```java
_fplanView.selectBooth("720");
```

Build route:

```java
_fplanView.buildRoute("720", "751");
```

Set current position(Blue-dot):

```java
_fplanView.setCurrentPosition(2875, 1734);
```

Clear floor plan:

```java
_fplanView.clear();
```

### Events<a id='1.1.10-events'></a>

Floor plan ready event:

```java
@Override
public void onFpConfigured() {
}
```

Select booth event:

```java
@Override
public void onBoothSelected(String boothName) {
}
```

Route create event:

```java
@Override
public void onRouteCreated(Route route) {
}
```
