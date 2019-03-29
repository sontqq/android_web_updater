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
	        implementation 'com.github.sontqq:android_web_updater:57379ebe28'
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

That's it!
