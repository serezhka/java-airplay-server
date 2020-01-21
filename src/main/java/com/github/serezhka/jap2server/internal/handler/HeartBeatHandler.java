package com.github.serezhka.jap2server.internal.handler;

import com.github.serezhka.jap2server.internal.handler.session.Session;
import com.github.serezhka.jap2server.internal.handler.session.SessionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

@ChannelHandler.Sharable
public class HeartBeatHandler extends AirTunesHandler {

    public HeartBeatHandler(SessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    protected boolean handleRequest(ChannelHandlerContext ctx, Session session, FullHttpRequest request) {
        // TODO /feedback
        var response = createResponseForRequest(request);
        return sendResponse(ctx, request, response);
    }
}
