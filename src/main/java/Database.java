import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class Database {
    private ArrayList<Account> allAccounts;
    private ArrayList<Receipt> allReceipt;
    private static Database database = null;
    private static final long ExpireTime = 60 * 60 * 1000 ;
    public ArrayList<Account> getAllAccounts() {
        return allAccounts;
    }

    public void setAllAccounts(ArrayList<Account> allAccounts) {
        this.allAccounts = allAccounts;
    }

    public void setAllReceipt(ArrayList<Receipt> allReceipt) {
        this.allReceipt = allReceipt;
    }

    private Database() {
        Gson gson = new Gson();
        File file = new File("Data\\"+"Accounts"+".json");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Type type = new TypeToken<ArrayList<Account>>(){}.getType();
        allAccounts = gson.fromJson(br, type);

        gson = new Gson();
        file = new File("Data\\"+"Receipt"+".json");
        br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        type = new TypeToken<ArrayList<Receipt>>(){}.getType();
        allReceipt = gson.fromJson(br, type);
    }

    public static Database getInstance(){
        if (database == null)
            database = new Database();
        return database;
    }

    public synchronized String createAccount(String firstName , String lastName , String username , String password, String repeatedPassword){
        if (!password.equals(repeatedPassword)){
            return "passwords do not match";
        }
        if (getAccountByUsername(username) != null){
            return "username is not available";
        }
        int accountId = new Random().nextInt(8999)+1000;
        while (getAccountById(accountId) != null){
            accountId = new Random().nextInt(8999)+1000;
        }
        Account newAccount = new Account(username , password , firstName , lastName , accountId);
        allAccounts.add(newAccount);
        return String.valueOf(accountId);
    }

    public Account getAccountByUsername(String username){
        for (Account account : allAccounts) {
            if (account.getUsername().equals(username))
                return account;
        }
        return null;
    }

    public Account getAccountById(int Id){
        for (Account account : allAccounts) {
            if (account.getAccountId() == Id)
                return account;
        }
        return null;
    }

    public String getToken(String username, String password) {
        Account account = getAccountByUsername(username);
        if (account == null || !account.getPassword().equals(password))
            return "invalid username or password";
        return encode(username + "-" + new Date().getTime());
    }

    public String encode(String str){
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    public String getBalance(String token) {
        String res = decode(token);
        if (res.equals("token is invalid") || res.equals("token expired"))
            return res;
        Account account = getAccountByUsername(res);
        return String.valueOf(account.getAmount());
    }

    private String decode(String token) {
        token = new String(Base64.getDecoder().decode(token)) ;
        if (!token.matches("\\S+-\\d+")){
            return "token is invalid";
        }
        String username = token.split("-")[0];
        long time = Long.parseLong(token.split("-")[1]);
        if (getAccountByUsername(username) == null)
            return "token is invalid";
        if (time + ExpireTime < new Date().getTime()){
            return "token expired";
        }
        return username;
    }

    public String createReceipt(String token , String type , String money , String sourceId , String destId , String description){
        if (!type.equals("withdraw") && !type.equals("deposit") && !type.equals("move"))
            return "invalid receipt type";
        if (!money.matches("\\d+"))
            return "invalid money";
        String username = decode(token);
        if (username.equals("token is invalid") || username.equals("token expired"))
            return username;
        if (!sourceId.matches("(-1|\\d+)"))
            return "source account id is invalid";
        if (!destId.matches("(-1|\\d+)"))
            return "dest account id is invalid";
        if (sourceId.equals(destId))
            return "”equal source and dest account";

        if (type.equals("withdraw") || type.equals("move")){
            Account sourceAccount = getAccountById(Integer.parseInt(sourceId));
            if (sourceAccount == null)
                return "source account id is invalid";
            if (!username.matches(sourceAccount.getUsername()))
                return "token is invalid";
        }

        if (type.equals("deposit") || type.equals("move")){
            Account destAccount = getAccountById(Integer.parseInt(destId));
            if (destAccount == null)
                return "source account id is invalid";
        }
        if (!description.matches("[0-9A-Za-z]+"))
            return "your input contains invalid characters";
        int id = new Random().nextInt(10000);
        while (getReceiptById(id) != null){
            id = new Random().nextInt(10000);
        }
        Receipt receipt = new Receipt(type , Integer.parseInt(money) , Integer.parseInt(sourceId), Integer.parseInt(destId) , id , description);
        allReceipt.add(receipt);
        return String.valueOf(receipt.getReceiptId());
    }

    private Receipt getReceiptById(int id){
        for (Receipt receipt : allReceipt) {
            if (receipt.getReceiptId() == id)
                return receipt;
        }
        return null;
    }

    public synchronized String payReceiptById(int receiptId){
        Receipt receipt = getReceiptById(receiptId);
        if (receipt == null)
            return "invalid receipt id";
        return receipt.pay();
    }

    public String getTransAction(String token, String type) {
        if (token.matches("\\d+")){
            Receipt receipt = getReceiptById(Integer.parseInt(token));
            if (receipt == null){
                return "invalid receipt id";
            }
            Gson gson = new Gson();
            return gson.toJson(receipt);
        }
        String username = decode(token);
        if (username.equals("token is invalid") || username.equals("token expired"))
            return username;
        ArrayList<Receipt> res = new ArrayList<>();
        if (type.equals("*") || type.equals("-")){
            res.addAll(getAllOutReceipt(getAccountByUsername(username).getAccountId()));
        }
        if (type.equals("*") || type.equals("+")){
            res.addAll(getAllInReceipt(getAccountByUsername(username).getAccountId()));
        }
        Gson gson = new Gson();
        return gson.toJson(res);
    }

    private ArrayList<Receipt> getAllInReceipt(int accountId) {
        ArrayList<Receipt> res = new ArrayList<>();
        for (Receipt receipt : allReceipt) {
            if (receipt.getDestId() == accountId)
                res.add(receipt);
        }
        return res;

    }

    private ArrayList<Receipt> getAllOutReceipt(int accountId) {
        ArrayList<Receipt> res = new ArrayList<>();
        for (Receipt receipt : allReceipt) {
            if (receipt.getSourceId() == accountId)
                res.add(receipt);
        }
        return res;
    }

    public void writeDataOnfile() {
        File file = new File("Data\\" + "Accounts" + ".json");
        file.getParentFile().mkdirs();
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(file);
            Gson gson = new Gson();
            String json = gson.toJson(allAccounts);
            fileWriter.write(json);
            fileWriter.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        file = new File("Data\\" + "Receipt" + ".json");
        file.getParentFile().mkdirs();
        fileWriter = null;

        try {
            fileWriter = new FileWriter(file);
            Gson gson = new Gson();
            String json = gson.toJson(allReceipt);
            fileWriter.write(json);
            fileWriter.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
