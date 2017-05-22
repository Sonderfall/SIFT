package com.sift.common.tools;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.List;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import Jama.Matrix;

import com.sift.common.image.KeyPoint;
import com.sift.common.image.KeyPointImage;

public class Tools {
	
	public static Mat changeSizeMat(final Mat iMatImage, int iNbRow, int iNbCol) {
		Mat lNewImage = new Mat(iNbRow, iNbCol, CvType.CV_8UC1);
		try {
			Imgproc.resize(iMatImage, lNewImage, new Size(iNbRow, iNbCol));
		} catch (RuntimeException e) {
			lNewImage = null;
		}
		return lNewImage;
	}
	
	public static Mat file2Mat(String iPath) {
		return Imgcodecs.imread(iPath);
	}
	
	public static void mat2File(String iName, Mat iMatImage) {
		Imgcodecs.imwrite(iName, iMatImage);
	}
	
    public static BufferedImage mat2BufferedImage(final Mat iMatImage) {
	    int lType = BufferedImage.TYPE_BYTE_GRAY;
	    if (iMatImage.channels() > 1)
	        lType = BufferedImage.TYPE_3BYTE_BGR;
	    int lBufferSize = iMatImage.channels() * iMatImage.cols() * iMatImage.rows();
	    byte[] lBytes = new byte[lBufferSize];
	    
	    iMatImage.get(0, 0, lBytes);
	    BufferedImage lBufferedImage = new BufferedImage(iMatImage.cols(), iMatImage.rows(), lType);
	    final byte[] lTargetPixels = ((DataBufferByte) lBufferedImage.getRaster().getDataBuffer()).getData();
	    System.arraycopy(lBytes, 0, lTargetPixels, 0, lBytes.length);
	    return lBufferedImage;
    }
    
    public static void displayImage(final BufferedImage iBufferedImage) {
    	JFrame lFrame = new JFrame();
    	lFrame.getContentPane().setLayout(new FlowLayout());
    	lFrame.getContentPane().add(new JLabel(new ImageIcon(iBufferedImage)));
    	lFrame.pack();
    	lFrame.setVisible(true);
    }

    public static void displayImage(final Mat iMatImage) {
    	Tools.displayImage(Tools.mat2BufferedImage(iMatImage));
    }

    public static Mat traceTrajectoryImage(Mat iMatImage, Point iA, Point iB, Scalar iColor) {
    	Imgproc.line(iMatImage, iA, iB, iColor);
    	Imgproc.circle(iMatImage, iA, 5, iColor);
    	Imgproc.circle(iMatImage, iB, 5, iColor);
    	return iMatImage;
    }
    
	public static void displayImage(Mat iFirstImage, Mat iSecondImage, List<Pair<KeyPoint, KeyPoint>> iFeatureComparaison) {
		Mat lComparaisonImage = new Mat(Math.max(iFirstImage.rows(), iSecondImage.rows()), iFirstImage.cols() + iSecondImage.cols(), CvType.CV_8UC3);
		
		for (int i = 0; i < iFirstImage.rows(); ++i) {
			for (int j = 0; j < iFirstImage.cols(); ++j) {
				lComparaisonImage.put(i, j, iFirstImage.get(i, j));
			}
		}
		
		for (int i = 0; i < iSecondImage.rows(); ++i) {
			for (int j = iFirstImage.cols(); j < iFirstImage.cols() + iSecondImage.cols(); ++j) {
				lComparaisonImage.put(i, j, iSecondImage.get(i, j - iFirstImage.cols()));
			}
		}
		
		if (iFeatureComparaison != null) {
			for (Pair<KeyPoint, KeyPoint> lPair : iFeatureComparaison) {
				KeyPoint lFirstKeyPoint  = lPair.getFirst();
				KeyPoint lSecondKeyPoint  = lPair.getSecond();
				
				if (lFirstKeyPoint != null && lSecondKeyPoint != null) {
					double lRandRed = randInt(0, 255);
					double lRandGreen = randInt(0, 255);
					double lRandBlue = randInt(0, 255);
					int lRowA = lFirstKeyPoint.row();
					int lColA = lFirstKeyPoint.col();
					int lRowB = lSecondKeyPoint.row();
					int lColB = iFirstImage.cols() + lSecondKeyPoint.col();
					
					traceTrajectoryImage(lComparaisonImage,
										new Point(lColA, lRowA),
										new Point(lColB, lRowB),
										new Scalar(lRandRed, lRandGreen, lRandBlue));
				}
			}
		}
		
		displayImage(lComparaisonImage);
	}
    
    public static int randInt(int iLower, int iUpper) {
        return new Random().nextInt((iUpper - iLower) + 1) + iLower;
    }
    
