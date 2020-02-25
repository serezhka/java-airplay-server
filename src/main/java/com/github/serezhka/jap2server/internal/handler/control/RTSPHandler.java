package com.github.serezhka.jap2server.internal.handler.control;

import com.github.serezhka.jap2server.MirrorDataConsumer;
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

import java.nio.charset.StandardCharsets;

@ChannelHandler.Sharable
public class RTSPHandler extends ControlHandler {

    private final MirrorDataConsumer mirrorDataConsumer;
    private final int airPlayPort;
    private final int airTunesPort;

    public RTSPHandler(int airPlayPort, int airTunesPort, SessionManager sessionManager,
                       MirrorDataConsumer mirrorDataConsumer) {
        super(sessionManager);
        this.mirrorDataConsumer = mirrorDataConsumer;
        this.airPlayPort = airPlayPort;
        this.airTunesPort = airTunesPort;
    }

    @Override
    protected boolean handleRequest(ChannelHandlerContext ctx, Session session, FullHttpRequest request) throws Exception {
        var response = createResponseForRequest(request);
        if (RtspMethods.SETUP.equals(request.method())) {
            session.getAirPlay().rtspSetup(new ByteBufInputStream(request.content()),
                    new ByteBufOutputStream(response.content()), airPlayPort, airTunesPort, 7011, 4998, 4999);

            if (session.getAirPlay().isFairPlayReady() && session.getAirPlayReceiverThread() == null) {
                var mirroringHandler = new MirroringHandler(session.getAirPlay(), mirrorDataConsumer);
                var airPlayReceiver = new MirroringReceiver(airPlayPort, mirroringHandler);
                var airPlayReceiverThread = new Thread(airPlayReceiver);
                session.setAirPlayReceiverThread(airPlayReceiverThread);
                airPlayReceiverThread.start();
                var audioHandler = new AudioHandler(session.getAirPlay());
                var audioReceiver = new AudioReceiver(audioHandler);
                new Thread(audioReceiver).start();
                var audioControlServer = new AudioControlServer();
                new Thread(audioControlServer).start();
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
            session.getAirPlayReceiverThread().interrupt();
            return sendResponse(ctx, request, response);
        }
        return false;
    }
}
