package com.trip.utils;

public class Heap {
	
	
	private DistributionBean[] A;

	public Heap(DistributionBean[] A) {
		this.A=A;
	}
	
	public DistributionBean[] getArray(){
		return A;
	}
	
	private int getParent(int i){
		int iParent=i/2;
		return iParent;
	}
	
	private int getLeftChild(int i){
		int iLeftChild=i*2;
		return iLeftChild;
	}
	
	private int getRightChild(int i){
		int iRightChild=(i*2)+1;
		return iRightChild;
	}
	
	public DistributionBean removeMax(){
		DistributionBean dbMax=A[0];
		int iLength=A.length-1;
		DistributionBean[] B=new DistributionBean[iLength];
		A[0]=A[iLength];
		System.arraycopy(A, 0, B, 0, iLength);
		A=B;
		shiftDown(0);
		return dbMax;
	}

	private void shiftDown(int i) {
		int largest=i;
		int leftChild=getLeftChild(i+1)-1;
		int rightChild=getRightChild(i+1)-1;
		if(leftChild<A.length && 0<A[leftChild].getAmount().compareTo(A[largest].getAmount())){
			largest=leftChild;
		}
		if(rightChild<A.length && 0>A[rightChild].getAmount().compareTo(A[largest].getAmount())){
			largest=rightChild;
		}
		if(largest!=i){
			swap(i,largest);
			shiftDown(largest);
		}
	}
	
	public void putValue(DistributionBean value){
		int iLength=A.length+1;
		DistributionBean[] B=new DistributionBean[iLength];
		B[iLength-1]=value;
		System.arraycopy(A, 0, B, 0, iLength-1);
		A=B;
		shiftUp(iLength-1);
	}

	private void shiftUp(int i) {
		int iElement=i;
		int iParent=getParent(iElement+1)-1;
		while(iParent>=0 && 0>A[iParent].getAmount().compareTo(A[iElement].getAmount())){
			swap(iParent, iElement);
			iElement=iParent;
			iParent=getParent(iElement+1)-1;
		}
	}

	
	public void buildMaxHeap() {
		for(int i=((A.length+1)/2)-1;i>=0;i--){
			shiftDown(i);
		}
	}
	
	private void swap(int i, int largest) {
		DistributionBean temp;
		temp=A[i];
		A[i]=A[largest];
		A[largest]=temp;
		
	}
	
}
