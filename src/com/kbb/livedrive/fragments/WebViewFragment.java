package com.kbb.livedrive.fragments;



import java.util.ArrayList;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.leaderboard.Leaderboard;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardVariantRef;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.games.leaderboard.Leaderboards.LoadPlayerScoreResult;
import com.google.android.gms.games.leaderboard.Leaderboards.LoadScoresResult;
import com.kbb.livedrive.R;
import com.kbb.livedrive.app.LiveDriveApplication;
import com.kbb.livedrive.applink.AppLinkService;
import com.kbb.livedrive.artifact.Location;
import com.kbb.livedrive.googleplay.GooglePlayService;
import com.kbb.livedrive.profile.ProfileService;
import com.kbb.livedrive.scoring.DriverScoreService;
//import com.kbb.livedrive.adapter.ForecastListAdapter;
//import com.kbb.livedrive.weather.DayForecast;
//import com.kbb.livedrive.weather.WeatherDataManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Display;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class WebViewFragment extends BaseFragment {
	private View fragmentView;

	private TextView mTextView;
	private WebView leaderboardView; 
	
	private String leaderboardType = ""; //getString(R.string.leaderboard_type_driver);
	
	private boolean viewLoadFinished = false;
	
	private final double PIC_WIDTH = 1920;
	
	private Context context;

	private BroadcastReceiver driverScoreChangedReceiver = new BroadcastReceiver(){
		public void onReceive(android.content.Context context, Intent intent) {
			
			long driverScore = intent.getLongExtra("driverScore", 50);
			long previousDriverScore = intent.getLongExtra("previousDriverScore", 50);
			long bestDriverScore = intent.getLongExtra("bestDriverScore", 50);
			String driverRank = intent.getStringExtra("driverRank");
			
			//update UI on driver score change
			leaderboardView.loadUrl(String.format("javascript:drawDriverScore(%s,%s,%s);", driverScore, previousDriverScore, driverRank));
		}
	};

	private BroadcastReceiver mpgScoreChangedReceiver = new BroadcastReceiver(){
		public void onReceive(android.content.Context context, Intent intent) {

			long driverScore = intent.getLongExtra("driverScore", 50);
			long previousDriverScore = intent.getLongExtra("previousDriverScore", 50);
			long bestDriverScore = intent.getLongExtra("bestDriverScore", 50);
			String driverRank = intent.getStringExtra("driverRank");
			
			//update UI on mpg score change
			leaderboardView.loadUrl(String.format("javascript:drawMpgScore(%s,%s,%s);", driverScore, previousDriverScore, driverRank));

		}
	};
	
	private BroadcastReceiver driverRankChangedReceiver = new BroadcastReceiver(){
		public void onReceive(android.content.Context context, Intent intent) {
			//TODO update UI on driver rank change
		}
	};

	private BroadcastReceiver mpgRankChangedReceiver = new BroadcastReceiver(){
		public void onReceive(android.content.Context context, Intent intent) {
			//TODO update UI on mpg rank change
		}
	};

	private BroadcastReceiver driverLeaderboardChangedReceiver = new BroadcastReceiver(){
		public void onReceive(android.content.Context context, Intent intent) {
			//TODO update UI on driver leaderboard change
		}
	};

	private BroadcastReceiver mpgLeaderboardChangedReceiver = new BroadcastReceiver(){
		public void onReceive(android.content.Context context, Intent intent) {
			//TODO update UI on mpg leaderboard change
		}
	};

	
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		fragmentView = inflater.inflate(R.layout.fragment_web_view, null);
		
		context = getActivity().getApplicationContext();
		
		
		LocalBroadcastManager lb = LocalBroadcastManager.getInstance(context);
		
		lb.registerReceiver(driverScoreChangedReceiver, new IntentFilter(ProfileService.ACTION_DRIVER_SCORE_CHANGED));
		lb.registerReceiver(mpgScoreChangedReceiver, new IntentFilter(ProfileService.ACTION_MPG_SCORE_CHANGED));
		lb.registerReceiver(driverRankChangedReceiver, new IntentFilter(ProfileService.ACTION_DRIVER_PLAYER_RANK_CHANGED));
		lb.registerReceiver(mpgRankChangedReceiver, new IntentFilter(ProfileService.ACTION_MPG_PLAYER_RANK_CHANGED));
		lb.registerReceiver(driverLeaderboardChangedReceiver, new IntentFilter(ProfileService.ACTION_DRIVER_LEADERBOARD_CHANGED));
		lb.registerReceiver(mpgLeaderboardChangedReceiver, new IntentFilter(ProfileService.ACTION_MPG_LEADERBOARD_CHANGED));

	
        mTextView = (TextView) fragmentView.findViewById(R.id.textview);
        
        //TODO: populate textview lable when vehcile details are available. they are not available here
        //mTextView.setText(getTextViewLabel());
        
		leaderboardView = (WebView) fragmentView.findViewById(R.id.scoreswebview);
		leaderboardView.getSettings().setJavaScriptEnabled(true);
		leaderboardView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE); // disables cache
		leaderboardView.setWebViewClient(Client);
		
		leaderboardView.setPadding(0, 0, 0, 0);
		leaderboardView.setInitialScale(getScale());

		leaderboardView.loadUrl("file:///android_asset/leaderboard.html");
						
		return fragmentView;
	}
	
	private int getScale(){
	    Double val = new Double(PIC_WIDTH)/new Double(PIC_WIDTH);
	    val = val * 100d;
	    return val.intValue();
	}

	private CharSequence getTextViewLabel() {
		//android:text="LadiesMan217|2014 Ford Flex Limited"
        String driverName = ProfileService.getInstance().getCurrentPlayer().getUserName();
        String vehicleName = ProfileService.getInstance().getCurrentVehicle().getVehicleName();
        		
        SpannableString span1 = new SpannableString(driverName);
        SpannableString span2 = new SpannableString(vehicleName);

        span1.setSpan(
        		new RelativeSizeSpan(1.0f),  0, driverName.length(), 0);
        span1.setSpan(
        		new ForegroundColorSpan(Color.parseColor("#1155A5")), 
        		0, driverName.length(), 0);

        span2.setSpan(
        		new RelativeSizeSpan(0.75f),  0, vehicleName.length(), 0);
        span2.setSpan(new ForegroundColorSpan(Color.BLACK), 
        		0, vehicleName.length(), 0);	
        
        return TextUtils.concat(span1, "\n", span2);
	}

	public String getLeaderboardType (){
		return leaderboardType;
	}
	
	public void setLeaderboardType(String type){
		leaderboardType = type;
	}
	
	final WebViewClient Client = new WebViewClient () {
		
		@Override
	    public void onPageFinished(WebView view, String url) {	
			
			if(url.contains("file:///android_asset/leaderboard.html")){
			
				ProfileService ps = ProfileService.getInstance();
				
				ps.requestDriverScoreUpdate();
				ps.requestMpgScoreUpdate();
				
				ps.requestDriverRankUpdate();
				
				ps.requestMpgRankUpdate();
				
				ps.requestDriverLeaderboardUpdate();
				
				ps.requestMpgLeaderboardUpdate();
				
			}
	    }
	};
	
}
