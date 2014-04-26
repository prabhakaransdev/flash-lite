package com.prabhakarans.flashlight;

/**
 * Author	: 	Prabhakaran Srinivasan
 * App		:	flash.lite
 * Date		:	23-Feb-2014
 * Mod Date	:	25-Apr-2014
 * Version	:	1.0 beta
 *
 * Copyright (c) 2014 Prabhakaran Srinivasan
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

public class FlashMe extends Activity {
	Boolean FLASH_ON = false;
	LinearLayout clicker;
	Camera cam = null;
	SurfaceView sv;
	SurfaceHolder sh;
	Parameters cp;
	Resources r;
	Drawable drawBulb;
	Context c;
	Toast t;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		clicker = (LinearLayout) findViewById(R.id.clicker);
		sv = (SurfaceView) findViewById(R.id.preview);
		sh = sv.getHolder();
		r = getResources();
		drawBulb = r.getDrawable(R.drawable.unlit_bulb);
		clicker.setBackground(drawBulb);		
		clicker.setOnLongClickListener(new LinearLayout.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
		    	aboutBox();
		    	return true;
		    }
		});
		clicker.setOnClickListener(new LinearLayout.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				toggleFlash();
			}
		});
		
	}

	private void aboutBox() {
		LayoutInflater li = LayoutInflater.from(this);
		View view = li.inflate(R.layout.about, null);
	        Linkify.addLinks((TextView)view.findViewById(R.id.dev_email), Linkify.ALL);
		new AlertDialog.Builder(FlashMe.this)
				.setTitle("About")
				.setView(view)
				.setNegativeButton("Close",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int whichButton) {
							}
						}).show();
	}

	//check whether capable of a flashlight
	public Boolean canFlash() {
		Context c = this;
		PackageManager pm = c.getPackageManager();
		if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
			return true;
		else
			return false;

	}

	// flashlight toggle
	public void toggleFlash() {
		if (!FLASH_ON)
			turnFlashOn();
		else
			turnFlashOff();
	}

	// turn flashlight ON
	public void turnFlashOn() {
		Boolean canFlash = canFlash(); 
		turnFlashOff();
		if (canFlash && cam == null) {
			try {
				cam = Camera.open();
				cam.setPreviewDisplay(sh);
			} catch (Exception io) {
				io.printStackTrace();
			}
			cam.startPreview();
			try {//turn on the flash
				cp = cam.getParameters();
				cp.setFlashMode(Parameters.FLASH_MODE_TORCH);
				cam.setParameters(cp);
				FLASH_ON = true;
				r = getResources();
				drawBulb = r.getDrawable(R.drawable.lit_bulb);
				clicker.setBackground(drawBulb);
				serveToast("flash.lite started!");
			} catch (Exception e) {
				e.printStackTrace();
				}
		}
		else{//lets paint the screen white for devices without flashlight
			serveToast("Device flash incapable! Use screen as Light.");
			clicker.setBackgroundColor(Color.WHITE);
		}
	}

	//turn flashlight OFF
	public void turnFlashOff() {
		try {
			if (cam != null) {
				cp = cam.getParameters();
				if (cp.getFlashMode().equals(Parameters.FLASH_MODE_TORCH)) {
					cp.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
					cam.setParameters(cp);
					FLASH_ON = false;
					r = getResources();
					drawBulb = r.getDrawable(R.drawable.unlit_bulb);
					clicker.setBackground(drawBulb);
				}
			}
		} finally {
			//turn the cam OFF if it is ON
			if (cam != null) {
				cam.release();
				cam = null;
				serveToast("flash.lite stopped!");
			}
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (FLASH_ON == true) {
			turnFlashOff();
			serveToast("flash.lite stopped!");
			//turn the cam OFF if it is ON
			if (cam != null) {
				cam.release();
				cam = null;
			}
		}
	}

	public void serveToast(String ts) {
		c = getApplicationContext();
		t = android.widget.Toast.makeText(c, ts,Toast.LENGTH_SHORT);
		t.show();
	}
	}
