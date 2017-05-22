package com.sift.extractor;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;

import com.sift.common.image.KeyPoint;
import com.sift.common.image.KeyPointImage;
import com.sift.common.tools.Filter;
import com.sift.common.tools.Tools;

public class Extractor {

	private final Mat mImage;
	
	private final int mNbOctave;
	private final int mNbScale;
	private final double mBlurScale;
	private final Mat[][] mLoG;
	private final Mat[][] mDoGApprox;
	private final KeyPointImage[][] mAllKeyPointImages;
	private final KeyPointImage[][] mOptimaKeyPointImages;
	
	public Extractor(final Mat iImage) {
		mNbOctave = 1;
		mNbScale = 5;
		mBlurScale = 1.6;
//		mImage = Filter.grayscale(Tools.changeSizeMat(iImage, iImage.cols() * 2, iImage.rows() * 2));
		mImage = Filter.grayscale(iImage);
		mLoG = new Mat[mNbOctave][mNbScale];
		mDoGApprox = new Mat[mNbOctave][mNbScale - 1];
		mAllKeyPointImages = new KeyPointImage[mNbOctave][mNbScale - 3];
		mOptimaKeyPointImages = new KeyPointImage[mNbOctave][mNbScale - 3];
	}
	
	public List<KeyPoint> processSIFTExtraction() {
		scaleSpace();
		doGApproximation();
		findKeyPoints();
		maximaKeyPoints();
		orientationAssigment();
		generateFeatures();
		
		List<KeyPoint> lKeyPoints = new ArrayList<KeyPoint>();
		for (int lOctave = 0; lOctave < mNbOctave; ++lOctave) {
			for (int lScale = 0; lScale < mNbScale - 3; ++lScale) {
				KeyPointImage lKeyPointImage = mOptimaKeyPointImages[lOctave][lScale];
				for (KeyPoint lKeyPoint : lKeyPointImage.getRegionOfInterest()) {
					lKeyPoints.add(lKeyPoint);
				}
			}
		}
		
		System.out.println(lKeyPoints.size() + " total key points.");
		System.out.println();
		
		return lKeyPoints;
	}

	private void scaleSpace() {
		double lK = Math.pow(2, 1. / mNbScale);
		int lPow = 0;
		for (int lOctave = 0; lOctave < mNbOctave; ++lOctave) {
			Mat lOctaveImage = mImage;
			for (int lOctaveIterator = 0; lOctaveIterator < lOctave; ++lOctaveIterator) {
				lOctaveImage = Tools.changeSizeMat(lOctaveImage, lOctaveImage.cols() / 2, lOctaveImage.rows() / 2);
			}
			for (int lScale = 0; lScale < mNbScale; ++lScale) {
				double lValue = Math.pow(lK, lPow++) * mBlurScale;
				Mat lBlurImage = Filter.gaussian(lOctaveImage, lValue);
				mLoG[lOctave][lScale] = lBlurImage;
//				Tools.displayImage(lBlurImage);
			}
		}
	}

	private void doGApproximation() {
		for (int lOctave = 0; lOctave < mNbOctave; ++lOctave) {
			for (int lScale = 0; lScale < mNbScale - 1; ++lScale) {
				mDoGApprox[lOctave][lScale] = Filter.diffGaussian(mLoG[lOctave][lScale + 1], mLoG[lOctave][lScale]);
//				Tools.displayImage(mDoGApprox[lOctave][lScale]);
			}
		}
	}

