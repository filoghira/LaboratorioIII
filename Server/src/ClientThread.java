package server;

import java.net.Socket;

public class ClientThread implements Runnable{
    private final Socket socket;

    public ClientThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

    }
}
