package x40240.shaun.adriano.a4.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import x40240.shaun.adriano.a4.app.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class MainActivity extends Activity {
	public static final String LOGTAG = "WebView Map";
	public static final String MAPS_BASE_URL = "http://maps.google.com?z=15&q=";
	private final boolean DEBUG = true;

	private int index;
	private String feedUrl;
	private int locationCount;
	private TextView addressText;
	private TextView descriptionText;

	private WebView webView;
	private final ArrayList<String> allUrls = new ArrayList<String>();
	private final ArrayList<URL> locationUrl = new ArrayList<URL>();
	private final ArrayList<LocationInfo> locationArray = new ArrayList<LocationInfo>();
	private FetchDataTask fetchDataTask;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		feedUrl = getString(R.string.feed_url);
		addressText = (TextView) findViewById(R.id.address_value);
		descriptionText = (TextView) findViewById(R.id.description_value);

		webView = (WebView) findViewById(R.id.web_view);
		webView.setKeepScreenOn(true);
		webView.setInitialScale(100);
		webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		webView.setBackgroundColor(Color.TRANSPARENT);
		webView.setWebViewClient(new MyWebViewClient());

		final WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setBuiltInZoomControls(true);

		
		if (fetchDataTask != null)
			return;
			
		fetchDataTask = new FetchDataTask();
		fetchDataTask.execute(feedUrl, String.valueOf(locationCount));
		

		


	}

	@Override
	protected void onPause() {
		if (DEBUG)
			Log.d(LOGTAG, "onPause()");
		super.onPause();
		if (fetchDataTask != null) {
			fetchDataTask.cancel(true);
			fetchDataTask = null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onNextButtonClick(View view) {
		
		
		if (index > (allUrls.size() - 1))
			index = 0;
		final String url = allUrls.get(index);
		// Checking the URL
		System.out.println("Maps URL: "+url);
		webView.loadUrl(url);
		

		
		index++;


		
	}

	private class MyWebViewClient extends WebViewClient {
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler,
				SslError error) {
			Log.e(LOGTAG, error.toString());
			handler.proceed();
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return false;
		}
	}

	private void onTaskCompleted(boolean success) {
		fetchDataTask = null;
		if (success)
			locationCount++;
		else
			locationCount = 0;
		System.out.println("location count: " + locationCount);
	}

	private class FetchDataTask extends AsyncTask<String, Void, LocationInfo> {

		@Override
		protected LocationInfo doInBackground(String... paramArrayOfParams) {

			// Get feedUrl param passed to us.
			final String feedUrl = paramArrayOfParams[0];
			int locationCount = Integer.parseInt(paramArrayOfParams[1]);

			LocationInfo locationInfo = null;
			InputStream in = null;
			
			
			try {
				
					for (locationCount = 0; locationCount < 5; locationCount++) {
						final StringBuilder sb = new StringBuilder(feedUrl);
						sb.append("location-");
						sb.append(locationCount);
						sb.append(".xml");
						
						final URL url = new URL(sb.toString());
						
						final HttpURLConnection httpConnection = (HttpURLConnection) url
								.openConnection();
						final int responseCode = httpConnection.getResponseCode();
						//in = httpConnection.getInputStream();
						
						
						if (responseCode != HttpURLConnection.HTTP_OK) {
							 continue;
						} 
						//locationInfo = new LocationInfoSAX().parse(in);
						 locationUrl.add(url);
					}
						
					for (locationCount = 5;; locationCount++) {
						final StringBuilder sb = new StringBuilder(feedUrl);
						sb.append("location-");
						sb.append(locationCount);
						sb.append(".xml");
						
						final URL url = new URL(sb.toString());
						
						final HttpURLConnection httpConnection = (HttpURLConnection) url
								.openConnection();
						final int responseCode = httpConnection.getResponseCode();
						//in = httpConnection.getInputStream();
						
						
						if (responseCode != HttpURLConnection.HTTP_OK) {
							break;
						}
						//locationInfo = new LocationInfoSAX().parse(in);
						 locationUrl.add(url);
					}
					
					//testing
					for (int i = 0; i < locationUrl.size(); i++) {
					final URL url2 = new URL(locationUrl.get(i).toString());
					
					// Checking the array
						System.out.println("Printing the array of location-n.xml files...");
					for (int n = 0; n < locationUrl.size(); n++)
						System.out.println(locationUrl.get(n).toString());
					
					final HttpURLConnection httpConnection2 = (HttpURLConnection) url2
							.openConnection();
					final int responseCode2 = httpConnection2.getResponseCode();
					
					if (responseCode2 != HttpURLConnection.HTTP_OK) {
						// Handle error.
						Log.e(LOGTAG, "responseCode=" + responseCode2);
						return null;
					}
					
					in = httpConnection2.getInputStream();
					locationInfo = new LocationInfoSAX().parse(in);
					
					locationArray.add(locationInfo);
					}
				
				


			} catch (IOException e) {
				e.printStackTrace();
			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) { /* ignore */
					}
				}
			}
			return locationInfo;
		}

		// This runs in the main thread and can update the UI
		@Override
		protected void onPostExecute(LocationInfo locationInfo) {
			
			locationInfo = locationArray.get(index);
			
			if (locationInfo == null) {
				onTaskCompleted(false);
				return;
			}
			addressText.setText(locationInfo.getAddress());
			descriptionText.setText(locationInfo.getDescription());

			for (int i = 0; i < locationArray.size(); i++)
			{
			locationInfo = locationArray.get(i);
			final StringBuilder sb = new StringBuilder();
			String createdLocation = locationInfo.getLatitude() + ","
					+ locationInfo.getLongitude();
			sb.append(MAPS_BASE_URL);
			sb.append(createdLocation);
			allUrls.add(sb.toString());
			sb.delete(0, sb.length());
			}
			
			
			webView.loadUrl(allUrls.get(index));


			

			onTaskCompleted(true);
		}

	}
}
