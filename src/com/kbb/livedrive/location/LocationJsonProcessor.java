package com.kbb.livedrive.location;

import org.json.JSONObject;

import com.kbb.livedrive.artifact.Location;

public interface LocationJsonProcessor {

	public Location getLocation(JSONObject location);
}
