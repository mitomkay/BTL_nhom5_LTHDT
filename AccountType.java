import java.math.BigDecimal;
import java.util.Locale;

/**
 * ============================================================
 *  CLASS: AccountType  (Loại tài khoản: Savings, Checking, ...)
 * ============================================================
 * Vai trò: lưu thông tin MỘT loại tài khoản ngân hàng.
 * Quan hệ: nhiều BankAccount cùng trỏ tới một AccountType
 *          (BankAccount -> AccountType là quan hệ association N-1).
 *
 * Tính đóng gói (Encapsulation):
 *   - Các thuộc tính đều private, chỉ truy cập qua getter/setter.
 *   - Việc KIỂM TRA DỮ LIỆU hợp lệ nằm trong setter, nên dù dữ liệu
 *     đến từ bàn phím hay từ file thì đều đi qua cùng một luật.
 *     Dữ liệu sai => setter ném IllegalArgumentException.
 */
public class AccountType {

    // ---------------- Thuộc tính (private) ----------------
    private String accountTypeID = "";   // Mã loại, duy nhất, ví dụ "T01"
    private String typeName = "";        // Tên loại, ví dụ "Savings"
    private String currency = "";        // Đơn vị tiền tệ, ví dụ "VND"
    private double interestRate;         // Lãi suất, BẮT BUỘC > 0 (ví dụ 0.045 = 4.5%)

    // ---------------- Constructor ----------------

    /** Constructor mặc định: tạo đối tượng rỗng (chưa qua kiểm tra). */
    public AccountType() { }

    /**
     * Constructor đầy đủ tham số.
     * Gọi setter (không gán trực tiếp) để dữ liệu luôn được kiểm tra.
     */
    public AccountType(String accountTypeID, String typeName, String currency, double interestRate) {
        setAccountTypeID(accountTypeID);
        setTypeName(typeName);
        setCurrency(currency);
        setInterestRate(interestRate);
    }

    /**
     * Hàm tiện ích dùng chung cho setter kiểu String:
     * từ chối giá trị null/rỗng/chỉ có khoảng trắng và trả về chuỗi đã trim().
     */
    private static String requireText(String v, String field) {
        if (v == null || v.trim().isEmpty())
            throw new IllegalArgumentException(field + " must not be empty");
        return v.trim();
    }

    // ---------------- Getter ----------------
    public String getAccountTypeID() { return accountTypeID; }
    public String getTypeName()      { return typeName; }
    public String getCurrency()      { return currency; }
    public double getInterestRate()  { return interestRate; }

    // ---------------- Setter (có kiểm tra ràng buộc) ----------------
    public void setAccountTypeID(String v) { this.accountTypeID = requireText(v, "accountTypeID"); }
    public void setTypeName(String v)      { this.typeName = requireText(v, "typeName"); }
    public void setCurrency(String v)      { this.currency = requireText(v, "currency"); }

    /**
     * Ràng buộc đề bài: interestRate phải > 0.
     * Viết !(v > 0) thay vì v <= 0 để chặn luôn giá trị NaN
     * (NaN so sánh với bất kỳ số nào cũng ra false).
     */
    public void setInterestRate(double v) {
        if (!(v > 0) || Double.isInfinite(v))
            throw new IllegalArgumentException("interestRate must be greater than 0");
        this.interestRate = v;
    }

    // ---------------- Chuyển đổi thành chuỗi ----------------

    /**
     * Dạng chuỗi dùng để GHI RA FILE accounttypes.txt:
     *     accountTypeID, typeName, currency, interestRate
     * stripTrailingZeros + toPlainString giúp số in gọn, không ra dạng
     * khoa học (1.0E-4) và không thừa số 0 ("0.0450" -> "0.045").
     */
    @Override
    public String toString() {
        return accountTypeID + ", " + typeName + ", " + currency + ", "
                + BigDecimal.valueOf(interestRate).stripTrailingZeros().toPlainString();
    }

    /** Dòng tiêu đề của bảng khi in AccountType ra màn hình. */
    public static String screenHeader() {
        return String.format("%-8s | %-12s | %-8s | %s", "TypeID", "TypeName", "Currency", "InterestRate");
    }

    /**
     * Dạng chuỗi dùng để HIỂN THỊ ra màn hình (canh cột, lãi suất hiện theo %).
     * Locale.US để dấu thập phân luôn là '.', không phụ thuộc máy người dùng.
     */
    public String screenString() {
        return String.format(Locale.US, "%-8s | %-12s | %-8s | %.2f%%",
                accountTypeID, typeName, currency, interestRate * 100);
    }
}
