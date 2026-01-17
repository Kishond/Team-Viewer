package viewer;

import resources.*;;

public class NetworkReciever implements Runnable {
    private ServerProtocolReciever serverProtocol;
    private RemoteUpdateListener UI;

    public NetworkReciever(ServerProtocolReciever serverProtocol, RemoteUpdateListener UI) {
        this.serverProtocol = serverProtocol;
        this.UI = UI;
    }

    @Override
    public void run() {

    }
}
