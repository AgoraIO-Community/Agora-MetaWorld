## Get started

### Step

1. Download the SDK from [](), then copy the `*.jar` and `*.aar` to [android/app/libs](app/libs)
   directory, copy the `*.so` to [android/src/main/jniLibs/${ANDROID_ABI}](app/src/main/jniLibs)

2. Update the configuration which in local.properties.For example:
```mk
#app id
APP_ID=xxxxx
#app certificate
APP_CERTIFICATE=xxxxx
FACE_CAP_APP_ID=xxxxx
FACE_CAP_APP_KEY=xxxxx
```

3.add dependencies in your build.gradle
```groovy
    implementation(['com.squareup.okhttp3:logging-interceptor:3.9.0',
                    'com.squareup.retrofit2:retrofit:2.3.0',
                    'com.squareup.retrofit2:adapter-rxjava2:2.3.0',
                    'com.squareup.retrofit2:converter-gson:2.3.0'])
    implementation(["io.reactivex.rxjava2:rxandroid:2.0.1",
                    "io.reactivex.rxjava2:rxjava:2.1.3"])
```

4.Run the example
