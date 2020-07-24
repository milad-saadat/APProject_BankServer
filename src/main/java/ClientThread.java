import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
import java.util.Base64;

public class ClientThread extends Thread {
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private Socket socket;

    public ClientThread(Socket socket, String number) {
        try {
            this.socket = socket;
            dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream())) ;
            dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            dataOutputStream.writeUTF("hello " + number);
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true){
            try {
                String input = dataInputStream.readUTF();
                String[] details;
                String output = null ;
                if (input.matches("create_account \\S+ \\S+ \\S+ \\S+ \\S+")){
                    details = input.split(" ");
                    output = Database.getInstance().createAccount(details[1], details[2] ,details[3] , details[4] , details[5]);
                }
                else if (input.matches("get_token \\S+ \\S+")){
                    details = input.split(" ");
                    output = Database.getInstance().getToken(details[1] , details[2]);
                    System.out.println(new String(Base64.getDecoder().decode(output)));
                }
                else if (input.matches("exit")){
                    socket.close();
                    break;
                }
                else if (input.matches("get_balance \\S+")){
                    details = input.split(" ");
                    output = Database.getInstance().getBalance(details[1]);
                }
                else if (input.matches("pay \\d+")){
                    details = input.split(" ");
                    output = Database.getInstance().payReceiptById(Integer.parseInt(details[1]));
                }
                else if (input.matches("create_receipt \\S+ \\S+ \\S+ \\S+ \\S+ \\S+")){
                    details = input.split(" ");
                    output = Database.getInstance().createReceipt(details[1] , details[2] , details[3] , details[4], details[5], details[6]);
                }
                else if (input.matches("get_transactions \\S+ \\S")){
                    details = input.split(" ");
                    output = Database.getInstance().getTransAction(details[1], details[2]);
                }
                else {
                    output = "invalid input";
                }
                dataOutputStream.writeUTF(output);
                dataOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
