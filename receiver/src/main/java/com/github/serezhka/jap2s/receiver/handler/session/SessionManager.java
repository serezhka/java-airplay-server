package com.github.serezhka.jap2s.receiver.handler.session;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SessionManager {

    private final Map<String, Session> sessions = new HashMap<>();

    public Session getSession(String activeRemote) {
        synchronized (sessions) {
            Session session;
            if ((session = sessions.get(activeRemote)) == null) {
                session = new Session();
                sessions.put(activeRemote, session);
            }
            return session;
        }
    }
}
