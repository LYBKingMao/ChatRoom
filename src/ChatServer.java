import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer extends Thread{
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(9999);
            int number = 1;
            while(true){
                Socket socket = serverSocket.accept();
                System.out.println("客户端"+number+"连接成功");
                new ChatClient(socket, number).start();
                number++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
