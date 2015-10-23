package com.promobile.vod.vodmobile.vodplayer;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.SeekBar;

import com.google.android.exoplayer.DefaultLoadControl;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.ExoPlayerLibraryInfo;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.SampleSource;
import com.google.android.exoplayer.VideoSurfaceView;
import com.google.android.exoplayer.chunk.ChunkSampleSource;
import com.google.android.exoplayer.chunk.ChunkSource;
import com.google.android.exoplayer.chunk.FormatEvaluator;
import com.google.android.exoplayer.dash.DashChunkSource;
import com.google.android.exoplayer.dash.mpd.MediaPresentationDescription;
import com.google.android.exoplayer.dash.mpd.MediaPresentationDescriptionParser;
import com.google.android.exoplayer.dash.mpd.Representation;
import com.google.android.exoplayer.source.DefaultSampleSource;
import com.google.android.exoplayer.source.FrameworkSampleExtractor;
import com.google.android.exoplayer.upstream.BufferPool;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.UriDataSource;
import com.google.android.exoplayer.util.ManifestFetcher;
import com.promobile.vod.vodmobile.vodplayer.evaluator.AdaptechEvaluator;
import com.promobile.vod.vodmobile.vodplayer.evaluator.FestiveEvaluator;
import com.promobile.vod.vodmobile.vodplayer.evaluator.chunkSource.AdapTechDashChunkSource;
import com.promobile.vod.vodmobile.vodplayer.evaluator.chunkSource.FestiveDashChunkSource;
import com.promobile.vod.vodmobile.vodplayer.mpd.MpdManager;
import java.io.IOException;
import java.util.List;

/**
 * Created by CRUZ JR, A.C.V. on 19/05/15.
 * Essa classe implementa um player para uso no projeto Video On Demand (VOD)
 */
public class VodPlayer {

    public void setAudioManifestFletcherCallback(ManifestFetcher.ManifestCallback<MediaPresentationDescription> audioManifestFletcherCallback) {
        this.audioManifestFletcherCallback = audioManifestFletcherCallback;
    }

    public interface VodPlayerListener {
        public abstract void onPrepared();

        public abstract void onLoadingError();
    }

    private final String TAG = "VodPlayer";
    private boolean debugMode = true;

    public static final int DEFAULT_BUFFER_SEGMENT_SIZE = 32 * 1024;
    private static final int DEFAULT_VIDEO_BUFFER_SEGMENTS = 200;

    public static final int MINIMAL_MODE = 0;
    public static final int BASIC_MODE = 1;
    public static final int DASH_MODE = 2;

    public static final int UNBUILD = -1;
    public static final int BUILDING = 0;
    public static final int LOADING = 1;
    public static final int READY = 2;
    public static final int PLAYING = 3;
    public static final int PAUSED = 4;

    public static final int ADAPTECH_EVALUATOR = 0;
    public static final int FESTIVE_EVALUATOR = 1;
    public static final int DEFAULT_EVALUATOR = 2;

    private Context context;

    /**
     * Indica o estado atual do player
     */
    private int playerStatus;

    /**
     * Número de renderizações ??
     */
    private int numRenderers;
    /**
     * URL do vídeo a ser exibido
     */
    private String url;
    /**
     * Uri da {@link #url} setada.
     */
    private Uri uri;

    /**
     * View onde será exibido o vídeo.
     */
    private VideoSurfaceView videoSurfaceView;

    /**
     * Para um ExoPlayer Mínimo
     */
    /**
     * Player
     */
    private ExoPlayer exoPlayer;
    private int playbackMode;
    private Surface surface;
    private SampleSource sampleSource;
    private MediaCodecVideoTrackRenderer videoTrackRenderer;
    private MediaCodecAudioTrackRenderer audioTrackRenderer;
    private SeekBar seekBar;

    /**
     * Para um ExoPlayer Dash
     */

    /**
     *
     */
    private Handler handler;
    private DefaultLoadControl loadControl;
    private DefaultBandwidthMeter bandwidthMeter;
    private DataSource videoDataSource, audioDataSource;
    private ChunkSource chunkSource;
    private ChunkSource audioChunkSource;
    private FormatEvaluator formatEvaluator;
    private int evaluationMode;
    private FormatEvaluator audioFormatEvaluator;
    private MpdManager mpdManager;
    private MpdManager audioMpdManager;
    private ChunkSampleSource videoChunkSampleSource;
    private ChunkSampleSource audioChunkSampleSource;
    private String userAgent;
    private ManifestFetcher<MediaPresentationDescription> manifestFetcher;
    private ManifestFetcher<MediaPresentationDescription> audioManifestFetcher;

