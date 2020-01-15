package com.github.serezhka.jap2s.receiver.handler;

import com.github.serezhka.jap2s.receiver.AirPlayReceiver;
import com.github.serezhka.jap2s.receiver.handler.mirroring.MirrorDataConsumer;
import com.github.serezhka.jap2s.receiver.handler.mirroring.MirroringHandler;
import com.github.serezhka.jap2s.receiver.handler.session.Session;
import com.github.serezhka.jap2s.receiver.handler.session.SessionManager;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.rtsp.RtspMethods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@ChannelHandler.Sharable
public class RTSPHandler extends AirTunesHandler {

    @Value("${airplay.port}")
    private int airPlayPort;

    @Value("${airtunes.port}")
    private int airTunesPort;

    private final MirrorDataConsumer mirrorDataConsumer;

    @Autowired
    protected RTSPHandler(SessionManager sessionManager,
                          MirrorDataConsumer mirrorDataConsumer) {
        super(sessionManager);
        this.mirrorDataConsumer = mirrorDataConsumer;
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
