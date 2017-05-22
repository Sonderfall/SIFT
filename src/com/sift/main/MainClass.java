package com.sift.main;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import com.sift.common.image.KeyPoint;
import com.sift.common.tools.Pair;
import com.sift.common.tools.Tools;
import com.sift.comparator.Comparator;
import com.sift.extractor.Extractor;

public class MainClass {

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		Mat lFirstImage = Tools.file2Mat("resources/image1.png");
		Mat lSecondImage = Tools.file2Mat("resources/image3.png");
		
		Extractor lFirstExtractor = new Extractor(lFirstImage);
		List<KeyPoint> lFirstImageFeatures = lFirstExtractor.processSIFTExtraction();
		
		Extractor lSecondExtractor = new Extractor(lSecondImage);
		List<KeyPoint> lSecondImageFeatures = lSecondExtractor.processSIFTExtraction();
		
		Comparator lComparator = new Comparator(lFirstImageFeatures, lSecondImageFeatures);
		List<Pair<KeyPoint, KeyPoint>> lFeatureComparaison = lComparator.processSIFTComparaison();
		
		Tools.displayImage(lFirstImage, lSecondImage, lFeatureComparaison);
	}
}