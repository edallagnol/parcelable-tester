package com.edallagnol.parcelabletester.utils;

import android.os.Parcel;
import android.util.Log;

import com.edallagnol.parcelabletester.ParcelableTester.ParcelableException;
import com.edallagnol.parcelabletester.annotation.SkipParcelableTest;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class ObjectFiller {
	private static final String TAG = "ObjectFiller";
	private final Random random;
	private final Set<Class<?>> cyclicValidation;
	private MODE mode;

	public enum MODE {
		RANDOM, FIXED1, FIXED2
	}

	public void setMode(MODE mode) {
		this.mode = mode;
	}

	public ObjectFiller() {
		this.random = new Random();
		this.cyclicValidation = new LinkedHashSet<>();
		mode = MODE.RANDOM;
	}

	private int compareConstructors(Constructor<?> a, Constructor<?> b) {
		if (Modifier.isPublic(a.getModifiers()) != Modifier.isPublic(b.getModifiers())) {
			return Modifier.isPublic(a.getModifiers()) ? -1 : 1;
		}

		List<Constructor<?>> consts = Arrays.asList(a.getDeclaringClass().getConstructors());
		if (consts.contains(a) != consts.contains(b)) {
			return consts.contains(a) ? -1 : 1;
		}

		return a.getParameterTypes().length - b.getParameterTypes().length;
	}

	public <T> T createAndFill(Class<T> clss) throws ParcelableException {
		if (cyclicValidation.contains(clss)) {
			throw new ParcelableException("Error creating object: "
					+ clss.getName() + ", ciclic reference: " + cyclicValidation.toString());
		}
		T instance = null;

		cyclicValidation.add(clss);

		try {
			//noinspection TryWithIdenticalCatches
			try {
				instance = clss.newInstance();
			} catch (InstantiationException e) {
				Log.d(TAG, e.getMessage());
			} catch (IllegalAccessException e) {
				Log.d(TAG, e.getMessage());
			}
			// procura outros constructors válidos
			if (instance == null) {
				try {
					Exception first = null;

					// primeiro os publicos e vazios
					Constructor<?>[] constructors = clss.getDeclaredConstructors();
					Arrays.sort(constructors, this::compareConstructors);
					for (Constructor<?> c : constructors) {
						Class<?>[] paramTypes = c.getParameterTypes();
						if (Arrays.asList(paramTypes).contains(Parcel.class)) {
							continue;
						}

						try {
							Object[] args = new Object[paramTypes.length];
							for (int i = 0; i < paramTypes.length; i++) {
								args[i] = getValueForClass(paramTypes[i]);
							}
							c.setAccessible(true);
							//noinspection unchecked
							instance = (T) c.newInstance(args);
							break;
						} catch (InstantiationException e) {
							Log.d(TAG, e.getMessage());
						} catch (Exception e) {
							first = first == null ? e : first;
						}
					}
					if (instance == null && first != null) {
						throw first;
					}
				} catch (Exception t) {
					throw new ParcelableException("Error instantiating class constructor: "
							+ clss.getName(), t);
				}
			}

			if (instance == null) {
				throw new ParcelableException("No valid constructor found for: " + clss.getName());
			}

			try {
				// #TODO superclass fields
				for (Field field : clss.getDeclaredFields()) {
					if ((field.getModifiers() & (Modifier.STATIC | Modifier.FINAL
							| Modifier.TRANSIENT)) != 0) {
						continue;
					}
					if (field.isAnnotationPresent(SkipParcelableTest.class)) {
						continue;
					}
					field.setAccessible(true);
					Object value = getValueForClass(field.getType());

					try {
						Method setter = clss.getMethod("set"
								+ field.getName().substring(0, 1).toUpperCase()
								+ field.getName().substring(1), field.getType());
						setter.invoke(instance, value);
					} catch (NoSuchMethodException nm) {
						field.set(instance, value);
					}
				}
			} catch (Throwable t) {
				throw new RuntimeException("Erro ao preencher campos da classe: " + clss.getName(), t);
			}

		} finally {
			cyclicValidation.remove(clss);
		}

		return instance;
	}

	private Object getValueForClass(Class<?> type) throws ParcelableException {
		switch (mode) {
			case RANDOM:
				return getRandomValueForClass(type);
			case FIXED1:
				return getFixed1ValueForClass(type);
			case FIXED2:
				return getFixed2ValueForClass(type);
			default:
				throw new AssertionError();
		}
	}

	private Object getRandomValueForClass(Class<?> type) throws ParcelableException {
		if (type.isEnum()) {
			Object[] enumValues = type.getEnumConstants();
			if (enumValues.length == 0) {
				return null;
			}
			return enumValues[random.nextInt(enumValues.length)];
		} else if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
			return random.nextInt();
		} else if (type.equals(Byte.TYPE) || type.equals(Byte.class)) {
			return (byte)random.nextInt();
		} else if (type.equals(Short.TYPE) || type.equals(Short.class)) {
			return (short)random.nextInt();
		} else if (type.equals(Long.TYPE) || type.equals(Long.class)) {
			return random.nextLong();
		} else if (type.equals(Double.TYPE) || type.equals(Double.class)) {
			return random.nextDouble();
		} else if (type.equals(Float.TYPE) || type.equals(Float.class)) {
			return random.nextFloat();
		} else if (type.equals(Boolean.TYPE) || type.equals(Boolean.class)) {
			return random.nextBoolean();
		} else if (type.equals(Character.TYPE) || type.equals(Character.class)) {
			return (char)(random.nextInt(25) + 'a');
		} else if (type.equals(String.class)) {
			return UUID.randomUUID().toString();
		} else if (type.equals(BigInteger.class)) {
			return BigInteger.valueOf(random.nextInt());
		} else if (type.equals(BigDecimal.class)) {
			return BigDecimal.valueOf(random.nextDouble());
		} else if (type.equals(Date.class)) {
			return new Date(random.nextLong());
		} else if (type.equals(Timestamp.class)) {
			return new Timestamp(System.currentTimeMillis());
		} else if (type.equals(Calendar.class)) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis(random.nextLong());
			return cal;
		} else if (type.equals(ArrayList.class) || type.equals(List.class)) {
			// #TODO create elements
			return new ArrayList<>();
		} else if (type.isArray()) {
			Class<?> componentType = type.getComponentType();
			Object o = Array.newInstance(componentType, 1);
			Array.set(o, 0, getValueForClass(componentType));
			return o;
		}
		return createAndFill(type);
	}

	private Object getFixed1ValueForClass(Class<?> type) throws ParcelableException {
		if (type.isEnum()) {
			Object[] enumValues = type.getEnumConstants();
			if (enumValues.length == 0) {
				return null;
			}
			return enumValues[0];
		} else if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
			return 4937 * 4937;
		} else if (type.equals(Byte.TYPE) || type.equals(Byte.class)) {
			return (byte)4937;
		} else if (type.equals(Short.TYPE) || type.equals(Short.class)) {
			return (short)4937;
		} else if (type.equals(Long.TYPE) || type.equals(Long.class)) {
			return 4937L * 4937L * 4937L;
		} else if (type.equals(Double.TYPE) || type.equals(Double.class)) {
			return 4937.49374937;
		} else if (type.equals(Float.TYPE) || type.equals(Float.class)) {
			return 4937.4937f;
		} else if (type.equals(Boolean.TYPE) || type.equals(Boolean.class)) {
			return false;
		} else if (type.equals(Character.TYPE) || type.equals(Character.class)) {
			return 'a';
		} else if (type.equals(String.class)) {
			return "4937s";
		} else if (type.equals(BigInteger.class)) {
			return new BigInteger("49374937");
		} else if (type.equals(BigDecimal.class)) {
			return new BigDecimal("49374937.49374937");
		} else if (type.equals(Date.class)) {
			return new Date(4937L);
		} else if (type.equals(Timestamp.class)) {
			return new Timestamp(49374937L);
		} else if (type.equals(Calendar.class)) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis(493749374937L);
			return cal;
		} else if (type.equals(ArrayList.class) || type.equals(List.class)) {
			// #TODO create elements
			return new ArrayList<>();
		} else if (type.isArray()) {
			Class<?> componentType = type.getComponentType();
			Object o = Array.newInstance(componentType, 1);
			Array.set(o, 0, getValueForClass(componentType));
			return o;
		}
		return createAndFill(type);
	}

	private Object getFixed2ValueForClass(Class<?> type) throws ParcelableException {
		if (type.isEnum()) {
			Object[] enumValues = type.getEnumConstants();
			if (enumValues.length == 0) {
				return null;
			}
			return enumValues[0];
		} else if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
			return 1234 * 1234;
		} else if (type.equals(Byte.TYPE) || type.equals(Byte.class)) {
			return (byte)1234;
		} else if (type.equals(Short.TYPE) || type.equals(Short.class)) {
			return (short)1234;
		} else if (type.equals(Long.TYPE) || type.equals(Long.class)) {
			return 1234L * 1234L * 1234L;
		} else if (type.equals(Double.TYPE) || type.equals(Double.class)) {
			return 1234.12341234;
		} else if (type.equals(Float.TYPE) || type.equals(Float.class)) {
			return 1234.1234f;
		} else if (type.equals(Boolean.TYPE) || type.equals(Boolean.class)) {
			return true;
		} else if (type.equals(Character.TYPE) || type.equals(Character.class)) {
			return 'b';
		} else if (type.equals(String.class)) {
			return "1234s";
		} else if (type.equals(BigInteger.class)) {
			return new BigInteger("12341234");
		} else if (type.equals(BigDecimal.class)) {
			return new BigDecimal("12341234.12341234");
		} else if (type.equals(Date.class)) {
			return new Date(1234L);
		} else if (type.equals(Timestamp.class)) {
			return new Timestamp(12341234L);
		} else if (type.equals(Calendar.class)) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis(123412341234L);
			return cal;
		} else if (type.equals(ArrayList.class) || type.equals(List.class)) {
			// #TODO create elements
			return new ArrayList<>();
		} else if (type.isArray()) {
			Class<?> componentType = type.getComponentType();
			Object o = Array.newInstance(componentType, 1);
			Array.set(o, 0, getValueForClass(componentType));
			return o;
		}
		return createAndFill(type);
	}
}
