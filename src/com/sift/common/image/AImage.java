package com.sift.common.image;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;

public abstract class AImage<T extends ARegionOfInterest> {

	protected final List<T> mRegionOfInterest;
	protected final Mat mImage;
	
	public AImage(Mat iImage) {
		mRegionOfInterest = new ArrayList<T>();
		mImage = iImage;
	}
	
	public List<T> getRegionOfInterest() {
		return mRegionOfInterest;
	}
	
	public Mat getImage() {
		return mImage;
	}
	
	public abstract void displayImage();
	
}
