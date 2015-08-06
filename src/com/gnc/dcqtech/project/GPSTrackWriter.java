package com.gnc.dcqtech.project;

import java.io.File;

import android.content.Context;
import android.location.Location;

public class GPSTrackWriter extends GPSTrackAdapter {

	public GPSTrackWriter(File projectFolder) {
		super(projectFolder, false);
		// TODO Auto-generated constructor stub
	}
	
	public long	insert(Context context, Location location){
		return super.insert(context, location);
	}

}