    private int manifestReceive;

    /**
     * Listeners
     */
    /**
     *
     */
    private SurfaceHolder.Callback surfaceHolderCallback;
    private ExoPlayer.Listener exoPlayerListener;
    private DefaultBandwidthMeter.EventListener bandwidthMeterEventListener;
    private ChunkSampleSource.EventListener chunkSampleSourceEventListener;
    private MediaCodecVideoTrackRenderer.EventListener mediaCodecVideoTrackRendererListener;
    private ManifestFetcher.ManifestCallback<MediaPresentationDescription> manifestFletcherCallback;
    private ManifestFetcher.ManifestCallback<MediaPresentationDescription> audioManifestFletcherCallback;
    private VodPlayerListener vodPlayerListener;

    /**
     * Construtor único para criação da classe. Assim, VodPlayer funcionará em seu formato mínimo.
     *
     * @param context          Contexto da aplicação
     * @param videoSurfaceView View onde será exibido o vídeo.
     * @param numRenderers     Número de renderizações ??
     */
    public VodPlayer(Context context, VideoSurfaceView videoSurfaceView, int numRenderers) {
        this.context = context;
        this.videoSurfaceView = videoSurfaceView;
        this.numRenderers = numRenderers;
        this.vodPlayerListener = new DefaultVodPlayerListener();
        this.playerStatus = UNBUILD;
    }

