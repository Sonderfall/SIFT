package com.sift.common.image;

import org.opencv.core.Mat;


public class DescriptorImage extends AImage<KeyPoint> {

	public DescriptorImage(Mat iImage) {
		super(iImage);
	}

	@Override
	public void displayImage() {
	}
}