	private void findKeyPoints() {
		int lTotalKeyPoints = 0;
		for (int lOctave = 0; lOctave < mNbOctave; ++lOctave) {
			for (int lScale = 1; lScale < mNbScale - 2; ++lScale) {
				Mat[] lScales = new Mat[3];
				lScales[0] = mDoGApprox[lOctave][lScale - 1];
				lScales[1] = mDoGApprox[lOctave][lScale];
				lScales[2] = mDoGApprox[lOctave][lScale + 1];
				KeyPointImage lKeyPtImage = new KeyPointImage(lScale,
															  mDoGApprox[lOctave][lScale - 1],
															  mDoGApprox[lOctave][lScale],
															  mDoGApprox[lOctave][lScale + 1]);
				mAllKeyPointImages[lOctave][lScale - 1] = lKeyPtImage;
				for (int lRow = 1; lRow < lScales[0].rows() - 1; ++lRow) {
					for (int lCol = 1; lCol < lScales[0].cols() - 1; ++lCol) {
						int lFewer = 0;
						int lBigger = 0;
						double lRefValue = lScales[1].get(lRow, lCol)[0];
						boolean lOptima = false;
						boolean lBreak = false;
						for (int lScaleLevel = 0; lScaleLevel < 3; ++lScaleLevel) {
							for (int lConvolRow = lRow - 1; lConvolRow <= lRow + 1; ++lConvolRow) {
								for (int lConvolCol = lCol - 1; lConvolCol <= lCol + 1; ++lConvolCol) {
									if (!(lConvolRow == lRow && lConvolCol == lCol && lScaleLevel == 1)) {
										double lValue = lScales[lScaleLevel].get(lConvolRow, lConvolCol)[0];
										lFewer = lValue < lRefValue ? lFewer + 1 : lFewer;
										lBigger = lValue > lRefValue ? lBigger + 1 : lBigger;
										if (lFewer != 0 && lBigger != 0) {
											lBreak = true;
										}
										
										if (lFewer == 26 || lBigger == 26) {
											lOptima = true;
											lBreak = true;
										}
									}
									if (lBreak) break;
								}
								if (lBreak) break;
							}
							if (lBreak) break;
						}
						
						if (lOptima) {
							lKeyPtImage.getRegionOfInterest().add(new KeyPoint(lRow, lCol, mBlurScale * lScale, lScale));
							++lTotalKeyPoints;
						}
					}
				}
//				lKeyPtImage.displayImage();
			}
		}
		
		System.out.println(lTotalKeyPoints + " created key points.");
	}

	private void maximaKeyPoints() {
		int lTotalKeyPoints = 0;
		for (int lOctave = 0; lOctave < mNbOctave; ++lOctave) {
			for (int lScale = 0; lScale < mNbScale - 3; ++lScale) {
				KeyPointImage lKeyPointImage = mAllKeyPointImages[lOctave][lScale];
				KeyPointImage lOptimaKeyPointImage = lKeyPointImage.clone();
				mOptimaKeyPointImages[lOctave][lScale] = lOptimaKeyPointImage;
				for (KeyPoint lKeyPoint : lKeyPointImage.getRegionOfInterest()) {
					int lIterator = 0;
					int lRefRow = lKeyPoint.row();
					int lRefCol = lKeyPoint.col();
					int lRefLevel = lKeyPoint.level();
					double lRefScale = lKeyPoint.scale();
					
					while (lIterator < 25) {
						if (lRefCol >= lOptimaKeyPointImage.getImage().cols() - 1
							|| lRefCol <= 0
							|| lRefRow >= lOptimaKeyPointImage.getImage().rows() - 1
							|| lRefRow <= 0) {
							break;
						}
						
						double[][] lHessianMatrix3D = Tools.hessian(lKeyPointImage, lRefRow, lRefCol, lRefLevel);

						if (Tools.isInversibleMatrix(lHessianMatrix3D)) {
							double[][] lHessianInverseMatrix = Tools.invertMatrix(lHessianMatrix3D);
							double[][] lNegativeIdentityMatrix = new double[][] {{-1, 0, 0}, {0, -1, 0}, {0, 0, -1}};
							double[][] lHessianNegativeMatrix = Tools.multiplyMatrix(lHessianInverseMatrix, lNegativeIdentityMatrix);
							double[] lDerivative = Tools.derivative(lKeyPointImage, lRefRow, lRefCol, lRefLevel);
							double[] lDelta = Tools.multiplyMatrix(lHessianNegativeMatrix, lDerivative);
							
							double lDeltaRow = lDelta[0];
							double lDeltaCol = lDelta[1];
							double lDeltaScale = lDelta[2];
					
							if (lRefRow + lDeltaRow < 0 || lRefCol + lDeltaCol < 0 || lRefScale + lDeltaScale < 0)
								break;

							lRefRow += lDeltaRow;
							lRefCol += lDeltaCol;
							lRefScale += lDeltaScale;
							
							if (lDeltaRow < 0.5 && lDeltaCol < 0.5 && lDeltaScale < 0.5) {
								break;
							}
						}
						++lIterator;
					}
					
					if (lRefCol >= lOptimaKeyPointImage.getImage().cols() - 1
						|| lRefCol <= 0
						|| lRefRow >= lOptimaKeyPointImage.getImage().rows() - 1
						|| lRefRow <= 0)
						continue;
					
					int lRTreshold = 10;
					double lContrastTreshold = 0.03;
					double[][] lHessianMatrix2D = Tools.hessian(lKeyPointImage, lRefRow, lRefCol);
					double lTrace = Tools.traceMatrix(lHessianMatrix2D);
					double lDet = Tools.detMatrix(lHessianMatrix2D);
					double lCurvatureRatio = Math.pow(lTrace, 2) / lDet;
					double lCurvatureTreshold = Math.pow(lRTreshold + 1, 2) / lRTreshold;
					double lContrast = Tools.magnitude(lOptimaKeyPointImage.getImage(), lRefRow, lRefCol);
					
					if (lCurvatureRatio >= lCurvatureTreshold)
						continue;
					
					if (lContrast < lContrastTreshold)
						continue;
					
					
//					System.out.println("OLD = " + lKeyPoint.row() + " " + lKeyPoint.col() + " ----- NEW = " + lRefRow + " " + lRefCol);
					lOptimaKeyPointImage.getRegionOfInterest().add(new KeyPoint(lRefRow, lRefCol, lRefScale, lRefLevel));
					++lTotalKeyPoints;
				}
//				lOptimaKeyPointImage.displayImage();
			}
		}
		
		System.out.println(lTotalKeyPoints + " maxima key points.");
	}
	
