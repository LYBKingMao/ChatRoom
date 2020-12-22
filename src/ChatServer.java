import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

//TODO：多个客户端默认以最后连接的客户端身份操作
//TODO: 上一个问题导致先连接的客户端与服务器连接不能关闭
//TODO: 客户端互相私聊/服务器私聊转发
public class ChatServer{

    private static Map<Integer, Socket> treeMap = new TreeMap<>(new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o1 - o2;
        }
    });
    private static BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
    private static int number = 0;
    private static boolean sendCreated = false;

    private static class readThread implements Runnable{
        private Socket socket;
        public readThread(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                synchronized ((Object)number){
                    number++;
                }

                treeMap.put(number, socket);

                // 服务器自行打印消息
                System.out.println("客户端" + number + "已上线");
                System.out.println("目前在线人数: " + treeMap.size());

                broadcastStatus();

                System.out.println("测试人数：" + treeMap.size());
                for (Map.Entry<Integer, Socket> entry : treeMap.entrySet()){
                    System.out.println(entry.getValue());
                }

                // create only one send thread
                if(sendCreated == false){
                    new Thread(new sendThread(socket)).start();
                    sendCreated = true;
                }

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintStream printStream = new PrintStream(socket.getOutputStream());

                while(true){
                    String line = bufferedReader.readLine();

                    // 处理无意义输入
                    if(line == null || line.length() == 0){
                        printStream.println("无意义输入，请检查");
                        System.out.println("客户端" + number + "发送了一条空消息");
                        continue;
                    }

                    // 客户端从在线列表中移除条件1：客户端主动关闭连接
                    if ("over".equals(line)){
                        treeMap.remove(number);
                        synchronized ((Object) number){
                            number--;
                        }
                        System.out.println("客户端" + number + "离线");
                        System.out.println("目前在线人数: " + treeMap.size());
                        printStream.println("再见");

                        broadcastStatus();

                        break;
                    }
                    System.out.println("客户端" + number + "说：" + line);
                }
            } catch (IOException e) {
                e.printStackTrace();

                // 客户端从在线列表中移除条件2：出现异常时将客户端移除
                treeMap.remove(number);
                synchronized ((Object) number){
                    number--;
                }
                System.out.println("客户端" + number + "掉线了");
                System.out.println("目前在线人数: " + treeMap.size());

                broadcastStatus();

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
            while(true){
                if(socket != null){
                    // 选择模式
                    System.out.println("请选择模式：1.全体消息 2.私聊消息: ");
                    int choice = Integer.parseInt(readfromBuffer());
                    switch (choice){
                        case 1:
                            System.out.println("请输入全体消息，退出请输入Q");
                            String msg = readfromBuffer();
                            if(msg.length() != 1 && !"Q".equals(msg.toUpperCase().split("")[0])){
                                broadcastAll(msg);
                            }
                            continue;
                        case 2:
                            System.out.println("请输入想要发送的客户端编号，退出请输入Q");
                            int msgTo = Integer.parseInt(readfromBuffer());
                            System.out.print("请输入消息内容：");
                            String msg1 = readfromBuffer();
                            if(msg1.length() != 1 && !"Q".equals(msg1.toUpperCase().split("")[0])){
                                if(treeMap.containsKey(msgTo)){
                                    broadcastPrivate(treeMap.get(msgTo), msg1);
                                }
                            }
                            continue;
                    }
                }
            }
        }
    }

    // 向全员广播聊天室状态
    private static void broadcastStatus(){
        if(!treeMap.isEmpty()){
            for (Map.Entry<Integer, Socket> entry: treeMap.entrySet()){
                PrintStream printStream = null;
                try {
                    printStream = new PrintStream(entry.getValue().getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                assert printStream != null;
                printStream.println("聊天室人数发生变化，目前在线人数: " + treeMap.size());
            }
        }
    }

    // 服务器全体消息
    private static void broadcastAll(String msg){
        if (!treeMap.isEmpty()){
            for (Map.Entry<Integer, Socket> entry: treeMap.entrySet()){
                try {
                    PrintStream printStream = new PrintStream(entry.getValue().getOutputStream());
                    printStream.println(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static void broadcastPrivate(Socket socket, String msg){
        try {
            PrintStream printStream = new PrintStream(socket.getOutputStream());
            printStream.println(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 处理一次异常
    private static String readfromBuffer(){
        try {
            return inputReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(9999);
            while (true){
                Socket socket = serverSocket.accept();
                new Thread(new readThread(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