    public void builderMinimalPlayer(SurfaceHolder.Callback surfaceHolderCallback, String url) {
        this.playerStatus = BUILDING;

        setUrl(url);
        setSurfaceHolderCallback(surfaceHolderCallback);
        setSurface();

        sampleSource = new DefaultSampleSource(new FrameworkSampleExtractor(context, uri, null), numRenderers);

        videoTrackRenderer = new MediaCodecVideoTrackRenderer(sampleSource, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        audioTrackRenderer = new MediaCodecAudioTrackRenderer(sampleSource);

        exoPlayer = ExoPlayer.Factory.newInstance(getNumRenderers());
        exoPlayer.prepare(videoTrackRenderer, audioTrackRenderer);

        exoPlayer.sendMessage(videoTrackRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);

        setPlaybackMode(MINIMAL_MODE);
        playerStatus = READY;
    }

    public void builderMinimalPlayer(String url) {
        builderMinimalPlayer(surfaceHolderCallback, url);
    }

    public void builderBasicPlayer(String url) {
        builderMinimalPlayer(surfaceHolderCallback, url);

        if(exoPlayerListener == null) {
            exoPlayerListener = new DefaultExoPlayerListener();
        }

        exoPlayer.addListener(exoPlayerListener);
        setPlaybackMode(BASIC_MODE);
    }

    public void builderDashPlayer(String mpdUrl) throws IOException {
        this.builderDashPlayer(mpdUrl, ADAPTECH_EVALUATOR);
    }

    public void builderDashPlayer(String mdpUrl, int evaluationMode) throws IOException {
        this.evaluationMode = evaluationMode;
        playerStatus = BUILDING;
        setUrl(mdpUrl);
        setSurface();

        if(handler == null) {
            handler = new Handler();
        }

        loadControl = new DefaultLoadControl(new BufferPool(DEFAULT_BUFFER_SEGMENT_SIZE));

        if(bandwidthMeterEventListener == null) {
            bandwidthMeterEventListener = new DefaultBandwidthMeterEventListener();
        }
        bandwidthMeter = new DefaultBandwidthMeter(handler, bandwidthMeterEventListener);

        //Configurando a renderização do vídeo
        videoDataSource = new UriDataSource(getUserAgent(), bandwidthMeter);

        if(evaluationMode == ADAPTECH_EVALUATOR) {
            formatEvaluator = new AdaptechEvaluator(bandwidthMeter);
        }
        else if(evaluationMode == FESTIVE_EVALUATOR) {
            formatEvaluator = new FestiveEvaluator(bandwidthMeter);
        } else if(evaluationMode == DEFAULT_EVALUATOR) {
            formatEvaluator = new FormatEvaluator.AdaptiveEvaluator(bandwidthMeter);
        }

        manifestFetcher = new ManifestFetcher<MediaPresentationDescription>(new MediaPresentationDescriptionParser(), "VodPlayer", mdpUrl, userAgent);

        if(manifestFletcherCallback == null) {
            manifestFletcherCallback = new DefaultManifestFetcherCallbackForMediaPresentationDescription();
        }
        manifestFetcher.singleLoad(handler.getLooper(), manifestFletcherCallback);

        //configurando a renderização do áudio
        audioDataSource = new UriDataSource(getUserAgent(), bandwidthMeter);

        audioFormatEvaluator = new AdaptechEvaluator(bandwidthMeter);

        audioManifestFetcher = new ManifestFetcher<MediaPresentationDescription>(new MediaPresentationDescriptionParser(), "VodPlayerAudio", mdpUrl, userAgent);

        if(audioManifestFletcherCallback == null) {
            audioManifestFletcherCallback = new DefaultAudioManifestFetcherCallbackForMediaPresentationDescription();
        }

        audioManifestFetcher.singleLoad(handler.getLooper(), audioManifestFletcherCallback);
        playerStatus = LOADING;
    }

    private void onManifestReceive() {
        if (manifestReceive == 0) {
            manifestReceive++;
        }
        else {
            try {
                List<Representation> videoRepresentationList = mpdManager.getVideoRepresentationList();
                List<Representation> audioRepresentationList = audioMpdManager.getAudioRepresentationList();

                if(evaluationMode == ADAPTECH_EVALUATOR) {
                    chunkSource = new AdapTechDashChunkSource(videoDataSource, formatEvaluator, videoRepresentationList);
                    audioChunkSource = new AdapTechDashChunkSource(audioDataSource, formatEvaluator, audioRepresentationList);
                }
                else if (evaluationMode == FESTIVE_EVALUATOR){
                    chunkSource = new FestiveDashChunkSource(videoDataSource, formatEvaluator, videoRepresentationList);
                    audioChunkSource = new FestiveDashChunkSource(audioDataSource, formatEvaluator, audioRepresentationList);
                } else if (evaluationMode == DEFAULT_EVALUATOR) {
                    chunkSource = new DashChunkSource(videoDataSource, formatEvaluator, videoRepresentationList);
                    audioChunkSource = new DashChunkSource(audioDataSource, formatEvaluator, audioRepresentationList);
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro em DashChunckSource: " + e.getLocalizedMessage() + " | " + e.getMessage());
            }
            Log.d(TAG, "DashChunckSource criado");
            videoChunkSampleSource = new ChunkSampleSource(chunkSource, loadControl, DEFAULT_VIDEO_BUFFER_SEGMENTS * DEFAULT_BUFFER_SEGMENT_SIZE, true);
            audioChunkSampleSource = new ChunkSampleSource(audioChunkSource, loadControl, DEFAULT_VIDEO_BUFFER_SEGMENTS * DEFAULT_BUFFER_SEGMENT_SIZE, true);

            if (mediaCodecVideoTrackRendererListener == null) {
                mediaCodecVideoTrackRendererListener = new DefaultMediaCodecVideoTrackRendererEventListener();
            }
            videoTrackRenderer = new MediaCodecVideoTrackRenderer(videoChunkSampleSource, null, true, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 0, null, handler, mediaCodecVideoTrackRendererListener, 50);
            audioTrackRenderer = new MediaCodecAudioTrackRenderer(audioChunkSampleSource);

            exoPlayer = ExoPlayer.Factory.newInstance(getNumRenderers());
            exoPlayer.prepare(videoTrackRenderer, audioTrackRenderer);

            if (exoPlayerListener == null) {
                exoPlayerListener = new DefaultExoPlayerListener();
            }
            exoPlayer.addListener(exoPlayerListener);
            exoPlayer.sendMessage(videoTrackRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);


            setPlaybackMode(DASH_MODE);

            playerStatus = READY;
            readyStateListener();

        }
    }

    private void readyStateListener() {
        if(evaluationMode == ADAPTECH_EVALUATOR && exoPlayer.getBufferedPosition() < 10000) {
            //Estado inicial de carregamento. Esperando Buffer Mínimo para iniciar
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    readyStateListener();;
                }
            }, 500);
        } else {
            //Iniciando reprodução
            vodPlayerListener.onPrepared();
        }
    }

    private void setSurface() {
        if(videoSurfaceView != null) {
            try {
                if(surfaceHolderCallback == null) {
                    surfaceHolderCallback = new DefaultSurfaceHolderCallback();
                }
                videoSurfaceView.getHolder().addCallback(surfaceHolderCallback);
                surface = videoSurfaceView.getHolder().getSurface();

                if(surface != null) {
                    makeDebugLog("Surface não é null!");
                }
                else {
                    makeDebugLog("Surface é null!");
                    throw new Exception("VideoSurfaceView não forneceu um Surface válido!");

                }
            }
            catch (Exception e) {
                makeErrorLog("VideoSurfaceView não é válida! Não foi possível utilizar o VideoSurfaceView fornecido.\nMensagem: " + e.getMessage());
            }
        }
        else {
            makeErrorLog("VideoSurfaceView fornecido é null.");
        }
    }

    public void setUrl(String url) {
        this.url = url;

        uri = Uri.parse(url);
    }

    public void setPlaybackMode(int playbackMode) {
        this.playbackMode = playbackMode;
    }

    public int getNumRenderers() {
        return numRenderers;
    }

    public ExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    private void makeErrorLog(String logMessage) {
        Log.e(TAG, logMessage);
    }

    private void makeDebugLog(String logMessage) {
        if(debugMode) {
            Log.d(TAG, logMessage);
        }
    }

    public void start() {
        if(playerStatus == READY) {
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.sendMessage(videoTrackRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
            playerStatus = PLAYING;
        }
        else if(playerStatus == PAUSED) {
            exoPlayer.setPlayWhenReady(true);
            playerStatus = PLAYING;
        }
    }

    public void pause() {
        if(playerStatus == PLAYING) {
            exoPlayer.setPlayWhenReady(false);
            playerStatus = PAUSED;
        }
    }

    public int getPlayerStatus() {
        return playerStatus;
    }

    public void setSeekBar(SeekBar seekBar) {
        setSeekBar(seekBar, new DefaultOnSeekBarChangeListener());
    }

    public void setSeekBar(SeekBar seekBar, SeekBar.OnSeekBarChangeListener onSeekBarChangeListener) {
        this.seekBar = seekBar;
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    public void setSurfaceHolderCallback(SurfaceHolder.Callback surfaceHolderCallback) {
        this.surfaceHolderCallback = surfaceHolderCallback;
    }

    public void setExoPlayerListener(ExoPlayer.Listener exoPlayerListener) {
        this.exoPlayerListener = exoPlayerListener;
    }

    public void setBandwidthMeterEventListener(DefaultBandwidthMeter.EventListener bandwidthMeterEventListener) {
        this.bandwidthMeterEventListener = bandwidthMeterEventListener;
    }

    public void setMediaCodecVideoTrackRendererListener(MediaCodecVideoTrackRenderer.EventListener mediaCodecVideoTrackRendererListener) {
        this.mediaCodecVideoTrackRendererListener = mediaCodecVideoTrackRendererListener;
    }

    public void setVodPlayerListener(VodPlayerListener vodPlayerListener) {
        this.vodPlayerListener = vodPlayerListener;
    }

    public void release() {
        exoPlayer.release();
    }

    public String getUserAgent() {
        if(userAgent == null) {
            setDefaultUserAgent();
        }
        return userAgent;
    }

    private void setDefaultUserAgent() {
        String versionName;
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "?";
        }

        String defaultUserAgent = "VodPlayer/" + versionName + " (Linux;Android " + Build.VERSION.RELEASE + ") " + "ExoPlayerLib/" + ExoPlayerLibraryInfo.VERSION;

        makeDebugLog("String userAgent = " + defaultUserAgent + ";");

        setUserAgent(defaultUserAgent);
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }


    /**
     * Esta classe implementa um padrão para SurfaceHolder.Callback
     */
    class DefaultSurfaceHolderCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            makeDebugLog("surfaceCreated executado.");
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            makeDebugLog("surfaceChanged executado: ( format = " + format + " | width = " + width + " | height" + height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            makeDebugLog("surfaceDestroyed executado.");
        }
    }

    /**
     * Esta classe implementa um padrão para ExoPlayer.Listener
     */
    class DefaultExoPlayerListener implements ExoPlayer.Listener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            makeDebugLog("onPlayerStateChanged executado. ( playWhenReady=[" + playWhenReady + "] | playBackState=[" + playbackState + "] )");
        }

        @Override
        public void onPlayWhenReadyCommitted() {
            makeDebugLog("onPlayWhenReadyCommitted executado.");
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            makeDebugLog("onPlayerError executado.");
        }
    }

    /**
     * Esta classe implementa um padrão para DefaultBandwidthMeter.EventListener
     */
    class DefaultBandwidthMeterEventListener implements DefaultBandwidthMeter.EventListener {
        @Override
        public void onBandwidthSample(int elapsedMs, long bytes, long bitrate) {
            makeDebugLog("onBandwidthSample executado.");
        }
    }

    /**
     * Esta classe implementa um padrão para ChunkSampleSource.EventListener
     */
    class DefaultChunkSampleSourceEventListener implements ChunkSampleSource.EventListener {
        @Override
        public void onLoadStarted(int sourceId, String formatId, int trigger, boolean isInitialization, int mediaStartTimeMs, int mediaEndTimeMs, long length) {
            makeDebugLog("onLoadStarted executado.");
        }

        @Override
        public void onLoadCompleted(int sourceId, long bytesLoaded) {
            makeDebugLog("onLoadCompleted executado.");
        }

        @Override
        public void onLoadCanceled(int sourceId, long bytesLoaded) {
            makeDebugLog("onLoadCanceled executado.");
        }

        @Override
        public void onUpstreamDiscarded(int sourceId, int mediaStartTimeMs, int mediaEndTimeMs, long bytesDiscarded) {
            makeDebugLog("onUpstreamDiscarded executado.");
        }

        @Override
        public void onUpstreamError(int sourceId, IOException e) {
            makeDebugLog("onUpstreamError executado.");
        }

        @Override
        public void onConsumptionError(int sourceId, IOException e) {
            makeDebugLog("onConsumptionError executado.");
        }

        @Override
        public void onDownstreamDiscarded(int sourceId, int mediaStartTimeMs, int mediaEndTimeMs, long bytesDiscarded) {
            makeDebugLog("onDownstreamDiscarded executado.");
        }

        @Override
        public void onDownstreamFormatChanged(int sourceId, String formatId, int trigger, int mediaTimeMs) {
            makeDebugLog("onDownstreamFormatChanged executado.");
        }
    }

    /**
     * Esta classe implementa um padrão para MediaCodecVideoTrackRenderer.EventListener
     */
    class DefaultMediaCodecVideoTrackRendererEventListener implements MediaCodecVideoTrackRenderer.EventListener {

        @Override
        public void onDroppedFrames(int count, long elapsed) {
            makeDebugLog("onDroppedFrames executado.");
        }

        @Override
        public void onVideoSizeChanged(int width, int height, float pixelWidthHeightRatio) {
            makeDebugLog("onVideoSizeChanged executado.");
        }

        @Override
        public void onDrawnToSurface(Surface surface) {
            makeDebugLog("onDrawnToSurface executado.");
        }

        @Override
        public void onDecoderInitializationError(MediaCodecTrackRenderer.DecoderInitializationException e) {
            makeDebugLog("onDecoderInitializationError executado.");
        }

        @Override
        public void onCryptoError(MediaCodec.CryptoException e) {
            makeDebugLog("onCryptoError executado.");
        }
    }

    class DefaultManifestFetcherCallbackForMediaPresentationDescription implements ManifestFetcher.ManifestCallback<MediaPresentationDescription> {
        @Override
        public void onManifest(String contentId, MediaPresentationDescription manifest) {
            makeDebugLog("onManifest executado.");
            mpdManager = new MpdManager(contentId, manifest);
            onManifestReceive();
        }

        @Override
        public void onManifestError(String contentId, IOException e) {
            makeDebugLog("onManifestError executado.");
            makeErrorLog("Erro em onManifest: " + e.getLocalizedMessage() + " | " + e.getMessage());
        }
    }

    class DefaultVodPlayerListener implements VodPlayerListener {
        @Override
        public void onPrepared() {
            Log.i(TAG, "onPrepared executado.");
        }

        @Override
        public void onLoadingError() {

        }
    }

    private class DefaultAudioManifestFetcherCallbackForMediaPresentationDescription implements ManifestFetcher.ManifestCallback<MediaPresentationDescription> {
        @Override
        public void onManifest(String contentId, MediaPresentationDescription manifest) {
            makeDebugLog("audio onManifest executado.");
            audioMpdManager = new MpdManager(contentId, manifest);
            onManifestReceive();
        }

        @Override
        public void onManifestError(String contentId, IOException e) {
            makeDebugLog("audio onManifestError executado.");
            makeErrorLog("Erro em audio onManifest: " + e.getLocalizedMessage() + " | " + e.getMessage());
            vodPlayerListener.onLoadingError();
        }
    }

    private class DefaultOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser) {
                exoPlayer.seekTo((long) (((float) progress / 100) * exoPlayer.getDuration()));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
}