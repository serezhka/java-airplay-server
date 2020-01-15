package com.github.serezhka.jap2s.receiver.handler;

import com.github.serezhka.jap2s.receiver.handler.session.Session;
import com.github.serezhka.jap2s.receiver.handler.session.SessionManager;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
public class FairPlayHandler extends AirTunesHandler {

    @Autowired
    public FairPlayHandler(SessionManager sessionManager) {super(sessionManager);}

    @Override
    protected boolean handleRequest(ChannelHandlerContext ctx, Session session, FullHttpRequest request) throws Exception {
        var uri = request.uri();
        if ("/fp-setup".equals(uri)) {
            var response = createResponseForRequest(request);
            session.getAirPlay().fairPlaySetup(new ByteBufInputStream(request.content()),
                    new ByteBufOutputStream(response.content()));
            return sendResponse(ctx, request, response);
        }
        return false;
    }
}
