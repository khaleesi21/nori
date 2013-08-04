/*
 Copyright (c) 2012 Roman Truba

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial
 portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package pe.moe.nori.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Debug;
import android.util.AttributeSet;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import pe.moe.nori.R;
import pe.moe.nori.api.Image;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class UrlTouchImageView extends RelativeLayout {
  protected ProgressBar mProgressBar;
  protected TouchImageView mImageView;

  protected Context mContext;

  public UrlTouchImageView(Context ctx)
  {
    super(ctx);
    mContext = ctx;
    init();

  }
  public UrlTouchImageView(Context ctx, AttributeSet attrs)
  {
    super(ctx, attrs);
    mContext = ctx;
    init();
  }
  public TouchImageView getImageView() { return mImageView; }

  @SuppressWarnings("deprecation")
  protected void init() {
    mImageView = new TouchImageView(mContext);
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    mImageView.setLayoutParams(params);
    this.addView(mImageView);
    mImageView.setVisibility(GONE);

    mProgressBar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleHorizontal);
    params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    params.addRule(RelativeLayout.CENTER_VERTICAL);
    params.setMargins(30, 0, 30, 0);
    mProgressBar.setLayoutParams(params);
    mProgressBar.setIndeterminate(false);
    mProgressBar.setMax(100);
    this.addView(mProgressBar);
  }

  public void setUrl(Image image)
  {
    new ImageLoadTask().execute(image);
  }
  //No caching load
  public class ImageLoadTask extends AsyncTask<Image, Integer, Bitmap>
  {
    @Override
    protected Bitmap doInBackground(Image... images) {
      Image image = images[0];
      Bitmap bm = null;
      try {
        URL aURL = new URL(image.sampleUrl);
        URLConnection conn = aURL.openConnection();
        conn.connect();
        InputStream is = conn.getInputStream();
        int totalLen = conn.getContentLength();
        InputStreamWrapper bis = new InputStreamWrapper(is, 8192, totalLen);
        bis.setProgressListener(new InputStreamWrapper.InputStreamProgressListener()
        {
          @Override
          public void onProgress(float progressValue, long bytesLoaded,
                                 long bytesTotal)
          {
            publishProgress((int)(progressValue * 100));
          }
        });
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(image, getWidth(), getHeight());
        options.inPurgeable = true;
        bm = BitmapFactory.decodeStream(bis, null, options);
        bis.close();
        is.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return bm;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
      if (bitmap == null)
      {
        mImageView.setScaleType(ScaleType.CENTER);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_load_error);
        mImageView.setImageBitmap(bitmap);
      }
      else
      {
        mImageView.setScaleType(ScaleType.MATRIX);
        mImageView.setImageBitmap(bitmap);
      }
      mImageView.setVisibility(VISIBLE);
      mProgressBar.setVisibility(GONE);
    }

    @Override
    protected void onProgressUpdate(Integer... values)
    {
      mProgressBar.setProgress(values[0]);
    }
  }

  public static int calculateInSampleSize(
      Image image, int reqWidth, int reqHeight) {
    float bitmapWidth = image.sampleWidth;
    float bitmapHeight = image.sampleHeight;

    int bitmapResolution = (int) (bitmapWidth * bitmapHeight);
    int targetResolution = (reqWidth * reqHeight);

    int sampleSize = 1;

    if (targetResolution == 0) {
      return sampleSize;
    }

    for (int i = 1; ((bitmapResolution / i) > targetResolution) || !checkBitmapFitsInMemory(bitmapResolution / sampleSize); i *= 2) {
      sampleSize = i;
    }

    return sampleSize;
  }

  public static boolean checkBitmapFitsInMemory(long reqsize){
    long allocNativeHeap = Debug.getNativeHeapAllocatedSize();

    final long heapPad=(long) Math.max(4*1024*1024,Runtime.getRuntime().maxMemory()*0.1);

    // Bitmaps don't use native heap on honeycomb and above.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
    if (((reqsize*6) + allocNativeHeap + heapPad) >= Runtime.getRuntime().maxMemory())
      return false;
    } else {
       if (reqsize*6 >= Runtime.getRuntime().freeMemory())
        return false;
    }
    return true;

  }
}