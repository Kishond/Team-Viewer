package server;

public class ControlSession {
    private ClientHandler viewerHandler;
    private ClientHandler hostHandler;

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
}
