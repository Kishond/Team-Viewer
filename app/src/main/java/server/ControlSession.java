package server;

public class ControlSession {
    private ClientHandler viewerHandler;
    private ClientHandler hostHandler;

    private String sessionKey = null;

    public ControlSession(ClientHandler viewerHandler, ClientHandler hostHandler) {
        this.viewerHandler = viewerHandler;
        this.hostHandler = hostHandler;
    }

    public ClientHandler getViewerHandler() {
        return this.viewerHandler;
    }

    public ClientHandler getHostHandler() {
        return this.hostHandler;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }
}
