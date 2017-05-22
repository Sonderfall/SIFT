package com.sift.common.image;

import java.util.ArrayList;
import java.util.List;

public abstract class ARegionOfInterest {
	
	protected int mRow;
	protected int mCol;
	protected int mLevel;
	protected double mScale;
	protected double mOrientation;
	protected double mMagnitude;
	protected final List<Double> mVectorFeatures;
	
	public ARegionOfInterest() {
		mVectorFeatures = new ArrayList<Double>();
	}
	
	public ARegionOfInterest(int iRow, int iCol) {
		mRow = iRow;
		mCol = iCol;
		mVectorFeatures = new ArrayList<Double>();
	}

	public ARegionOfInterest(int iRow, int iCol, double iScale) {
		mRow = iRow;
		mCol = iCol;
		mScale = iScale;
		mVectorFeatures = new ArrayList<Double>();
	}
	
	public ARegionOfInterest(int iRow, int iCol, double iScale, int iLevel) {
		mRow = iRow;
		mCol = iCol;
		mScale = iScale;
		mLevel = iLevel;
		mVectorFeatures = new ArrayList<Double>();
	}
	
	public ARegionOfInterest(int iRow, int iCol, double iScale, double iOrientation, double iMagnitude) {
		mRow = iRow;
		mCol = iCol;
		mScale = iScale;
		mOrientation = iOrientation;
		mMagnitude = iMagnitude;
		mVectorFeatures = new ArrayList<Double>();
	}
	
	public int level() {
		return mLevel;
	}
	
	public int row() {
		return mRow;
	}

	public int col() {
		return mCol;
	}

	public double scale() {
		return mScale;
	}

	public double orientation() {
		return mOrientation;
	}

	public void setOrientation(double iOrientation) {
		this.mOrientation = iOrientation;
	}

	public double magnitude() {
		return mMagnitude;
	}

	public void setMagnitude(double iMagnitude) {
		this.mMagnitude = iMagnitude;
	}
	
	public List<Double> vectorFeatures() {
		return mVectorFeatures;
	}
}
