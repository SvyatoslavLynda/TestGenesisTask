package com.svdroid.testgenesistask.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.svdroid.testgenesistask.R;
import com.svdroid.testgenesistask.activity.utils.AppUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;

public class MainActivity extends AppCompatActivity
	implements LoaderManager.LoaderCallbacks<JSONObject>, Html.ImageGetter
{
	private static final String LOG_TAG = "MainActivity";
	private static final String KEY_TITLE = "title";
	private static final String KEY_DATE = "date";
	private static final String KEY_CONTENT = "content";
	private static final String KEY_IMG = "img";
	private static final int API_LOADER_ID = 0;

	private TextView _title;
	private TextView _time;
	private TextView _content;
	private ImageView _img;
	private WebView _webView;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		AppUtils.initImageLoader(this);

		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
		collapsingToolbar.setTitle(getText(R.string.app_name));

		_title = (TextView) findViewById(R.id.title);
		_time = (TextView) findViewById(R.id.time);
		_content = (TextView) findViewById(R.id.content);
		_content.setMovementMethod(new ScrollingMovementMethod());
		_content.setMovementMethod(LinkMovementMethod.getInstance());
		_img = (ImageView) findViewById(R.id.img);
		_webView = (WebView) findViewById(R.id.web_view);
		_webView.getSettings().setJavaScriptEnabled(true);
		_webView.getSettings().setUseWideViewPort(true);
		_webView.getSettings().setLoadWithOverviewMode(true);
		_webView.setInitialScale(1);
		_webView.setBackgroundColor(Color.TRANSPARENT);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		getSupportLoaderManager().initLoader(API_LOADER_ID, null, this).forceLoad();
	}

	@Override
	public Drawable getDrawable(String source)
	{
		final LevelListDrawable d = new LevelListDrawable();
		ImageLoader.getInstance().loadImage(source, new TextImageLoaderListener(d));

		return d;
	}

	@Override
	public Loader<JSONObject> onCreateLoader(int id, Bundle args)
	{
		return new APILoader(this);
	}

	@Override
	public void onLoadFinished(Loader<JSONObject> loader, JSONObject data)
	{
		if (data != null && loader.getId() == API_LOADER_ID) {
			_setupData(data);
		} else {
			Toast.makeText(this, getText(R.string.message_fail_load), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onLoaderReset(Loader<JSONObject> loader)
	{

	}

	private void _setupData(JSONObject obj)
	{
		try {
			_title.setText(obj.getString(KEY_TITLE));
			_time.setText(AppUtils.formatTimeWithTimeZone(obj.getString(KEY_DATE)));
			ImageLoader.getInstance().displayImage(obj.getString(KEY_IMG), _img);
			final String content = obj.getString(KEY_CONTENT);
			_content.setText(Html.fromHtml(content, MainActivity.this, null));
			final String iFrame = content.replaceAll(".*(<iframe.*?/iframe>).*", "$1");
			_webView.loadData(iFrame, "text/html", "UTF-8");
		} catch (JSONException | ParseException e) {
			Log.e(LOG_TAG, e.getMessage());
		}
	}

	private static class APILoader extends AsyncTaskLoader<JSONObject>
	{
		private static final String API_URL = "http://app.naij.com/test.json";
		private final OkHttpClient client = new OkHttpClient();

		public APILoader(Context context)
		{
			super(context);
		}

		@Override
		public JSONObject loadInBackground()
		{
			JSONObject obj = null;

			try {
				obj = new JSONObject(_run(API_URL));
				Log.d(LOG_TAG, obj.toString());
			} catch (IOException | JSONException e) {
				Log.w(LOG_TAG, e.getMessage());
			}

			return obj;
		}

		private String _run(String url)
			throws IOException
		{
			Request request = new Request.Builder()
				.url(url)
				.build();
			Response response = client.newCall(request).execute();

			return response.body().string();
		}
	}

	public class TextImageLoaderListener implements ImageLoadingListener
	{
		private final LevelListDrawable _drawable;

		public TextImageLoaderListener(LevelListDrawable drawable)
		{
			_drawable = drawable;
		}

		@Override
		public void onLoadingStarted(String imageUri, View view)
		{

		}

		@Override
		public void onLoadingFailed(String imageUri, View view, FailReason failReason)
		{

		}

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
		{
			final Display display = getWindowManager().getDefaultDisplay();
			int width;
			int height;
			final int activityMargin = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);

			if (Build.VERSION.SDK_INT == Build.VERSION_CODES.HONEYCOMB_MR2) {
				final Point point = new Point();
				display.getSize(point);
				width = point.x - 2 * activityMargin;
				height = width * point.y / loadedImage.getWidth();
			} else {
				//noinspection deprecation
				width = display.getWidth() - 2 * activityMargin;
				//noinspection deprecation
				height = width * display.getHeight() / loadedImage.getWidth();
			}

			if (width > height) {
				width = loadedImage.getWidth();
				height = loadedImage.getHeight();
			}

			final BitmapDrawable d = new BitmapDrawable(getResources(), loadedImage);
			_drawable.addLevel(1, 1, d);
			_drawable.setBounds(0, 0, width, height);
			_drawable.setLevel(1);
			final CharSequence t = _content.getText();
			_content.setText(t);
		}

		@Override
		public void onLoadingCancelled(String imageUri, View view)
		{

		}
	}
}