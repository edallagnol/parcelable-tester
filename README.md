# parcelable-tester


### Usage:

```java
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import org.junit.runner.RunWith;

import com.edallagnol.parcelabletester.ParcelableTester;

@RunWith(AndroidJUnit4.class)
public class ParcelableTest {

	@org.junit.Test
	public void test() throws Exception {
		new ParcelableTester(InstrumentationRegistry.getTargetContext())
				.testAllInAppPackage();
	}
	
}
```

### Gradle:

Project build.gradle:

<pre>
allprojects {
    repositories {
        ...
        // com.edallagnol
        maven { url "https://mymavenrepo.com/repo/Ghd1bN1WIPA0LBBLKxW8/" }
    }
}
</pre>

Module build.gradle:

<pre>
dependencies {
	...
	androidTestCompile group: 'com.edallagnol', name: 'parcelable-tester', version: '0.4'
}
</pre>
