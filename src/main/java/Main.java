
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.Date;
import java.util.Scanner;

public class Main {
    private static int connectedNumber = 1000;
    private static Scanner scanner = new Scanner(System.in);
    private static String createJWT() {

        String hello = "hell-19991-salam";
        return Base64.getEncoder().encodeToString(hello.getBytes());
    }

    public static void main(String[] args) throws IOException {
        System.out.println(createJWT());
        System.out.println(new String(Base64.getDecoder().decode(createJWT())));
        String port = scanner.nextLine();
        createServer(port);
    }

    private static void createServer(String port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port)) ;
        new Thread(new Runnable() {
            @Override
            public void run() {
                String line = scanner.nextLine();
                if (line.equals("exit")){
//                    try {
//                        serverSocket.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    Database.getInstance().writeDataOnfile();
                    System.exit(0);
                }
            }
        }).start();
        while (true){
            Socket socket = serverSocket.accept();
            new ClientThread(socket, String.valueOf(connectedNumber)).start();
            System.out.println(connectedNumber);
            connectedNumber++;
        }
    }
}
