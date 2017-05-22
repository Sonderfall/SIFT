package com.sift.common.image;


public class KeyPoint extends ARegionOfInterest {

	public KeyPoint(int iRow, int iCol) {
		super(iRow, iCol);
	}
	
	public KeyPoint(int iRow, int iCol, double iScale) {
		super(iRow, iCol, iScale);
	}
	
	public KeyPoint(int iRow, int iCol, double iScale, int iLevel) {
		super(iRow, iCol, iScale, iLevel);
	}
	
	public KeyPoint(int iRow, int iCol, double iScale, double iOrientation, double iMagnitude) {
		super(iRow, iCol, iScale, iOrientation, iMagnitude);
	}
}
