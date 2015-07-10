/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer.demo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.Surface;
import android.view.View;

/**
 * This class is easy to switch between SurfaceView and TextureView;
 */
public class VideoSurfaceView extends /*SurfaceView */TextureView implements
  TextureView.SurfaceTextureListener ,
  SurfaceHolder.Callback {
  private static final String TAG = "VideoSurfaceView";
  private static final float MAX_ASPECT_RATIO_DEFORMATION_PERCENT = 0.01f;

  private float videoAspectRatio;

  private Surface surface = null;
  private Callback listener = null;
  private SurfaceHolder surfaceHolder = null;

  public interface Callback {
    public void surfaceCreated(Surface surface);
    public void surfaceChanged(int width, int height);
    public void surfaceDestroyed();
  };

  public VideoSurfaceView(Context context) {
    super(context);
    init(context);
  }

  public VideoSurfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context) {
    this.surface = null;
    this.listener = null;
    this.surfaceHolder = null;

    View baseView = this;
    if (baseView instanceof TextureView) {
      TextureView view = (TextureView) this;
      view.setSurfaceTextureListener(this);

    } else if (baseView instanceof SurfaceView) {
      SurfaceView view = (SurfaceView) baseView;
      surfaceHolder= view.getHolder();
      if (surfaceHolder != null) {
        surfaceHolder.addCallback(this);
      }
    }
  }

  public void addCallback(Callback listener) {
    this.listener = listener;
  }

  protected Surface getSurface() {
    return surface;
  }

  /**
   * Set the aspect ratio that this {@link VideoSurfaceView} should satisfy.
   *
   * @param widthHeightRatio The width to height ratio.
   */
  public void setVideoWidthHeightRatio(float widthHeightRatio, int degree) {
    if (this.videoAspectRatio != widthHeightRatio) {
      this.videoAspectRatio = widthHeightRatio;
      setRotationEx(degree);
      requestLayout();
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int degree = (int) getRotation();

    int width = (degree == 90 || degree == 270) ? getMeasuredHeight() :getMeasuredWidth();
    int height = (degree == 90 || degree == 270) ? getMeasuredWidth() :getMeasuredHeight();

    if (videoAspectRatio != 0) {
      float viewAspectRatio = (float) width / height;
      float aspectDeformation = videoAspectRatio / viewAspectRatio - 1;
      if (aspectDeformation > MAX_ASPECT_RATIO_DEFORMATION_PERCENT) {
        height = (int) (width / videoAspectRatio);
      } else if (aspectDeformation < -MAX_ASPECT_RATIO_DEFORMATION_PERCENT) {
        width = (int) (height * videoAspectRatio);
      }
    }

    setMeasuredDimension(width, height);
  }
  //For TextureView
  private void setRotationEx(int degree) {
    if (this instanceof TextureView) {
      this.setRotation(degree);
    }
  }

  private float getRotationEx() {
    if (this instanceof TextureView) {
      return this.getRotation();
    }
    return 0f;
  }

  @Override
  public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
    this.surface = new Surface(surface);

    if (listener != null) {
      listener.surfaceCreated(this.surface);
    }
  }

  @Override
  public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    if (listener != null) {
      listener.surfaceChanged(width, height);
    }
  }

  @Override
  public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
    surface = null;
    if (listener != null) {
      listener.surfaceDestroyed();
    }
    return true;
  }

  @Override
  public void onSurfaceTextureUpdated(SurfaceTexture surface) {

  }

  //For SurfaceView
  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    if (listener != null) {
      listener.surfaceChanged(width, height);
    }
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    if (listener != null) {
      surfaceHolder = holder;
      surface = holder.getSurface();
      listener.surfaceCreated(surface);
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    surfaceHolder = null;
    surface = null;
    if (listener != null) {
      listener.surfaceDestroyed();
    }
  }
}
