package com.zst.xposed.halo.floatingwindow.helpers;

import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.zst.xposed.halo.floatingwindow.hooks.MovableWindow;

public class Movable implements View.OnTouchListener {
	private static Float screenX;
	private static Float screenY;
	private static Float viewX;
	private static Float viewY;
	private static Float leftFromScreen;
	private static Float topFromScreen;
	final Window mWindow;
	final LayoutParams param;
	final AeroSnap mAeroSnap;
	final boolean mReturn;
	private View offsetView;

	public Movable(Window window, AeroSnap aerosnap, boolean return_value) {
		mWindow = window;
		param = mWindow.getAttributes();
		mAeroSnap = aerosnap;
		mReturn = return_value;
	}

	public Movable(Window window, View v, AeroSnap aerosnap) {
		this(window, aerosnap, false);
		offsetView = v;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				viewX = event.getX();
				viewY = event.getY();
				if (offsetView != null) {
					int[] location = {0, 0};
					offsetView.getLocationInWindow(location);
					viewX = viewX + location[0];
					viewY = viewY + location[1];
				}
				break;
			case MotionEvent.ACTION_MOVE:
				screenX = event.getRawX();
				screenY = event.getRawY();
				leftFromScreen = (screenX - viewX);
				topFromScreen = (screenY - viewY);
				mWindow.setGravity(Gravity.LEFT | Gravity.TOP);
				updateView(mWindow, leftFromScreen, topFromScreen);
				break;
		}
		if (mAeroSnap != null) {
			mAeroSnap.dispatchTouchEvent(event);
		}
		return mReturn;
	}

	private void updateView(Window mWindow, float x, float y) {
		param.x = (int) x;
		param.y = (int) y;
		mWindow.setAttributes(param);
		MovableWindow.initAndRefreshLayoutParams(mWindow, mWindow.getContext(), mWindow.getContext().getPackageName());
	}
}
