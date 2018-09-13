package com.edallagnol.appparcelabletester;

import android.os.Parcel;
import android.os.Parcelable;

import com.edallagnol.parcelabletester.annotation.SkipParcelableTest;

import java.math.BigDecimal;

public class Entity implements Parcelable{
	public static boolean fail;
	private final int finalInt;
	private final String finalString;
	private int anInt;
	private double aDouble;
	private String aString;
	private Boolean aBoolean;
	private BigDecimal aBigDecimal;
	@SkipParcelableTest
	public long ignore;

	public Entity(int finalInt, String finalString) {
		this.finalInt = finalInt;
		this.finalString = finalString;
		this.ignore = System.nanoTime();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.finalInt);
		dest.writeString(this.finalString);
		if (!fail) {
			dest.writeInt(this.anInt);
		}
		dest.writeDouble(this.aDouble);
		dest.writeString(this.aString);
		dest.writeValue(this.aBoolean);
		dest.writeValue(this.aBigDecimal);
	}

	protected Entity(Parcel in) {
		this.finalInt = in.readInt();
		this.finalString = in.readString();
		if (!fail) {
			this.anInt = in.readInt();
		}
		this.aDouble = in.readDouble();
		this.aString = in.readString();
		this.aBoolean = (Boolean) in.readValue(Boolean.class.getClassLoader());
		this.aBigDecimal = (BigDecimal) in.readValue(BigDecimal.class.getClassLoader());
	}

	public static final Creator<Entity> CREATOR = new Creator<Entity>() {
		@Override
		public Entity createFromParcel(Parcel source) {
			return new Entity(source);
		}

		@Override
		public Entity[] newArray(int size) {
			return new Entity[size];
		}
	};
}
