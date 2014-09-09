package com.ford.mobileweather.processor;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.ford.mobileweather.app.LiveDriveApplication;

public class ImageProcessor {

	private static final Map<String, String> conditionsImageMap = 
		    Collections.unmodifiableMap(new HashMap<String, String>() {
				private static final long serialVersionUID = 1L;
			{ 
		        put("sunny.gif", "sunny");
		        put("clear.gif", "sunny");
		        put("mostlysunny.gif", "partlysunny");
		        put("partlysunny.gif", "partlysunny");
		        put("hazy.gif", "hazy");
		        put("fog.gif", "fog");
		        put("partlycloudy.gif", "partlycloudy");
		        put("chancetstorms.gif", "tstorms");
		        put("tstorms.gif", "tstorms");
		        put("chancerain.gif", "chancerain");
		        put("rain.gif", "rain");
		        put("chanceflurries.gif", "flurries");
		        put("flurries.gif", "flurries");
		        put("chancesnow.gif", "chancesnow");
		        put("snow.gif", "snow");
		        put("chancesleet.gif", "sleet");
		        put("sleet.gif", "sleet");
		        put("mostlycloudy.gif", "cloudy");
		        put("cloudy.gif", "cloudy");
		        put("nt_sunny.gif", "sunny");
		        put("nt_clear.gif", "sunny");
		        put("nt_mostlysunny.gif", "partlysunny");
		        put("nt_partlysunny.gif", "partlysunny");
		        put("nt_hazy.gif", "hazy");
		        put("nt_fog.gif", "fog");
		        put("nt_partlycloudy.gif", "partlycloudy");
		        put("nt_chancetstorms.gif", "tstorms");
		        put("nt_tstorms.gif", "tstorms");
		        put("nt_chancerain.gif", "chancerain");
		        put("nt_rain.gif", "rain");
		        put("nt_chanceflurries.gif", "flurries");
		        put("nt_flurries.gif", "flurries");
		        put("nt_chancesnow.gif", "chancesnow");
		        put("nt_snow.gif", "snow");
		        put("nt_chancesleet.gif", "sleet");
		        put("nt_sleet.gif", "sleet");
		        put("nt_mostlycloudy.gif", "cloudy");
		        put("nt_cloudy.gif", "cloudy");
		    }});
	
	public static String getMappedConditionsImageName(String conditionsImage) {
		if (conditionsImageMap.containsKey(conditionsImage)) {
			return conditionsImageMap.get(conditionsImage);
		}
		else {
			return null;
		}
	}
	
	public static Bitmap getBitmapFromResources(String imageName) {
		Resources resources = LiveDriveApplication.getInstance().getResources();
		int resId = resources.getIdentifier(imageName, "drawable", "com.ford.mobileweather");
		return BitmapFactory.decodeResource(resources, resId);
	}
	
	public static String getFileFromURL(URL url) {
		if (url != null) {
			String urlPath = url.getFile();
			return urlPath.substring(urlPath.lastIndexOf('/') + 1, urlPath.length());
		}
		else {
			return null;
		}
	}
	
	public static void setConditionsImage(ImageView imageView, URL conditionsImageURL) {
		String conditionsImageName = getFileFromURL(conditionsImageURL);
		String mappedName = getMappedConditionsImageName(conditionsImageName);
		if (mappedName != null) {
			Bitmap mappedImage = getBitmapFromResources(mappedName);
			imageView.setImageBitmap(mappedImage);
		}
		else {
			// TODO: download from the web
		}
	}
	
	public static byte[] getConditionsImageBytes(URL conditionsImageURL) {
		String conditionsImageName = getFileFromURL(conditionsImageURL);
		String mappedName = getMappedConditionsImageName(conditionsImageName);
		Bitmap bm = null;
		if (mappedName != null) {			
			bm = getBitmapFromResources(mappedName);
		}
		else {
			// TODO
		}
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
		return stream.toByteArray();
	}
}


