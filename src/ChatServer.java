import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

//TODO: 后加入的客户端收不到服务器状态消息
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
    private static boolean monitorCreated = false;

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

                // 发送客户端编号
                if(socket != null){
                    for (Map.Entry<Integer, Socket> entry: treeMap.entrySet()){
                        if(entry.getKey() == number){
                            PrintStream printStream = getPrintStream(entry.getValue());
                            printStream.println(number);
                        }
                    }
                }

                // 服务器自行打印消息
                System.out.println("客户端" + number + "已上线");
                System.out.println("目前在线人数: " + treeMap.size());

                // 只需要一个线程来发送消息
                if(sendCreated == false){
                    new Thread(new sendThread(socket)).start();
                    new Thread(new userMonitor()).start();
                    sendCreated = true;
                    monitorCreated = true;
                }

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintStream printStream = new PrintStream(socket.getOutputStream());

                while(true){
                    String line = bufferedReader.readLine();

                    // 处理无意义输入
                    if(line.length() == 1){
                        printStream.println("无意义输入，请检查");
                        System.out.println("客户端" + Integer.parseInt(line.split("")[0])+ "发送了一条空消息");
                        continue;
                    }

                    // 客户端从在线列表中移除条件1：客户端主动关闭连接
                    if (line.length() >= 5 && "over".equals(line.substring(line.length()-4))){
                        int waitToRemove = Integer.parseInt(line.split("")[0]);
                        treeMap.remove(waitToRemove);
                        synchronized ((Object) number){
                            number--;
                        }
                        System.out.println("客户端" + waitToRemove + "离线");
                        System.out.println("目前在线人数: " + treeMap.size());
                        printStream.println("再见");

                        break;
                    }
                    System.out.println("客户端" + Integer.parseInt(line.split("")[0]) + "说：" + line.substring(1));
                }
            } catch (IOException e) {
                e.printStackTrace();
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
                    int choice;
                    try {
                        choice = Integer.parseInt(readfromBuffer());
                    }catch (NumberFormatException nfe){
                        nfe.printStackTrace();
                        continue;
                    }
                    switch (choice){
                        case 1:
                            System.out.println("请输入全体消息");
                            String msg = readfromBuffer();
                            if(msg != null && msg.length() != 0){
                                broadcastAll(msg);
                            }
                            continue;
                        case 2:
                            System.out.println("请输入想要发送的客户端编号");
                            int msgTo = Integer.parseInt(readfromBuffer());
                            System.out.print("请输入消息内容：");
                            String msg1 = readfromBuffer();
                            if(msg1 != null && msg1.length() != 0){
                                if(treeMap.containsKey(msgTo)){
                                    sendToPrivate(msgTo, msg1);
                                }
                            }
                            continue;
                    }
                }
            }
        }
    }

    // 用户数量监视及广播通知线程（时间到了过后用户数量没有变化不通知）
    private static class userMonitor implements Runnable{
        int initialUser = 0;
        @Override
        public void run() {
            while (true){
                if(人数没有变化){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else{
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

    // 服务器私聊消息
    private static void sendToPrivate(int msgTo, String msg){
        if(!treeMap.isEmpty()){
            for (Map.Entry<Integer, Socket> entry: treeMap.entrySet()){
                if(msgTo == entry.getKey()){
                    try {
                        PrintStream printStream = new PrintStream(entry.getValue().getOutputStream());
                        printStream.println(msg);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // 使用独立方法只用处理一次异常
    private static String readfromBuffer(){
        try {
            return inputReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 创建PrintStream
    private static PrintStream getPrintStream(Socket socket){
        try {
            PrintStream printStream = new PrintStream(socket.getOutputStream());
            return printStream;
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
