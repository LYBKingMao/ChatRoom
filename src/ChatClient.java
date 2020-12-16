import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class ChatClient extends Thread{
    static Socket socket;

    static {
        try {
            socket = new Socket(InetAddress.getLocalHost(), 9999);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        try {
            while(true){
                new receiveMsgThread().start();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                PrintStream printStream = new PrintStream(socket.getOutputStream());
                String line = bufferedReader.readLine();
                printStream.println(line);
                if("over".equals(line)){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class receiveMsgThread extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                while(true){
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line = bufferedReader.readLine();
                    if (line == null || line.length() == 0 || "over".equals(line)){
                        break;
                    }
                    System.out.println("服务器说：" + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.start();
    }
}
