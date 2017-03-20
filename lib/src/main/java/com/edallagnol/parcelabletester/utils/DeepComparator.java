package com.edallagnol.parcelabletester.utils;

import com.edallagnol.parcelabletester.ParcelableTester.ParcelableException;
import com.edallagnol.parcelabletester.annotation.SkipParcelableTest;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

public class DeepComparator {
	private boolean compareTransient;

	public DeepComparator compareTransient(boolean compareTransient) {
		this.compareTransient = compareTransient;
		return this;
	}

	public void deepCompare(Object a, Object b) throws ParcelableException {
		deepCompare(null, a, b, null);
	}

	private void deepCompare(Class<?> parentClass, Object a, Object b, Field field) throws ParcelableException {
		if (a == b) {
			return;
		}
		if (field != null && field.isAnnotationPresent(SkipParcelableTest.class)) {
			return;
		}
		String fieldName = field == null ? null : field.getName();
		if (a == null) {
			throw new ParcelableException("a == null; field: " + fieldName);
		}
		if (b == null) {
			throw new ParcelableException("b == null; field: " + fieldName);
		}
		if (a.getClass() != b.getClass()) {
			throw new ParcelableException("Classes diferentes: " + a.getClass().getName()
					+ " - " + b.getClass().getName() + "; field: " + fieldName);
		}

		//#TODO use getters!

		Class<?> clss = a.getClass();
		if (clss.isPrimitive()
				|| CharSequence.class.isAssignableFrom(clss)
				|| Number.class.isAssignableFrom(clss)
				|| Character.class.isAssignableFrom(clss)
				|| Calendar.class.isAssignableFrom(clss)
				|| Date.class.isAssignableFrom(clss)
				|| Collection.class.isAssignableFrom(clss)) {
			if (!a.equals(b)) {
				throw createEqualsException(parentClass, fieldName);
			}
		} else if (clss.isArray()) {
			int length = Array.getLength(a);
			if (length != Array.getLength(b)) {
				throw createEqualsException(parentClass, fieldName);
			}
			for (int i = 0; i != length; i++) {
				try {
					deepCompare(parentClass, Array.get(a, i), Array.get(b, i), field);
				} catch (Throwable t) {
					throw new ParcelableException("Error comparing array: " + fieldName, t);
				}
			}
		} else {
			// #TODO superclass fields
			Field[] fields = clss.getDeclaredFields();
			for (Field f : fields) {
				if (Modifier.isStatic(f.getModifiers())) {
					continue;
				}
				if (!compareTransient) {
					if (Modifier.isTransient(f.getModifiers())) {
						continue;
					}
				}

				f.setAccessible(true);
				try {
					deepCompare(clss, f.get(a), f.get(b), f);
				} catch (IllegalAccessException e) {
					throw new ParcelableException(e);
				}
			}
		}
	}

	private static ParcelableException createEqualsException(Class<?> parentClass, String fieldName) {
		if (fieldName == null) {
			return new ParcelableException("Diferença na classe: " + parentClass.getName());
		}
		if (parentClass == null) {
			return new ParcelableException("Diferença no field: " + fieldName);
		}
		return new ParcelableException("Diferença no campo: " + fieldName + " classe: " + parentClass.getName());
	}
}
