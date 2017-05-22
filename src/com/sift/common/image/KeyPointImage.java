package com.sift.common.image;

import org.opencv.core.Mat;

import com.sift.common.tools.Tools;


public class KeyPointImage extends AImage<KeyPoint> {

	private int mScale;
	private final double[][][] mDImage;

	public KeyPointImage(int iScale, Mat... iImages) {
		super(iImages[1]);
		mScale = iScale;
		mDImage = new double[iImages[0].rows()][iImages[0].cols()][3];
		
		for (int lScale = 0; lScale < iImages.length; ++lScale) {
			Mat lImage = iImages[lScale];
			for (int lRow = 0; lRow < iImages[0].rows(); ++lRow) {
				for (int lCol = 0; lCol < iImages[0].cols(); ++lCol) {
					mDImage[lRow][lCol][lScale] = lImage.get(lRow, lCol)[0];
				}
			}
		}
	}
	
	public KeyPointImage(double[][][] iDImage, Mat iImage, int iScale) {
		super(iImage);
		mDImage = iDImage;
		mScale = iScale;
	}
	
	@Override
	public KeyPointImage clone() {
		return new KeyPointImage(mDImage, mImage, mScale);
	}
	
	@Override
	public void displayImage() {
		for (KeyPoint lKeyPoint : mRegionOfInterest) {
			mImage.put(lKeyPoint.row(), lKeyPoint.col(), new double[] {255, 0, 0});
		}
		Tools.displayImage(mImage);
	}
	
	public double get(int iRow, int iCol, int iScale) {
		if (iScale == mScale - 1)
			return mDImage[iRow][iCol][0];
		if (iScale == mScale)
			return mDImage[iRow][iCol][1];
		if (iScale == mScale + 1)
			return mDImage[iRow][iCol][2];
		return -1.;
	}
	
	public double get(int iRow, int iCol) {
		return mDImage[iRow][iCol][1];
	}
}