	private void orientationAssigment() {
		for (int lOctave = 0; lOctave < mNbOctave; ++lOctave) {
			for (int lScale = 0; lScale < mNbScale - 3; ++lScale) {
				KeyPointImage lOptimaKeyPointImage = mOptimaKeyPointImages[lOctave][lScale];
				Mat lLoGImage = mLoG[lOctave][lScale];
				List<KeyPoint> lNewKeyPoints = new ArrayList<KeyPoint>();
				for (KeyPoint lKeyPoint : lOptimaKeyPointImage.getRegionOfInterest()) {
					int lSigma = (int)(1.5 * lKeyPoint.scale());
					int lMarge = lSigma;
					int lHighestBin = 0;
					int lSecondHighestBin = 0;
					double lMaxMagnitude = 0;
					double lSecondMaxMagnitude = 0;
					List<List<Double>> lGradientHist = new ArrayList<List<Double>>();
					
					for (int lBin = 0; lBin < 36; ++lBin) {
						lGradientHist.add(new ArrayList<Double>());
					}
					
					for (int lRow = (lKeyPoint.row() - lMarge >= 1 ? lKeyPoint.row() - lMarge : 1);
						lRow <= (lKeyPoint.row() + lMarge < lOptimaKeyPointImage.getImage().rows() - 1 ? lKeyPoint.row() + lMarge : lOptimaKeyPointImage.getImage().rows() - 2);
						++lRow) {
						for (int lCol = (lKeyPoint.col() - lMarge >= 1 ? lKeyPoint.col() - lMarge : 1);
							lCol <= (lKeyPoint.col() + lMarge < lOptimaKeyPointImage.getImage().cols() - 1 ? lKeyPoint.col() + lMarge : lOptimaKeyPointImage.getImage().cols() - 2);
							++lCol) {
							double lMagnitude = Tools.magnitude(lLoGImage, lRow, lCol); 
							double lOrientation = Tools.orientation(lLoGImage, lRow, lCol);
							
							int lHistIndex = (int)(Math.round((lOrientation / 10.) - 0.5));
//							System.out.println(lOrientation + " " + lMagnitude);
							lGradientHist.get(lHistIndex < 36 ? lHistIndex : 35).add(lMagnitude * Tools.gaussian(0, lSigma, Tools.distance(lKeyPoint.row(), lKeyPoint.col(), lRow, lCol)));
						}
					}
					
					for (int lBin = 0; lBin < 36; ++lBin) {
						List<Double> lMagnitudes = lGradientHist.get(lBin);
						double lAmountMagnitude = 0;
						for (Double lMagnitude : lMagnitudes) {
							lAmountMagnitude += lMagnitude;
						}
						
						if (lAmountMagnitude > lSecondMaxMagnitude && lAmountMagnitude < lMaxMagnitude) {
							lSecondMaxMagnitude = lAmountMagnitude;
							lSecondHighestBin = lBin;
						}
						
						if (lAmountMagnitude > lMaxMagnitude) {
							
							if (lAmountMagnitude > lSecondMaxMagnitude) {
								lSecondMaxMagnitude = lMaxMagnitude;
								lSecondHighestBin = lHighestBin;
							}
							
							lMaxMagnitude = lAmountMagnitude;
							lHighestBin = lBin;
						}
					}
					
					lKeyPoint.setOrientation(lHighestBin * 10);
					lKeyPoint.setMagnitude(lMaxMagnitude);
					
					if (lSecondMaxMagnitude / lMaxMagnitude >= 0.8)
						lNewKeyPoints.add(new KeyPoint(lKeyPoint.row(), lKeyPoint.col(), lKeyPoint.scale(), lSecondHighestBin * 10, lSecondMaxMagnitude));
				}
				
				lOptimaKeyPointImage.getRegionOfInterest().addAll(lNewKeyPoints);
			}
		}
	}
	
