package com.ford.mobileweather.location;

import org.json.JSONObject;

import com.ford.mobileweather.artifact.Location;

public interface LocationJsonProcessor {

	public Location getLocation(JSONObject location);
}
