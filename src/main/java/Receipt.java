public class Receipt {
    private String type;
    private int money;
    private int sourceId;
    private int destId;
    private int receiptId;
    private String description;
    private boolean paid;

    public Receipt(String type, int money, int sourceId, int destId, int receiptId, String description) {
        this.type = type;
        this.money = money;
        this.sourceId = sourceId;
        this.destId = destId;
        this.receiptId = receiptId;
        this.description = description;
        this.paid = false;
    }

    public int getReceiptId() {
        return receiptId;
    }

    public int getSourceId() {
        return sourceId;
    }

    public int getDestId() {
        return destId;
    }

    public String pay(){
        if (paid)
            return "receipt is paid before";
        Account sourceAccount = Database.getInstance().getAccountById(sourceId);
        Account destAccount = Database.getInstance().getAccountById(destId);
        if (type.equals("deposit")){
            if (destAccount == null || sourceId != -1)
                return "invalid account id";
            destAccount.setAmount(destAccount.getAmount() + money);
            paid = true;
        }
        if (type.equals("withdraw")){
            if (sourceAccount == null || destId != -1)
                return "invalid account id";
            if (sourceAccount.getAmount() < money)
                return "source account does not have enough money";
            sourceAccount.setAmount(sourceAccount.getAmount() - money);
            paid = true;
        }
        if (type.equals("move")){
            if (sourceAccount == null || destAccount == null)
                return "invalid account id";
            if (sourceAccount.getAmount() < money)
                return "source account does not have enough money";
            sourceAccount.setAmount(sourceAccount.getAmount() - money);
            destAccount.setAmount(destAccount.getAmount() + money);
            paid = true;
        }
        return "done successfully";

    }
}