	private void generateFeatures() {
		int lKeyPointMarge = 8;
		int lSubMarge = 4;
		for (int lOctave = 0; lOctave < mNbOctave; ++lOctave) {
			for (int lScale = 0; lScale < mNbScale - 3; ++lScale) {
				KeyPointImage lOptimaKeyPointImage = mOptimaKeyPointImages[lOctave][lScale];
				Mat lLoGImage = mLoG[lOctave][lScale];
				for (KeyPoint lKeyPoint : lOptimaKeyPointImage.getRegionOfInterest()) {
					int lSigma = (int)(1.5 * lKeyPoint.scale());
					for (int lRow = lKeyPoint.row() - lKeyPointMarge; lRow < lKeyPoint.row() + lKeyPointMarge; lRow += 4) {
						for (int lCol = lKeyPoint.col() - lKeyPointMarge; lCol < lKeyPoint.col() + lKeyPointMarge; lCol += 4) {
							Double[] lGradientHist = new Double[] {0., 0., 0., 0., 0., 0., 0., 0.};
							for (int lSubRow = (lRow > 0 ? lRow : 1);
								 lSubRow < ( lRow + lSubMarge < lOptimaKeyPointImage.getImage().rows() - 1 ? lRow + lSubMarge : lOptimaKeyPointImage.getImage().rows() - 2);
								 ++lSubRow) {
								for (int lSubCol = (lCol > 0 ? lCol : 1);
									 lSubCol < (lCol + lSubMarge < lOptimaKeyPointImage.getImage().cols() - 1 ? lCol + lSubMarge : lOptimaKeyPointImage.getImage().cols() - 2);
								 	 ++lSubCol) {
									
//									System.out.println(lSubRow + " " + lSubCol);
									
									double lMagnitude = Tools.magnitude(lLoGImage, lSubRow, lSubCol); 
									double lOrientation = Tools.positiveAngle(Tools.orientation(lLoGImage, lSubRow, lSubCol) - lKeyPoint.orientation());
//									double lOrientation = Tools.positiveAngle(lKeyPoint.orientation() - Tools.orientation(lLoGImage, lSubRow, lSubCol));
									
									int lHistIndex = (int)(Math.round((lOrientation / 45.) - 0.5));
//									System.out.println(lOrientation +" " + lHistIndex + " " + lMagnitude);
									lGradientHist[lHistIndex < 8 ? lHistIndex : 7] += lMagnitude * Tools.gaussian(0, lSigma, Tools.distance(lKeyPoint.row(), lKeyPoint.col(), lSubRow, lSubCol));
								}
							}

							for (int lBin = 0; lBin < 8; ++lBin) {
								lKeyPoint.vectorFeatures().add(lGradientHist[lBin]);
//								System.out.println(lGradientHist[lBin]);
							}
//							System.out.println();
						}
					}
					
					Tools.normalizedVector(lKeyPoint.vectorFeatures());
					Tools.treshold(lKeyPoint.vectorFeatures(), 0, 0.2);
					Tools.normalizedVector(lKeyPoint.vectorFeatures());
				}
			}
		}
	}
}