import java.math.BigDecimal;
import java.util.Locale;

/**
 * ============================================================
 *  CLASS: BankAccount  (Tài khoản ngân hàng)
 * ============================================================
 * Vai trò: lưu thông tin MỘT tài khoản. Mỗi tài khoản thuộc đúng
 *          MỘT AccountType (thuộc tính accountType là tham chiếu
 *          tới đối tượng AccountType, KHÔNG phải chỉ lưu mã).
 *
 * Implements Comparable<BankAccount> để có thể dùng Collections.sort()
 * (đa hình: sort gọi compareTo của chính lớp này).
 */
public class BankAccount implements Comparable<BankAccount> {

    // ---------------- Thuộc tính (private) ----------------
    private String accountID = "";          // Mã tài khoản, duy nhất, ví dụ "A01"
    private AccountType accountType;        // Loại tài khoản (liên kết N-1)
    private String ownerName = "";          // Tên chủ tài khoản
    private String accountNumber = "";      // Số tài khoản
    private double balance;                 // Số dư, BẮT BUỘC >= 0

    // ---------------- Constructor ----------------

    /** Constructor mặc định. */
    public BankAccount() { }

    /** Constructor đầy đủ: gọi setter để dữ liệu luôn được kiểm tra. */
    public BankAccount(String accountID, AccountType accountType, String ownerName,
                       String accountNumber, double balance) {
        setAccountID(accountID);
        setAccountType(accountType);
        setOwnerName(ownerName);
        setAccountNumber(accountNumber);
        setBalance(balance);
    }

    /** Từ chối chuỗi null/rỗng; trả về chuỗi đã trim(). */
    private static String requireText(String v, String field) {
        if (v == null || v.trim().isEmpty())
            throw new IllegalArgumentException(field + " must not be empty");
        return v.trim();
    }

    // ---------------- Getter ----------------
    public String getAccountID()          { return accountID; }
    public AccountType getAccountType()   { return accountType; }
    public String getOwnerName()          { return ownerName; }
    public String getAccountNumber()      { return accountNumber; }
    public double getBalance()            { return balance; }

    // ---------------- Setter (có kiểm tra ràng buộc) ----------------
    public void setAccountID(String v)     { this.accountID = requireText(v, "accountID"); }
    public void setOwnerName(String v)     { this.ownerName = requireText(v, "ownerName"); }
    public void setAccountNumber(String v) { this.accountNumber = requireText(v, "accountNumber"); }

    /** Mỗi BankAccount bắt buộc phải có AccountType (không được null). */
    public void setAccountType(AccountType t) {
        if (t == null) throw new IllegalArgumentException("accountType must not be null");
        this.accountType = t;
    }

    /** Ràng buộc đề bài: balance >= 0 (viết !(v >= 0) để chặn cả NaN). */
    public void setBalance(double v) {
        if (!(v >= 0) || Double.isInfinite(v))
            throw new IllegalArgumentException("balance must not be negative");
        this.balance = v;
    }

    // ---------------- Chuyển đổi thành chuỗi ----------------

    /**
     * Dạng chuỗi dùng để GHI RA FILE bankaccounts.txt:
     *     accountID, accountTypeID, ownerName, accountNumber, balance
     * Lưu ý: ghi MÃ của AccountType (accountTypeID), không ghi cả đối tượng.
     */
    @Override
    public String toString() {
        String typeID = (accountType == null) ? "" : accountType.getAccountTypeID();
        return accountID + ", " + typeID + ", " + ownerName + ", " + accountNumber + ", "
                + BigDecimal.valueOf(balance).stripTrailingZeros().toPlainString();
    }

    /** Dòng tiêu đề của bảng khi in BankAccount ra màn hình. */
    public static String screenHeader() {
        return String.format("%-6s | %-10s | %-16s | %-12s | %18s", "ID", "TypeName", "Owner", "AccountNo", "Balance");
    }

    /** Dạng chuỗi HIỂN THỊ ra màn hình: canh cột, số dư có dấu phẩy ngăn cách hàng nghìn. */
    public String screenString() {
        String name = (accountType == null) ? "" : accountType.getTypeName();
        String cur = (accountType == null) ? "" : accountType.getCurrency();
        return String.format(Locale.US, "%-6s | %-10s | %-16s | %-12s | %,14.2f %-3s",
                accountID, name, ownerName, accountNumber, balance, cur);
    }

    // ---------------- So sánh để sắp xếp ----------------

    /**
     * Quy tắc sắp xếp theo đề bài:
     *   1) Theo typeName tăng dần (không phân biệt hoa/thường).
     *   2) Nếu typeName giống nhau -> theo accountID tăng dần.
     * Trả về: <0 nếu this đứng trước other, 0 nếu bằng nhau, >0 nếu đứng sau.
     * Dùng compareToIgnoreCase (không dùng ==) để so sánh nội dung chuỗi.
     */
    @Override
    public int compareTo(BankAccount other) {
        int c = this.accountType.getTypeName().compareToIgnoreCase(other.accountType.getTypeName());
        if (c != 0) return c;                                   // typeName khác nhau -> có kết quả ngay
        return this.accountID.compareToIgnoreCase(other.accountID);  // trùng typeName -> xét accountID
    }
}
