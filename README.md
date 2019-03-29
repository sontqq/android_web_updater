# Android Web Updater Library
Update your android app from custom web sever

## Usage:

### Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

### Step 2. Add the dependency
```
dependencies {
	        implementation 'com.github.sontqq:android_web_updater:-SNAPSHOT'
	}
```

And in your activity or service:
```
WebUpdater updater = new WebUpdater(
            getApplicationContext(),
            "update_file.apk",
            "https://example.com/update_file.apk"
        );
updater.startUpdate();
```
### Step 3.
Edit your AndroidManifest.xml

Change "com.apppackage" with your application's package id

```
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.sontme.esp.appupdater">
<application>
        <provider
            android:name=".GenericFileProvider"
            android:authorities="${applicationId}.com.apppackage.provider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/provider_paths" />
        </provider>
</application>
</manifest>
```
That's it!
