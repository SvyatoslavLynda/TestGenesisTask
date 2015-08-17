package com.svdroid.testgenesistask.activity.utils;

import android.content.Context;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AppUtils
{
	private static final String INPUT_DATE_TEMPLATE = "yyyy-MM-dd'T'HH:mm:ssZ";
	private static final String OUTPUT_DATE_TEMPLATE = "yyyy-MM-dd HH:mm:ss";

	private AppUtils()
	{

	}

	public static String formatTimeWithTimeZone(String dateString)
		throws ParseException
	{

		final SimpleDateFormat outputSDF = new SimpleDateFormat(OUTPUT_DATE_TEMPLATE, Locale.getDefault());
		final SimpleDateFormat inputSDF = new SimpleDateFormat(INPUT_DATE_TEMPLATE, Locale.getDefault());
		inputSDF.setTimeZone(TimeZone.getTimeZone("UTC"));

		final Date date = inputSDF.parse(dateString);

		return outputSDF.format(date);
	}

	public static void initImageLoader(Context context)
	{
		final DisplayImageOptions displayImageOptions = _getDisplayImageOptions();
		final ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(context)
			.defaultDisplayImageOptions(displayImageOptions);

		ImageLoader.getInstance().init(builder.build());
	}

	private static DisplayImageOptions _getDisplayImageOptions()
	{
		return new DisplayImageOptions.Builder()
			.showImageOnLoading(android.R.color.transparent)
			.showImageForEmptyUri(android.R.color.transparent)
			.showImageOnFail(android.R.color.transparent)
//			.showImageOnFail(android.R.drawable.)
//			.showImageOnLoading(R.drawable.ic_picture)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.imageScaleType(ImageScaleType.EXACTLY)
			.considerExifParams(true)
			.build();
	}
}
