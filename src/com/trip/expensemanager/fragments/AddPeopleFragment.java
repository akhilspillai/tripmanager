package com.trip.expensemanager.fragments;

import java.util.EnumMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.trip.expensemanager.R;
import com.trip.utils.Constants;

public class AddPeopleFragment extends CustomFragment {

	public static Fragment newInstance(long lngTripId, String strTripName, long lngAdminId, String strDate) {
		Fragment fragment=new AddPeopleFragment();
		try {
			Bundle bundle=new Bundle();
			bundle.putLong(Constants.STR_TRIP_ID, lngTripId);
			bundle.putString(Constants.STR_TRIP_NAME, strTripName);
			bundle.putLong(Constants.STR_ADMIN_ID, lngAdminId);
			bundle.putString(Constants.STR_DATE, strDate);
			fragment.setArguments(bundle);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fragment;
	}

	private ImageView ivQrImage;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView=null;
		try {
			((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.add_people);
			Bundle bundle=getArguments();
			long lngTripId=bundle.getLong(Constants.STR_TRIP_ID);
			String strTripName=bundle.getString(Constants.STR_TRIP_NAME);
			long lngAdminId=bundle.getLong(Constants.STR_ADMIN_ID);
			String strDate=bundle.getString(Constants.STR_DATE);
			rootView=inflater.inflate(R.layout.fragment_add_people, container, false);
			ivQrImage=(ImageView) rootView.findViewById(R.id.iv_qrcode);
			int size=Integer.parseInt(getActivity().getResources().getString(R.string.qr_code_size));
			Bitmap bm = encodeAsBitmap(lngTripId+","+strTripName+","+strDate+","+lngAdminId, BarcodeFormat.QR_CODE, size, size);

			if(bm != null) {
				ivQrImage.setImageBitmap(bm);
//				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ivQrImage.getLayoutParams(); 
//			    params.width = size;
//			    params.height = size;
//			    ivQrImage.setLayoutParams(params);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rootView;
	}

	public Bitmap encodeAsBitmap(String content, BarcodeFormat bf, int width, int height) throws WriterException {
		QRCodeWriter writer = new QRCodeWriter();
		Bitmap bmp=null;
		try {
			Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
//			hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
			hints.put(EncodeHintType.MARGIN, 4);
			BitMatrix bitMatrix = writer.encode(content, bf, width, height, hints);
			int bmWidth = bitMatrix.getWidth();
			int bmHeight = bitMatrix.getHeight();
			bmp = Bitmap.createBitmap(bmWidth, bmHeight, Bitmap.Config.RGB_565);
			for (int x = 0; x < bmWidth; x++) {
				for (int y = 0; y < bmHeight; y++) {
					bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
				}
			}

		} catch (WriterException e) {
			e.printStackTrace();
		}
		return bmp;
	}
	
}
