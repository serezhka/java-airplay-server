package com.github.serezhka.jap2server.internal.handler;

import com.github.serezhka.jap2server.MirrorDataConsumer;
import com.github.serezhka.jap2server.internal.AirPlayReceiver;
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
public class RTSPHandler extends AirTunesHandler {

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
                    new ByteBufOutputStream(response.content()), airPlayPort, airTunesPort, 7011);

            if (session.getAirPlay().isFairPlayReady()) {
                var mirroringHandler = new MirroringHandler(session.getAirPlay(), mirrorDataConsumer);
                var airPlayReceiver = new AirPlayReceiver(airPlayPort, mirroringHandler);
                var airPlayReceiverThread = new Thread(airPlayReceiver);
                session.setAirPlayReceiverThread(airPlayReceiverThread);
                airPlayReceiverThread.start();
            }
            return sendResponse(ctx, request, response);
        } else if (RtspMethods.GET_PARAMETER.equals(request.method())) {
            byte[] content = "volume: 0.000000\r\n".getBytes(StandardCharsets.US_ASCII);
            response.content().writeBytes(content);
            return sendResponse(ctx, request, response);
        } else if (RtspMethods.RECORD.equals(request.method())) {
            return sendResponse(ctx, request, response);
        } else if (RtspMethods.SET_PARAMETER.equals(request.method())) {
            return sendResponse(ctx, request, response);
        } else if (RtspMethods.TEARDOWN.equals(request.method())) {
            session.getAirPlayReceiverThread().interrupt();
            return sendResponse(ctx, request, response);
        }
        return false;
    }
}
