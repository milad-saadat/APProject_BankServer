
public class Account {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private int accountId;
    private int amount;

    public Account(String username, String password, String firstName, String lastName, int accountId) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accountId = accountId;
        this.amount = 0;
    }

    public String getUsername() {
        return username;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getPassword() {
        return password;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
