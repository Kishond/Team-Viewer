package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;

    private final DataOutputStream outputStream;
    private final DataInputStream inputStream;

    private ClientType clientType = null;
    private boolean isRunning = false;
    private String SessionCode;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        this.inputStream = new DataInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        System.out.println("thread is running");
        this.isRunning = true;
        try {
            this.clientType = identification();
            switch (this.clientType) {
                case HOST:
                    
                    break;
                case VIEWER:

                    break;
                case NULL:
                    break;
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally  {
            closeEverything();
        }
    }

    private ClientType identification() throws IOException {
        String identification = this.inputStream.readUTF().toUpperCase();

        if (identification != ClientType.HOST.name() && identification != ClientType.VIEWER.name()) {
            sendMessage("identification has been failed :(");
            return ClientType.NULL;
        }
        sendMessage("enter desired session code");
        return identification == ClientType.HOST.name() ? ClientType.HOST : ClientType.VIEWER;
    }

    
    private void hostHandler() throws IOException {
        this.SessionCode = recieveSessionCodeFromHost();
        RelayServer.addAWaitingHost(SessionCode, this);
        while (true) {

        }

    }
    
    private String recieveSessionCodeFromHost() throws IOException {
        while (true) {
            String sessionCode = this.inputStream.readUTF();
            if (!RelayServer.isKeyInActiveSessions(sessionCode) && !RelayServer.isKeyInWaitingHosts(sessionCode)) {
                return sessionCode;
            }
            this.outputStream.writeUTF("Key is already in use, try using a different key");
        }
    }

    private void sendMessage(String msg) throws IOException {
        this.outputStream.writeUTF(msg);
    }
    
    private void closeEverything() {
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
        System.err.println("Error while closing: " + e.getMessage());
        }
    }

    public Socket getSocket() {
        return this.socket;
    }

    public DataInputStream getDataInputStream() {
        return this.inputStream;
    }

    public DataOutputStream getDataOutputStream() {
        return this.outputStream;
    }

    public ClientType getClientType() {
        return this.clientType;
    }

    public boolean isRunning() {
        return this.isRunning;
    }
}


enum ClientType {
    HOST,
    VIEWER,
    NULL;
}
