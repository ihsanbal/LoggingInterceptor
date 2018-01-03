LoggingInterceptor - Interceptor for [OkHttp3](https://github.com/square/okhttp) with pretty logger
--------

[![Build Status](https://travis-ci.org/ihsanbal/LoggingInterceptor.svg?branch=master)](https://travis-ci.org/ihsanbal/LoggingInterceptor)
[![](https://img.shields.io/badge/AndroidWeekly-%23272-blue.svg?style=flat-square)](http://androidweekly.net/issues/issue-272)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-LoggingInterceptor-green.svg?style=flat-square)](https://android-arsenal.com/details/1/5870)
[![API](https://img.shields.io/badge/API-9%2B-brightgreen.svg?style=flat-square)](http://www.oracle.com/technetwork/java/javase/downloads/jre7-downloads-1880261.html)
[![JAVA](https://img.shields.io/badge/JAVA-7-brightgreen.svg?style=flat-square)](http://www.oracle.com/technetwork/java/javase/downloads/jre7-downloads-1880261.html)
[![SwaggerUI](https://img.shields.io/badge/Swagger-mockable.io-orange.svg?style=flat-square)](https://www.mockable.io/swagger/index.html?url=https%3A%2F%2Fdemo2961085.mockable.io%3Fopenapi#!/demo2961085)

Logcat
--------
```java

 ┌────── Request ────────────────────────────────────────────────────────────────────────
 │ URL: http://demo2961085.mockable.io/post
 │ 
 │ Method: @POST
 │ 
 │ Headers:
 │ ┌ version: 1.0
 │ └ Cache-Control: Custom-Max-Value=640000
 │ 
 │ Body:
 │ {
 │    "header": "array",
 │    "sparseArray": {
 │       "mGarbage": false,
 │       "mKeys": [
 │          0,
 │          1,
 │          2,
 │          0,
 │          0
 │       ],
 │       "mSize": 3,
 │       "mValues": [
 │          1,
 │          2,
 │          3,
 │          null,
 │          null
 │       ]
 │    }
 │ }
 └───────────────────────────────────────────────────────────────────────────────────────
 ┌────── Response ───────────────────────────────────────────────────────────────────────
 │ /post - is success : true - Received in: 349ms
 │ 
 │ Status Code: 200
 │ 
 │ Headers:
 │ ┌ access-control-allow-origin: *
 │ ├ Content-Type: application/json; charset=UTF-8
 │ ├ X-Cloud-Trace-Context: 5ab0ad3fb9d7ae4dca27af3c8ef3905d
 │ ├ Date: Wed, 19 Jul 2017 08:28:56 GMT
 │ ├ Server: Google Frontend
 │ └ Content-Length: 26
 │ 
 │ Body:
 │ {
 │    "glossary": {
 │       "title": "example glossary",
 │       "GlossDiv": {
 │          "title": "S",
 │          "GlossList": {
 │             "GlossEntry": {
 │                "ID": "SGML",
 │                "SortAs": "SGML",
 │                "GlossTerm": "Standard Generalized Markup Language",
 │                "Acronym": "SGML",
 │                "Abbrev": "ISO 8879:1986",
 │                "GlossDef": {
 │                   "para": "A meta-markup language, used to create markup languages such as DocBook.",
 │                   "GlossSeeAlso": [
 │                      "GML",
 │                      "XML"
 │                   ]
 │                },
 │                "GlossSee": "markup"
 │             }
 │          }
 │       }
 │    }
 │ }
 └───────────────────────────────────────────────────────────────────────────────────────

```

Usage
--------

```java

OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(new LoggingInterceptor.Builder()
                .loggable(BuildConfig.DEBUG)
                .setLevel(Level.BASIC)
                .log(Platform.INFO)
                .request("Request")
                .response("Response")
                .addHeader("version", BuildConfig.VERSION_NAME)
                .addQueryParam("query", "0")
//              .logger(new Logger() {
//                  @Override
//                  public void log(int level, String tag, String msg) {
//                      Log.w(tag, msg);
//                  }
//              })
               .build());
        OkHttpClient okHttpClient = client.build();

//You can use with Retrofit
Retrofit retrofitAdapter = new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .baseUrl("https://.../")
            .client(okHttpClient)
            .build();
```

Download
--------

Gradle:
```groovy
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}

dependencies {
	compile('com.github.ihsanbal:LoggingInterceptor:2.0.4') {
        	exclude group: 'org.json', module: 'json'
    	}
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
	    <version>2.0.4</version>
</dependency>
```

Level
--------

```java
setLevel(Level.BASIC)
	      .NONE // No logs
	      .BASIC // Logging url,method,headers and body.
	      .HEADERS // Logging headers
	      .BODY // Logging body
```	

Platform - [Platform](https://github.com/square/okhttp/blob/master/okhttp/src/main/java/okhttp3/internal/platform/Platform.java)
--------

```java
loggable(BuildConfig.DEBUG) // enable/disable sending logs output.
log(Platform.WARN) // setting log type
```

Tag
--------

```java
tag("LoggingI") // Request & response each log tag
request("request") // Request log tag
response("response") // Response log tag

```
	
Header - [Recipes](https://github.com/square/okhttp/wiki/Recipes)
--------

```java
addHeader("token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9 ") // Adding to request
```

Notes
--------
Use the filter & configure logcat header for a better result

<p align="left">
    <img src="https://github.com/ihsanbal/LoggingInterceptor/blob/master/images/screen_shot_5.png" width="280" height="155"/>
    <img src="https://github.com/ihsanbal/LoggingInterceptor/blob/master/images/screen_shot_4.png" width="280" height="155"/>
</p>
