package com.sift.comparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sift.common.image.KeyPoint;
import com.sift.common.tools.Pair;

public class Comparator {

	private final List<KeyPoint> mFirstImageKeyPoints;
	private final List<KeyPoint> mSecondImageKeyPoints;
	
	public Comparator(List<KeyPoint> iFirstImageKeyPoint, List<KeyPoint> iSecondImageKeyPoint) {
		mFirstImageKeyPoints = iFirstImageKeyPoint;
		mSecondImageKeyPoints = iSecondImageKeyPoint;
	}
	
	public List<Pair<KeyPoint, KeyPoint>> processSIFTComparaison() {
		List<Pair<KeyPoint, KeyPoint>> lComparaison = new ArrayList<Pair<KeyPoint, KeyPoint>>();
		Map<KeyPoint, List<Pair<KeyPoint, Double>>> lMapComparaison = new HashMap<KeyPoint, List<Pair<KeyPoint, Double>>>();
		double lTreshold = 0.25;
		
		for (KeyPoint lFirstKeyPoint : mFirstImageKeyPoints) {
			KeyPoint lBestKeyPoint = null;
			double lShortestDistance = Double.MAX_VALUE;
			for (KeyPoint lSecondKeyPoint : mSecondImageKeyPoints) {
				double lDistance = distanceKeyPoints(lFirstKeyPoint, lSecondKeyPoint);
				if (lDistance < lShortestDistance && lDistance <= lTreshold) {
					lShortestDistance = lDistance;
					lBestKeyPoint = lSecondKeyPoint;
				}
			}
			
			if (lMapComparaison.containsKey(lBestKeyPoint)) {
				List<Pair<KeyPoint, Double>> lCorrespondances = lMapComparaison.get(lBestKeyPoint);
				lCorrespondances.add(new Pair<KeyPoint, Double>(lFirstKeyPoint, lShortestDistance));
				lMapComparaison.remove(lBestKeyPoint);
				lMapComparaison.put(lBestKeyPoint, lCorrespondances);
			} else {
				List<Pair<KeyPoint, Double>> lNewCorrespondance = new ArrayList<Pair<KeyPoint, Double>>();
				lNewCorrespondance.add(new Pair<KeyPoint, Double>(lFirstKeyPoint, lShortestDistance));
				lMapComparaison.put(lBestKeyPoint, lNewCorrespondance);
			}
		}
		
		for (Entry<KeyPoint, List<Pair<KeyPoint, Double>>> lKeySet : lMapComparaison.entrySet()) {
			KeyPoint lBestKeyPoint = null;
			double lBestScore = Double.MAX_VALUE;
			for (Pair<KeyPoint, Double> lPair : lKeySet.getValue()) {
				if (lPair.getSecond() < lBestScore) {
					lBestScore = lPair.getSecond();
					lBestKeyPoint = lPair.getFirst();
				}
			}
			lComparaison.add(new Pair<KeyPoint, KeyPoint>(lBestKeyPoint, lKeySet.getKey()));
		}
		
		return lComparaison;
	}

	private double distanceKeyPoints(KeyPoint iFirstKeyPoint, KeyPoint iSecondKeyPoint) {
		double lDistance = 0;
		for (int i = 0; i < 128; ++i) {
			double lFirstKeyHistvValue = iFirstKeyPoint.vectorFeatures().get(i);
			double lSecondKeyHistvValue = iSecondKeyPoint.vectorFeatures().get(i);
			lDistance += Math.pow(lFirstKeyHistvValue - lSecondKeyHistvValue, 2);
		}
//		lDistance += Math.pow(iFirstKeyPoint.magnitude() - iSecondKeyPoint.magnitude(), 2);
		return Math.sqrt(lDistance);
	}
}