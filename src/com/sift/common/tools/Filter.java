package com.sift.common.tools;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Filter {

	public static Mat grayscale(final Mat iRefImage) {
		Mat lResult = new Mat(iRefImage.rows(), iRefImage.cols(), CvType.CV_8UC1);
		for (int i = 0; i < iRefImage.rows(); ++i) {
			for (int j = 0; j < iRefImage.cols(); ++j) {
				double lBlue = iRefImage.get(i, j)[0];
				double lGreen = iRefImage.get(i, j)[1];
				double lRed  = iRefImage.get(i, j)[2];
				double lGray = (lRed * 299 + lGreen * 587 + lBlue * 114) / 1000;
				lResult.put(i, j, lGray);
			}
		}
		Tools.displayImage(lResult);
		return lResult;
	}
	
	public static Mat gaussian(final Mat iRefImage, final double iSigma) {
		Mat lResult = new Mat(iRefImage.rows(), iRefImage.cols(), CvType.CV_8UC1);
		Imgproc.GaussianBlur(iRefImage, lResult, new Size(5, 5), iSigma);
		return lResult;
	}
	
	public static Mat diffGaussian(final Mat iFirstImage, final Mat iSecondImage) {
		Mat lResult = new Mat(iFirstImage.rows(), iFirstImage.cols(), CvType.CV_8UC1);
		for (int i = 0; i < iFirstImage.rows(); ++i) {
			for (int j = 0; j < iFirstImage.cols(); ++j) {
				lResult.put(i, j, Math.abs(iFirstImage.get(i, j)[0] - iSecondImage.get(i, j)[0]));
			}
		}
		return lResult;
	}
}