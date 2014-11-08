public static class PlaceholderFragment extends Fragment {

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
			     Bundle savedInstanceState) {
	View rootView = inflater.inflate(R.layout.fragment_main, container,
					 false);			
	WebView web = (WebView)rootView.findViewById(R.id.webView1);
	web.getSettings().setJavaScriptEnabled(true);
	web.setWebViewClient(new Client());
	web.loadUrl("file:///android_asset/html/livedrive.html");						
	return rootView;
    }
}
	
public static class Client extends WebViewClient {
    @Override
    public void onPageFinished(WebView view, String url) {			
	view.loadUrl("javascript:draw(55,75);");
    }
}
