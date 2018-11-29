/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.google.android.exoplayer2.adsdemo;

import android.content.Context;
import android.net.Uri;
import android.view.ViewGroup;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.C.ContentType;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the {@link ExoPlayer}, the IMA plugin and all video playback.
 */
/* package */ final class PlayerManager implements AdsMediaSource.MediaSourceFactory {

  private final AdsLoader adsLoader;
  private final DataSource.Factory dataSourceFactory;

  private SimpleExoPlayer player;
  private long contentPosition;

  public PlayerManager(Context context) {
    adsLoader = new AdsLoader() {
      private List<String> supportedMimeTypes;

      @Override
      public void setSupportedContentTypes(int... contentTypes) {
        List<String> supportedMimeTypes = new ArrayList<>();
        for (@C.ContentType int contentType : contentTypes) {
          if (contentType == C.TYPE_DASH) {
            supportedMimeTypes.add(MimeTypes.APPLICATION_MPD);
          } else if (contentType == C.TYPE_HLS) {
            supportedMimeTypes.add(MimeTypes.APPLICATION_M3U8);
          }
        }
        this.supportedMimeTypes = Collections.unmodifiableList(supportedMimeTypes);
      }

      @Override
      public void attachPlayer(ExoPlayer player, EventListener eventListener,
          ViewGroup adUiViewGroup) {

      }

      @Override
      public void detachPlayer() {

      }

      @Override
      public void release() {

      }

      @Override
      public void handlePrepareError(int adGroupIndex, int adIndexInAdGroup,
          IOException exception) {

      }
    };

    dataSourceFactory =
        new DefaultDataSourceFactory(
            context, Util.getUserAgent(context, context.getString(R.string.application_name)));
  }

  public void init(Context context, PlayerView playerView) {
    // Create a default track selector.
    TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
    TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

    // Create a player instance.
    player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

    // Bind the player to the view.
    playerView.setPlayer(player);

    // This is the MediaSource representing the content media (i.e. not the ad).
    String contentUrl = context.getString(R.string.content_url);
    MediaSource contentMediaSource = buildMediaSource(Uri.parse(contentUrl));

    // Compose the content media source into a new AdsMediaSource with both ads and content.
    MediaSource mediaSourceWithAds =
        new AdsMediaSource(
            contentMediaSource,
            /* adMediaSourceFactory= */ this,
            adsLoader,
            playerView.getOverlayFrameLayout());

    // Prepare the player with the source.
    player.seekTo(contentPosition);
    player.prepare(contentMediaSource);
    player.setPlayWhenReady(true);
  }

  public void reset() {
    if (player != null) {
      contentPosition = player.getContentPosition();
      player.release();
      player = null;
    }
  }

  public void release() {
    if (player != null) {
      player.release();
      player = null;
    }
    adsLoader.release();
  }

  // AdsMediaSource.MediaSourceFactory implementation.

  @Override
  public MediaSource createMediaSource(Uri uri) {
    return buildMediaSource(uri);
  }

  @Override
  public int[] getSupportedTypes() {
    // IMA does not support Smooth Streaming ads.
    return new int[]{C.TYPE_DASH, C.TYPE_HLS};
  }

  // Internal methods.

  private MediaSource buildMediaSource(Uri uri) {
    @ContentType int type = Util.inferContentType(uri);
    switch (type) {
      case C.TYPE_DASH:
        return new DashMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
      case C.TYPE_HLS:
        return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
      default:
        throw new IllegalStateException("Unsupported type: " + type);
    }
  }
}
