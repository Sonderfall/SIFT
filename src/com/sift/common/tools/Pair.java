package com.sift.common.tools;

public class Pair<T1, T2> {

	private T1 mFirst;
	private T2 mSecond;
	
	public Pair() {
		mFirst = null;
		mSecond = null;
	}
	
	public Pair(T1 iFirst, T2 iSecond) {
		mFirst = iFirst;
		mSecond = iSecond;
	}
	
	public T1 getFirst() {
		return mFirst;
	}
	
	public T2 getSecond() {
		return mSecond;
	}
	
	public void setFirst(T1 iFirst) {
		mFirst = iFirst;
	}
	
	public void setSecond(T2 iSecond) {
		mSecond = iSecond;
	}
	
	@Override
	public boolean equals(Object obj) {
		@SuppressWarnings("unchecked")
		Pair<T1, T2> lObj = (Pair<T1, T2>) obj;
		if (lObj == null) {
			return false;
		}
		if (mFirst == null && lObj.getFirst() != null) {
			return false;
		}
		if (mSecond == null && lObj.getSecond() != null) {
			return false;
		}
		
		if (mFirst == null && mSecond == null) {
			return true;
		}
		
		return mFirst.equals(lObj.getFirst()) && mSecond.equals(lObj.getSecond());
	}

	public int hashCode() {
		if (mFirst == null && mSecond == null) {
			return 0;
		}
		
		if (mFirst == null) {
			return mSecond.hashCode();
		}
		if (mSecond == null) {
			return mFirst.hashCode();
		}
		
		return mFirst.hashCode() + mSecond.hashCode();
	}
	
	public String toString() {
		return mFirst.toString() + ", " + mSecond.toString();
	}
}
