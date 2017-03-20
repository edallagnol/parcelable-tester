package com.edallagnol.appparcelabletester;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.edallagnol.parcelabletester.ParcelableTester;
import com.edallagnol.parcelabletester.ParcelableTester.ParcelableException;

import org.junit.runner.RunWith;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ParcelableTest {
	@org.junit.Test
	public void test() throws Exception {
		Entity.fail = false;
		new ParcelableTester(InstrumentationRegistry.getTargetContext())
				.testAllInAppPackage();
	}

	@org.junit.Test(expected = ParcelableException.class)
	public void testFail() throws Exception {
		Entity.fail = true;
		new ParcelableTester(InstrumentationRegistry.getTargetContext())
				.testAllInAppPackage();
	}
}
