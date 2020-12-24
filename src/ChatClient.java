import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class ChatClient extends Thread{
    static Socket socket;
    static boolean nameReceived = false;
    static int number;

    static {
        try {
            socket = new Socket(InetAddress.getLocalHost(), 9999);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class sendThread implements Runnable{
        @Override
        public void run() {
            try {
                while(true){
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                    PrintStream printStream = new PrintStream(socket.getOutputStream());
                    System.out.println("请输入要发送的信息，如需私聊，请以字母p开头后跟需要私聊的客户端编号：");
                    String line = bufferedReader.readLine();
                    printStream.println(number + line);
                    if(line.length() >= 4 && "over".equals(line.substring(line.length() - 4))){
                        System.exit(0);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class readThread implements Runnable{
        @Override
        public void run() {

            // 接收客户端编号
            if(!nameReceived){
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line = bufferedReader.readLine();
                    if(line.length() >= 5){
                        number = Integer.parseInt(line.split("")[5]);
                        nameReceived = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // 接收服务器和私聊消息
            try {
                while(true){
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line = bufferedReader.readLine();
                    if("服务器".equals(line.substring(0,4))){
                        System.out.println(line.substring(0,6) + line.substring(6));
                    }else{
                        System.out.println(line.substring(0,9) + line.substring(9));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new Thread(new readThread()).start();
        new Thread(new sendThread()).start();
    }
}
