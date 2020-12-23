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
                    String line = bufferedReader.readLine();
                    printStream.println(number + line);
                    if((number+"over").equals(line)){
                        socket.close();
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
                    if(line.length() != 0 && line != null){
                        number = Integer.parseInt(line.split("")[0]);
                        nameReceived = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // 接收服务器消息
            try {
                while(true){
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line = bufferedReader.readLine();
                    System.out.println("服务器说：" + line);
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
