package com.edallagnol.parcelabletester;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.edallagnol.parcelabletester.ParcelableTester;

import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class Test {
	@org.junit.Test
	public void test() throws Exception {
		new ParcelableTester(InstrumentationRegistry.getTargetContext())
				.testAllInAppPackage();
	}
}