    public static double[][] hessian(KeyPointImage iImage, int iRow, int iCol) {
    	double[][] lHessianMatrix = new double[2][2];
    	double[] lDerivative = derivative(iImage, iRow, iCol);
    	
    	lHessianMatrix[0][0] = lDerivative[0];
    	lHessianMatrix[0][1] = (iImage.get(iRow + 1, iCol + 1) - iImage.get(iRow - 1, iCol + 1) - iImage.get(iRow + 1, iCol - 1) + iImage.get(iRow - 1, iCol - 1)) / 2.;
    	lHessianMatrix[1][0] = (iImage.get(iRow + 1, iCol + 1) - iImage.get(iRow + 1, iCol - 1) - iImage.get(iRow - 1, iCol + 1) + iImage.get(iRow - 1, iCol - 1)) / 2.;
    	lHessianMatrix[1][1] = lDerivative[1];
    	
    	return lHessianMatrix;
    }
    
    public static double[][] hessian(KeyPointImage iImage, int iRow, int iCol, int iScale) {
    	double[][] lHessianMatrix = new double[3][3];
    	double[] lDerivative = derivative(iImage, iRow, iCol, iScale);
    	
    	lHessianMatrix[0][0] = lDerivative[0];
    	lHessianMatrix[0][1] = (iImage.get(iRow + 1, iCol + 1, iScale) - iImage.get(iRow - 1, iCol + 1, iScale) - iImage.get(iRow + 1, iCol - 1, iScale) + iImage.get(iRow - 1, iCol - 1, iScale)) / 2.;
    	lHessianMatrix[0][2] = (iImage.get(iRow + 1, iCol, iScale + 1) - iImage.get(iRow - 1, iCol, iScale + 1) - iImage.get(iRow + 1, iCol, iScale - 1) + iImage.get(iRow - 1, iCol, iScale - 1)) / 2.;
    	
    	lHessianMatrix[1][0] = (iImage.get(iRow + 1, iCol + 1, iScale) - iImage.get(iRow + 1, iCol - 1, iScale) - iImage.get(iRow - 1, iCol + 1, iScale) + iImage.get(iRow - 1, iCol - 1, iScale)) / 2.;
    	lHessianMatrix[1][1] = lDerivative[1];
    	lHessianMatrix[1][2] = (iImage.get(iRow, iCol + 1, iScale + 1) - iImage.get(iRow, iCol - 1, iScale + 1) - iImage.get(iRow, iCol + 1, iScale - 1) + iImage.get(iRow, iCol - 1, iScale - 1)) / 2.;
    	
    	lHessianMatrix[2][0] = (iImage.get(iRow + 1, iCol, iScale + 1) - iImage.get(iRow + 1, iCol, iScale - 1) - iImage.get(iRow - 1, iCol, iScale + 1) + iImage.get(iRow - 1, iCol, iScale - 1)) / 2.;;
    	lHessianMatrix[2][1] = (iImage.get(iRow, iCol + 1, iScale + 1) - iImage.get(iRow, iCol + 1, iScale - 1) - iImage.get(iRow, iCol - 1, iScale + 1) + iImage.get(iRow, iCol - 1, iScale - 1)) / 2.;;
    	lHessianMatrix[2][2] = lDerivative[2];
    	
    	return lHessianMatrix;
    }
    
    public static double[] derivative(KeyPointImage iImage, int iRow, int iCol, int iScale) {
    	double[] lDerivative = new double[3];
    	lDerivative[0] = (iImage.get(iRow + 1, iCol, iScale) + iImage.get(iRow - 1, iCol, iScale)) / 2.;
    	lDerivative[1] = (iImage.get(iRow, iCol + 1, iScale) + iImage.get(iRow, iCol - 1, iScale)) / 2.;
    	lDerivative[2] = (iImage.get(iRow, iCol, iScale + 1) + iImage.get(iRow, iCol, iScale - 1)) / 2.;
    	return lDerivative;
    }
    
    public static double[] derivative(KeyPointImage iImage, int iRow, int iCol) {
    	double[] lDerivative = new double[2];
    	lDerivative[0] = (iImage.get(iRow + 1, iCol) + iImage.get(iRow - 1, iCol)) / 2.;
    	lDerivative[1] = (iImage.get(iRow, iCol + 1) + iImage.get(iRow, iCol - 1)) / 2.;
    	return lDerivative;
    }
    
