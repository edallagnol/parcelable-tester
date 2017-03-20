package com.edallagnol.parcelabletester;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.edallagnol.parcelabletester.annotation.SkipParcelableTest;
import com.edallagnol.parcelabletester.utils.DeepComparator;
import com.edallagnol.parcelabletester.utils.ObjectFiller;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;

public class ParcelableTester {
	private static final String TAG = "ParcelableTester";
	private final List<Class<? extends Parcelable>> parcelables = new ArrayList<>();
	private final Context context;

	public static class ParcelableException extends Exception {
		public ParcelableException(String message) {
			super(message);
		}

		public ParcelableException(String message, Throwable cause) {
			super(message, cause);
		}

		public ParcelableException(Throwable cause) {
			super(cause);
		}
	}

	public ParcelableTester(Context context) {
		this.context = context;
	}

	@SuppressWarnings("WeakerAccess")
	public void testAllInAppPackage(String pkg) throws Exception {
		setUp(pkg);
		runTest();
	}

	@SuppressWarnings("WeakerAccess")
	public void testAllInAppPackage() throws Exception {
		testAllInAppPackage(context.getPackageName());
	}

	@SuppressWarnings("unchecked")
	private void setUp(String pkg) throws Exception {
		// procura todas as classes parcelable no classpath
		DexFile df = new DexFile(context.getPackageCodePath());
		Enumeration<String> entries = df.entries();
		while (entries.hasMoreElements()) {
			String className = entries.nextElement();
			if (!className.startsWith(pkg)) {
				continue;
			}
			try {
				Class<?> c = Class.forName(className);
				if (Parcelable.class.isAssignableFrom(c)) {
					parcelables.add((Class<? extends Parcelable>) c);
				}
			} catch (Exception e) {
				Log.d("ParcelableTester", e.getMessage());
			}
		}
		df.close();

		if (parcelables.isEmpty()) {
			throw new RuntimeException("No parcelables found in package: " + pkg);
		}
	}

	private void runTest() throws ParcelableException {
		for (Class<? extends Parcelable> c : parcelables) {
			try {
				testClass(c);
			} catch (Exception e) {
				throw new ParcelableException("Error testing class: " + c.getName(), e);
			}
		}
	}

	private void testClass(Class<? extends Parcelable> c) throws ParcelableException {
		if (c.isAnnotationPresent(SkipParcelableTest.class)) {
			Log.i(TAG, "Skipped " + c.getName());
			return;
		}

		Log.i(TAG, c.getName());

		DeepComparator comparator = new DeepComparator();

		ObjectFiller objectFiller = new ObjectFiller();

		Parcel p = Parcel.obtain();

		for (ObjectFiller.MODE mode : ObjectFiller.MODE.values()) {
			objectFiller.setMode(mode);

			// create object with random values
			Parcelable obj = objectFiller.createAndFill(c);

			// rewind
			p.setDataPosition(0);

			p.writeParcelable(obj, 0);

			// rewind
			p.setDataPosition(0);

			Parcelable obj2 = p.readParcelable(obj.getClass().getClassLoader());
			//compara todos os atributos recursivamente
			comparator.deepCompare(obj, obj2);

		}

		p.recycle();
	}
}
