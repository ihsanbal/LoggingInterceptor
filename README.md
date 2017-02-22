#LoggingInterceptor

Interceptor for OkHttp3 with pretty logger - [OkHttp3](https://github.com/square/okhttp) 

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-StreetView-green.svg?style=flat-square)](http://android-arsenal.com/details/1/2972)
[![API](https://img.shields.io/badge/API-9%2B-brightgreen.svg?style=flat-square)](https://android-arsenal.com/api?level=9)

<p align="center">
    <img src="https://github.com/ihsanbal/LoggingInterceptor/blob/master/logging.gif" width="580" height="440"/>
</p>

Usage
--------

```java

OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(new LoggingInterceptor.Builder()
                .loggable(BuildConfig.DEBUG)
                .setLevel(Level.BASIC)
                .log(Log.INFO)
                .request("Request")
                .response("Response")
                .addHeader("version", BuildConfig.VERSION_NAME)
                .build());
        client.build();
```

Download
--------

Gradle:
```groovy
repositories {
	    maven {
	        url "https://jitpack.io"
	    }
	}
	
dependencies {
	        compile 'com.github.ihsanbal:LoggingInterceptor:1.0'
	}
```

Maven:
```xml
<repository>
   <id>jitpack.io</id>
   <url>https://jitpack.io</url>
</repository>

<dependency>
	    <groupId>com.github.ihsanbal</groupId>
	    <artifactId>LoggingInterceptor</artifactId>
	    <version>1.0</version>
</dependency>
```

#Notes
Use the filter & configure logcat header for a better result

<p align="left">
    <img src="https://github.com/ihsanbal/LoggingInterceptor/blob/master/images/screen_shot_5.png" width="280" height="155"/>
    <img src="https://github.com/ihsanbal/LoggingInterceptor/blob/master/images/screen_shot_4.png" width="280" height="155"/>
</p>