    public static double[][] multiplyMatrix(final double[][] iA, final double[][] iB) {
        int lARows = iA.length;
        int lAColumns = iA[0].length;
        int lBRows = iB.length;
        int lBColumns = iB[0].length;

        if (lAColumns != lBRows) {
            throw new IllegalArgumentException("A:Rows: " + lAColumns + " did not match B:Columns " + lBRows + ".");
        }

        double[][] lResult = new double[lARows][lBColumns];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                lResult[i][j] = 0.00000;
            }
        }

        for (int i = 0; i < lARows; ++i) {
            for (int j = 0; j < lBColumns; ++j) {
                for (int k = 0; k < lAColumns; ++k) {
                    lResult[i][j] += iA[i][k] * iB[k][j];
                }
            }
        }
        return lResult;
    }

    public static double[] multiplyMatrix(final double[][] iA, final double[] iB) {
        int lARows = iA.length;
        int lAColumns = iA[0].length;
        int lBRows = iB.length;

        if (lAColumns != lBRows) {
            throw new IllegalArgumentException("A:Rows: " + lAColumns + " did not match B:Columns " + lBRows + ".");
        }

        double[] lResult = new double[lARows];
        for (int i = 0; i < 2; i++) {
        	lResult[i] = 0.00000;
        }

        for (int i = 0; i < lARows; ++i) {
        	for (int k = 0; k < lAColumns; ++k) {
        		lResult[i] += iA[i][k] * iB[k];
        	}
        }
        return lResult;
    }
    
    public static double[][] invertMatrix(final double[][] iMatrix) {
    	Matrix lMat = new Matrix(iMatrix);
    	lMat.inverse();
    	return lMat.getArray();
    }
    
    public static double traceMatrix(final double[][] iMatrix) {
    	double lTrace = 0;
    	for (int i = 0; i < iMatrix.length; ++i) {
    		lTrace += iMatrix[i][i];
    	}
    	return lTrace;
    }
    
    public static double detMatrix(final double[][] iMatrix) {
    	Matrix lMat = new Matrix(iMatrix);
    	return lMat.det();
    }
    
    public static boolean isInversibleMatrix(final double[][] iMatrix) {
    	Matrix lMat = new Matrix(iMatrix);
    	return lMat.det() != 0;
    }
    
    public static void displayMatrix(double[][] iMatrix) {
    	for (int i = 0; i < iMatrix.length; ++i) {
            for (int j = 0; j < iMatrix[0].length; ++j)
                System.out.print(iMatrix[i][j] + " ");
            System.out.println();
        }
    	System.out.println();
    }
    
    public static double magnitude(final Mat iImage, int iRow, int iCol) {
    	return Math.sqrt(Math.pow(iImage.get(iRow + 1, iCol)[0] - iImage.get(iRow - 1, iCol)[0], 2) +
    					 Math.pow(iImage.get(iRow, iCol + 1)[0] - iImage.get(iRow, iCol - 1)[0], 2));
    }
    
    public static double orientation(final Mat iImage, int iRow, int iCol) {
    	return iImage.get(iRow, iCol + 1)[0] != iImage.get(iRow, iCol - 1)[0]
    				? positiveAngle(Math.toDegrees(Math.atan((iImage.get(iRow + 1, iCol)[0] - iImage.get(iRow - 1, iCol)[0])
    													   / (iImage.get(iRow, iCol + 1)[0] - iImage.get(iRow, iCol - 1)[0]))))
    			: 0;		
   }
    
    public static double gaussian(double iMean, double iStd, double iValue) {
    	return (1 / (iStd * Math.sqrt(2 * Math.PI))) * Math.exp(-Math.pow((iValue - iMean), 2) / (2 * Math.pow(iStd, 2)));
    }
    
    public static double distance(int iXa, int iYa, int iXb, int iYb) {
    	return Math.sqrt(Math.pow(iXa - iXb, 2) + Math.pow(iYa - iYb, 2));
    }
    
    public static double norm(List<Double> iVector) {
    	double lSum = 0;
    	for (Double lVal : iVector)
    		lSum += Math.pow(lVal, 2);
    	return Math.sqrt(lSum);
    }
    
    public static List<Double> normalizedVector(List<Double> iVector) {
    	double lNorm = norm(iVector);
    	for (int i = 0; i < iVector.size(); ++i)
    		iVector.set(i, iVector.get(i) / lNorm);
    		
    	return iVector;
    }
    
    public static double upTreshold(double iValue, double iTreshold) {
    	return iValue <= iTreshold ? iValue : iTreshold;
    }
    
    public static double lowTreshold(double iValue, double iTreshold) {
    	return iValue >= iTreshold ? iValue : iTreshold;
    }
    
    public static double treshold(double iValue, double iLowTreshold, double iUpTreshold) {
    	return lowTreshold(upTreshold(iValue, iUpTreshold), iLowTreshold);
    }
    
    public static List<Double> treshold(List<Double> iVector, double iLowTreshold, double iUpTreshold) {
    	for (int i = 0; i < iVector.size(); ++i)
    		iVector.set(i, treshold(iVector.get(i), iLowTreshold, iUpTreshold));
    	return iVector;
    }
    
    public static double positiveAngle(double iAngle) {
    	return (360 + iAngle) % 360; 
    }
}