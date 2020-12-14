import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ChatClient extends Thread{
    private Socket socket;
    private int number;

    public ChatClient(Socket socket, int number){
        this.socket = socket;
        this.number = number;
    }

    @Override
    public void run() {
        try {
            socket.getOutputStream().write(("Hello client" + number).getBytes(StandardCharsets.UTF_8));
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
