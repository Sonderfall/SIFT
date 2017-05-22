package com.sift.common.image;

public class Descriptor extends ARegionOfInterest {
	
	public Descriptor() {}
	
	public Descriptor(int iRow, int iCol) {
		super(iRow, iCol);
	}
	
	public Descriptor(int iRow, int iCol, int iScale) {
		super(iRow, iCol, iScale);
	}
}
