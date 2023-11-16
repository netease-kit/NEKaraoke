// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.pitch.ui.util;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.webp.decoder.WebpDrawable;
import com.bumptech.glide.integration.webp.decoder.WebpDrawableTransformation;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class ImageLoader {
  public static void loadImg(String url, ImageView imageView) {
    Glide.with(ApplicationWrapper.getNewApplication()).load(url).into(imageView);
  }

  public static void loadWebpImg(
      int resId, ImageView imageView, WebpAnimationPlayEndCallback callback) {
    Transformation<Bitmap> transformation = new CenterCrop();
    Glide.with(ApplicationWrapper.getNewApplication())
        .load(resId)
        .optionalTransform(WebpDrawable.class, new WebpDrawableTransformation(transformation))
        .addListener(
            new RequestListener<Drawable>() {
              @Override
              public boolean onLoadFailed(
                  @Nullable GlideException e,
                  Object model,
                  Target<Drawable> target,
                  boolean isFirstResource) {
                return false;
              }

              @Override
              public boolean onResourceReady(
                  Drawable resource,
                  Object model,
                  Target<Drawable> target,
                  DataSource dataSource,
                  boolean isFirstResource) {
                WebpDrawable webpDrawable = (WebpDrawable) resource;
                //需要设置为循环1次才会有onAnimationEnd回调
                webpDrawable.setLoopCount(1);
                webpDrawable.registerAnimationCallback(
                    new Animatable2Compat.AnimationCallback() {
                      @Override
                      public void onAnimationStart(Drawable drawable) {
                        super.onAnimationStart(drawable);
                      }

                      @Override
                      public void onAnimationEnd(Drawable drawable) {
                        super.onAnimationEnd(drawable);
                        webpDrawable.unregisterAnimationCallback(this);
                        callback.onAnimationEnd();
                      }
                    });
                return false;
              }
            })
        .into(imageView);
  }

  public interface WebpAnimationPlayEndCallback {
    void onAnimationEnd();
  }
}
