package com.trip.expensemanager.fragments;
import android.support.v4.app.DialogFragment;


public interface ConfirmDialogListener {
	public void onDialogPositiveClick(DialogFragment dialog);
	public void onDialogNegativeClick(DialogFragment dialog);
}
