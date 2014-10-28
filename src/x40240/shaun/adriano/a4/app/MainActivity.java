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
			
		/*
		for (;;)
		{
			for (int i = 0; i < 5) {
				
			}
		}
		*/
		final String url = allUrls.get(index);
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
	}

	private class FetchDataTask extends AsyncTask<String, Void, LocationInfo> {

		@Override
		protected LocationInfo doInBackground(String... paramArrayOfParams) {

			// Get feedUrl param passed to us.
			final String feedUrl = paramArrayOfParams[0];
			final int locationCount = Integer.parseInt(paramArrayOfParams[1]);

			LocationInfo locationInfo = null;
			InputStream in = null;

			try {
				final StringBuilder sb = new StringBuilder(feedUrl);
				sb.append("location-");
				sb.append(locationCount);
				sb.append(".xml");

				// http://www.jeffreypeacock.com/uci/x402.40/data/location-0.xml
				final URL url = new URL(sb.toString());
				final HttpURLConnection httpConnection = (HttpURLConnection) url
						.openConnection();
				final int responseCode = httpConnection.getResponseCode();

				if (responseCode != HttpURLConnection.HTTP_OK) {
					// Handle error.
					Log.e(LOGTAG, "responseCode=" + responseCode);
					return null;
				}
				in = httpConnection.getInputStream();
				locationInfo = new LocationInfoSAX().parse(in);
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

			if (locationInfo == null) {
				onTaskCompleted(false);
				return;
			}
			addressText.setText(locationInfo.getAddress());
			descriptionText.setText(locationInfo.getDescription());

			final StringBuilder sb = new StringBuilder();
			String createdLocation = locationInfo.getLatitude() + ","
					+ locationInfo.getLongitude();

			sb.append(MAPS_BASE_URL);
			sb.append(createdLocation);
			allUrls.add(sb.toString());
			sb.delete(0, sb.length());
			webView.loadUrl(allUrls.get(index));

			onTaskCompleted(true);
		}

	}
}
