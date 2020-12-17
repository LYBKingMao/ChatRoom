import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

//TODO: 全体消息/私聊消息
public class ChatServer{
    private static List<Socket> socketList = Collections.synchronizedList(new ArrayList<>());

    private static class readThread implements Runnable{
        private Socket socket;
        public readThread(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                socketList.add(socket);
                System.out.println("客户端已连接");
                new Thread(new sendThread(socket)).start();
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
    }

    private static class sendThread implements Runnable{
        private Socket socket;
        public sendThread(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            Scanner scan = new Scanner(System.in);
            String welcome = "欢迎连接服务器";
            PrintStream printStream = null;
            try {
                printStream = new PrintStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            printStream.println(welcome);
            while(true){
                if(socket != null){
                    String msg = scan.nextLine();
                    if(msg == null || msg.length() == 0 ||"over".equals(msg)){
                        break;
                    }
                    printStream.println(msg);
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(9999);
            while (true){
                Socket socket = serverSocket.accept();
                socketList.add(socket);
                new Thread(new readThread(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
