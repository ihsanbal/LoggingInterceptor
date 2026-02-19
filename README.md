LoggingInterceptor - Interceptor for [OkHttp3](https://github.com/square/okhttp) with pretty logger
--------

[![](https://img.shields.io/badge/AndroidWeekly-%23272-blue.svg?style=flat-square)](http://androidweekly.net/issues/issue-272)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-LoggingInterceptor-green.svg?style=flat-square)](https://android-arsenal.com/details/1/5870)
[![API](https://img.shields.io/badge/API-9%2B-brightgreen.svg?style=flat-square)](http://www.oracle.com/technetwork/java/javase/downloads/jre7-downloads-1880261.html)
[![](https://jitpack.io/v/ihsanbal/LoggingInterceptor.svg)](https://jitpack.io/#ihsanbal/LoggingInterceptor)
[![SwaggerUI](https://img.shields.io/badge/Swagger-mockable.io-orange.svg?style=flat-square)](https://www.mockable.io/swagger/index.html?url=https%3A%2F%2Fdemo2961085.mockable.io%3Fopenapi#!/demo2961085)

<p align="center">
    <img src="https://github.com/ihsanbal/LoggingInterceptor/blob/master/images/logcat.png"/>
</p>

Usage
--------

```kotlin
val client = OkHttpClient.Builder()
    client.addInterceptor(LoggingInterceptor.Builder()
             .setLevel(Level.BASIC)
             .log(VERBOSE)
             .addHeader("cityCode","53")
             .addQueryParam("moonStatus", "crescent")
             .build())
```

Download
--------

Gradle:

Groovy
```groovy
allprojects {
	repositories {
		maven { url 'https://jitpack.io' }
	}
}

dependencies {
    implementation('com.github.ihsanbal:LoggingInterceptor:4.0.0') {
            exclude group: 'org.json', module: 'json'
        }
}
```

kotlin DSL
```
allprojects {
	repositories {
		maven { setUrl("https://jitpack.io") }
	}
}


dependencies {
    implementation("com.github.ihsanbal:LoggingInterceptor:4.0.0") {
            exclude(group = "org.json", module = "json")
        }
}

```

## Batching and custom sinks (fork feature)

This fork adds a `sink(...)` API so you can batch a whole request/response block before logging
to avoid interleaving in Logcat. Example (using the bundled `BatchingSink`, now public):

```kotlin
val sink = BatchingSink(LogSink { type, tag, message ->
    // Logcat truncates ~4k per line; forward to your own chunker if needed
    Log.println(type, tag, message)
})

val client = OkHttpClient.Builder()
    .addInterceptor(
        LoggingInterceptor.Builder()
            .setLevel(Level.BODY)
            .log(Log.DEBUG)
            .sink(sink)
            .build()
    )
    .build()

If you need chunking/queuing (e.g., Logcat 4k limit), wrap the `LogSink` to your own queue before passing to `BatchingSink`, similar to the sample above.
```

If you want the forked artifact via JitPack:

```kotlin
allprojects {
    repositories { maven { setUrl("https://jitpack.io") } }
}

dependencies {
    implementation("com.github.rtsketo:LoggingInterceptor:3.1.0rt6") {
        exclude(group = "org.json", module = "json")
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
    <version>4.0.0</version>
</dependency>
```


Logger & Mock Support
---------------------
```kotlin
LoggingInterceptor.Builder()
    //Add logger to print log as plain text
    .logger(object : Logger {
          override fun log(level: Int, tag: String?, msg: String?) {
              Log.e("$tag - $level", "$msg")
          }
      })
      //Enable mock for develop app with mock data
      .enableMock(BuildConfig.MOCK, 1000L, object : BufferListener {
          override fun getJsonResponse(request: Request?): String? {
              val segment = request?.url?.pathSegments?.getOrNull(0)
              return mAssetManager.open(String.format("mock/%s.json", segment)).source().buffer().readUtf8()
          }
      })
```	

Level
--------

```kotlin
setLevel(Level.BASIC)
	      .NONE // No logs
	      .BASIC // Logging url,method,headers and body.
	      .HEADERS // Logging headers
	      .BODY // Logging body
```	

Platform - [Platform](https://github.com/square/okhttp/blob/master/okhttp/src/main/java/okhttp3/internal/platform/Platform.java)
--------

```kotlin
log(Platform.WARN) // setting log type
```

Tag
--------

```kotlin
tag("LoggingI") // Request & response each log tag
request("request") // Request log tag
response("response") // Response log tag

```
	
Header - [Recipes](https://github.com/square/okhttp/wiki/Recipes)
--------

```kotlin
addHeader("token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9 ") // Adding to request
```

Notes
--------
Some tips about log at this blog post: [“The way to get faster on development.”](https://medium.com/@ihsanbal/the-way-to-get-faster-on-development-9d7b23ef8c10)

Also use the filter & configure logcat header for a better result

<p align="left">
    <img src="https://github.com/ihsanbal/LoggingInterceptor/blob/master/images/screen_shot_5.png" width="280" height="155"/>
    <img src="https://github.com/ihsanbal/LoggingInterceptor/blob/master/images/screen_shot_4.png" width="280" height="155"/>
</p>
