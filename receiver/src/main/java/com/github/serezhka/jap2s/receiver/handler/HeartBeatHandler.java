package com.github.serezhka.jap2s.receiver.handler;

import com.github.serezhka.jap2s.receiver.handler.session.Session;
import com.github.serezhka.jap2s.receiver.handler.session.SessionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
public class HeartBeatHandler extends AirTunesHandler {

    @Autowired
    protected HeartBeatHandler(SessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    protected boolean handleRequest(ChannelHandlerContext ctx, Session session, FullHttpRequest request) {
        // TODO /feedback
        var response = createResponseForRequest(request);
        return sendResponse(ctx, request, response);
    }
}
