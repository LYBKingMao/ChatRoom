import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

//TODO: 全体消息/私聊消息
public class ChatServer extends Thread{
    static ServerSocket serverSocket;
    static Socket socket;

    static {
        try {
            serverSocket = new ServerSocket(9999);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            socket = serverSocket.accept();
            System.out.println("客户端已连接");
            new sendMsgThread().start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while(true){
                String line = bufferedReader.readLine();
                if (line == null || line.length() == 0 || "over".equals(line)){
                    break;
                }
                System.out.println("客户端说：" + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    class sendMsgThread extends Thread{
        @Override
        public void run() {
            super.run();
            Scanner scan = new Scanner(System.in);
            PrintStream printStream = null;
            try {
                printStream = new PrintStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            while(true){
                if(socket != null){
                    String msg = scan.nextLine();
                    if(msg == null || msg.length() == 0 ||"over".equals(msg)){
                        break;
                    }
                    printStream.println(msg);
                }
            }
            scan.close();
            printStream.close();
        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.start();
    }
}
