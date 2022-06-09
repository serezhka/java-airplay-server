package com.github.serezhka.jap2server.internal.handler.control;

import com.github.serezhka.jap2lib.rtsp.AudioStreamInfo;
import com.github.serezhka.jap2lib.rtsp.MediaStreamInfo;
import com.github.serezhka.jap2lib.rtsp.VideoStreamInfo;
import com.github.serezhka.jap2server.AirplayDataConsumer;
import com.github.serezhka.jap2server.internal.AudioControlServer;
import com.github.serezhka.jap2server.internal.AudioReceiver;
import com.github.serezhka.jap2server.internal.MirroringReceiver;
import com.github.serezhka.jap2server.internal.handler.audio.AudioHandler;
import com.github.serezhka.jap2server.internal.handler.mirroring.MirroringHandler;
import com.github.serezhka.jap2server.internal.handler.session.Session;
import com.github.serezhka.jap2server.internal.handler.session.SessionManager;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.rtsp.RtspMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

@ChannelHandler.Sharable
public class RTSPHandler extends ControlHandler {

    private static final Logger log = LoggerFactory.getLogger(RTSPHandler.class);

    private final AirplayDataConsumer airplayDataConsumer;
    private final int airPlayPort;
    private final int airTunesPort;

    public RTSPHandler(int airPlayPort, int airTunesPort, SessionManager sessionManager,
                       AirplayDataConsumer airplayDataConsumer) {
        super(sessionManager);
        this.airplayDataConsumer = airplayDataConsumer;
        this.airPlayPort = airPlayPort;
        this.airTunesPort = airTunesPort;
    }

    @Override
    protected boolean handleRequest(ChannelHandlerContext ctx, Session session, FullHttpRequest request) throws Exception {
        var response = createResponseForRequest(request);
        if (RtspMethods.SETUP.equals(request.method())) {

            MediaStreamInfo mediaStreamInfo = session.getAirPlay().rtspGetMediaStreamInfo(new ByteBufInputStream(request.content()));
            if (mediaStreamInfo == null) {
                request.content().resetReaderIndex();
                session.getAirPlay().rtspSetupEncryption(new ByteBufInputStream(request.content()));
            } else {
                switch (mediaStreamInfo.getStreamType()) {
                    case AUDIO:
                        AudioStreamInfo audioStreamInfo = (AudioStreamInfo) mediaStreamInfo;

                        log.info("Audio format is: {}", audioStreamInfo.getAudioFormat());
                        log.info("Audio compression type is: {}", audioStreamInfo.getCompressionType());
                        log.info("Audio samples per frame is: {}", audioStreamInfo.getSamplesPerFrame());

                        airplayDataConsumer.onAudioFormat(audioStreamInfo);

                        var audioHandler = new AudioHandler(session.getAirPlay(), airplayDataConsumer);
                        var audioReceiver = new AudioReceiver(audioHandler, this);
                        var audioReceiverThread = new Thread(audioReceiver);
                        session.setAudioReceiverThread(audioReceiverThread);
                        audioReceiverThread.start();
                        synchronized (this) {
                            wait();
                        }

                        var audioControlServer = new AudioControlServer(this);
                        var audioControlServerThread = new Thread(audioControlServer);
                        session.setAudioControlServerThread(audioControlServerThread);
                        audioControlServerThread.start();
                        synchronized (this) {
                            wait();
                        }

                        session.getAirPlay().rtspSetupAudio(new ByteBufOutputStream(response.content()),
                                audioReceiver.getPort(), audioControlServer.getPort());

                        break;

                    case VIDEO:
                        VideoStreamInfo videoStreamInfo = (VideoStreamInfo) mediaStreamInfo;

                        airplayDataConsumer.onVideoFormat(videoStreamInfo);

                        var mirroringHandler = new MirroringHandler(session.getAirPlay(), airplayDataConsumer);
                        var airPlayReceiver = new MirroringReceiver(airPlayPort, mirroringHandler);
                        var airPlayReceiverThread = new Thread(airPlayReceiver);
                        session.setAirPlayReceiverThread(airPlayReceiverThread);
                        airPlayReceiverThread.start();

                        session.getAirPlay().rtspSetupVideo(new ByteBufOutputStream(response.content()), airPlayPort, airTunesPort, 7011);
                        break;
                }
            }
            return sendResponse(ctx, request, response);
        } else if (RtspMethods.GET_PARAMETER.equals(request.method())) {
            byte[] content = "volume: 1.000000\r\n".getBytes(StandardCharsets.US_ASCII);
            response.content().writeBytes(content);
            return sendResponse(ctx, request, response);
        } else if (RtspMethods.RECORD.equals(request.method())) {
            response.headers().add("Audio-Latency", "11025");
            response.headers().add("Audio-Jack-Status", "connected; type=analog");
            return sendResponse(ctx, request, response);
        } else if (RtspMethods.SET_PARAMETER.equals(request.method())) {
            return sendResponse(ctx, request, response);
        } else if ("FLUSH".equals(request.method().toString())) {
            return sendResponse(ctx, request, response);
        } else if (RtspMethods.TEARDOWN.equals(request.method())) {
            MediaStreamInfo mediaStreamInfo = session.getAirPlay().rtspGetMediaStreamInfo(new ByteBufInputStream(request.content()));
            if (mediaStreamInfo != null) {
                switch (mediaStreamInfo.getStreamType()) {
                    case AUDIO:
                        session.stopAudio();
                        break;
                    case VIDEO:
                        session.stopMirroring();
                        break;
                }
            } else {
                session.stopAudio();
                session.stopMirroring();
            }
            return sendResponse(ctx, request, response);
        } else if ("POST".equals(request.method().toString()) && request.uri().equals("/audioMode")) {
            return sendResponse(ctx, request, response);
        }
        return false;
    }
}
