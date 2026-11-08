import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ============================================================
 *  CLASS: BankAccountList  (Danh sách các BankAccount)
 * ============================================================
 * Kế thừa ArrayList<BankAccount>. Giữ thêm một tham chiếu tới AccountTypeList
 * để khi đọc file (chỉ có MÃ loại) hoặc khi thêm/sửa tài khoản có thể
 * tra cứu ra đối tượng AccountType tương ứng.
 */
public class BankAccountList extends ArrayList<BankAccount> {
    private static final long serialVersionUID = 1L;

    /** Danh sách loại tài khoản dùng để tra cứu (được truyền vào từ bên ngoài). */
    private AccountTypeList accountTypeList;

    public BankAccountList(AccountTypeList accountTypeList) {
        super();
        this.accountTypeList = accountTypeList;
    }

    /** In một thông báo lỗi chuẩn: tên file + số dòng + lý do. */
    private static void fileError(String filename, int lineNo, String msg) {
        System.out.println("  [" + filename + " line " + lineNo + "] " + msg);
    }

    // ============================================================
    //  1) NẠP DỮ LIỆU TỪ FILE
    // ============================================================

    /**
     * Đọc file bankaccounts.txt, mỗi dòng dạng:
     *     accountID, accountTypeID, ownerName, accountNumber, balance
     *
     * PHẢI nạp AccountTypeList trước, vì mỗi dòng chỉ ghi MÃ loại, cần tra ra
     * đối tượng AccountType để gắn vào BankAccount.
     *
     * Dòng bị bỏ qua (kèm báo số dòng + lý do) khi:
     *   - không đủ 5 trường;
     *   - accountTypeID không tồn tại trong AccountTypeList;
     *   - balance không phải số hoặc âm; trường văn bản rỗng;
     *   - accountID trùng với dòng đã nạp.
     */
    public boolean loadFromFile(String filename) {
        clear();
        int lineNo = 0, errors = 0;
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filename), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (lineNo == 1 && line.startsWith("\uFEFF")) line = line.substring(1);  // bỏ BOM
                if (line.trim().isEmpty()) continue;                                      // bỏ dòng trống

                String[] p = line.split(",", -1);
                if (p.length != 5) {
                    fileError(filename, lineNo, "expected 5 fields but found " + p.length);
                    errors++;
                    continue;
                }
                try {
                    // Tra cứu loại tài khoản theo mã ở cột thứ 2
                    int typePos = accountTypeList.searchID(p[1]);
                    if (typePos == -1) {
                        fileError(filename, lineNo, "accountType '" + p[1].trim() + "' does not exist in AccountTypeList");
                        errors++;
                        continue;
                    }
                    // Constructor tự kiểm tra: văn bản rỗng, balance < 0
                    BankAccount acc = new BankAccount(p[0], accountTypeList.get(typePos), p[2], p[3],
                            Double.parseDouble(p[4].trim()));
                    if (searchID(acc.getAccountID()) != -1) {       // trùng mã với dòng đã nạp
                        fileError(filename, lineNo, "duplicate accountID '" + acc.getAccountID() + "'");
                        errors++;
                    } else {
                        add(acc);
                    }
                } catch (NumberFormatException e) {                  // phải đứng trước IllegalArgumentException
                    fileError(filename, lineNo, "balance is not a number: '" + p[4].trim() + "'");
                    errors++;
                } catch (IllegalArgumentException e) {
                    fileError(filename, lineNo, e.getMessage());
                    errors++;
                }
            }
        } catch (IOException e) {
            System.out.println("  Error: cannot read '" + filename + "' ("
                    + (e instanceof java.nio.file.NoSuchFileException ? "file not found" : e.getMessage()) + ")");
            return false;
        }
        System.out.println("Loaded " + size() + " BankAccount(s) from " + filename
                + (errors > 0 ? " (" + errors + " bad line(s) skipped)" : ""));
        return true;
    }

    // ============================================================
    //  2) GHI DỮ LIỆU RA FILE  (menu option 11)
    // ============================================================

    /** Ghi toàn bộ BankAccount ra file, mỗi tài khoản một dòng (dùng toString()). */
    public boolean saveToFile(String filename) {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_8))) {
            for (BankAccount a : this) pw.println(a.toString());
            if (pw.checkError()) throw new IOException("write failed");
            return true;
        } catch (IOException e) {
            System.out.println("  Error: cannot write '" + filename + "' (" + e.getMessage() + ")");
            return false;
        }
    }

    // ============================================================
    //  3) TÌM KIẾM THEO MÃ
    // ============================================================

    /** Tìm BankAccount theo accountID (không phân biệt hoa/thường). Trả về chỉ số hoặc -1. */
    public int searchID(String id) {
        if (id == null) return -1;
        for (int i = 0; i < size(); i++)
            if (get(i).getAccountID().equalsIgnoreCase(id.trim())) return i;
        return -1;
    }

    // ============================================================
    //  4) THÊM MỚI  (menu option 8)
    // ============================================================

    /**
     * Nhập từng trường, kiểm tra ngay:
     *   - accountID : không rỗng, không trùng.
     *   - accountType : CHỌN từ danh sách AccountType có sẵn (không gõ tay -> không thể sai mã).
     *   - ownerName, accountNumber : không rỗng.
     *   - balance : là số và >= 0.
     * Nếu chưa có AccountType nào thì từ chối vì không thể tạo tài khoản không có loại.
     */
    public void addBankAccount() {
        System.out.println("--- Add a new BankAccount ---");
        if (accountTypeList.isEmpty()) {
            System.out.println("  There is no AccountType yet. Add one first (option 2).");
            return;
        }
        String id;
        while (true) {
            id = Menu.readString("accountID: ", null);
            if (searchID(id) == -1) break;
            System.out.println("  Error: accountID '" + id + "' already exists.");
        }
        AccountType type = accountTypeList.getUserChoice();
        String owner = Menu.readString("ownerName: ", null);
        String number = Menu.readString("accountNumber: ", null);
        double balance = Menu.readDouble("balance (>= 0): ", false, null);
        add(new BankAccount(id, type, owner, number, balance));
        System.out.println("BankAccount added.");
    }

    // ============================================================
    //  5) XÓA THEO MÃ  (menu option 9)
    // ============================================================

    /**
     * Tìm theo mã, hiện thông tin, hỏi xác nhận rồi mới xóa.
     * @return true nếu đã xóa; false nếu không tìm thấy hoặc người dùng hủy.
     */
    public boolean removeBankAccount() {
        System.out.println("--- Remove BankAccount ---");
        String id = Menu.readString("accountID to remove: ", null);
        int pos = searchID(id);
        if (pos == -1) {
            System.out.println("  BankAccount '" + id + "' not found.");
            return false;
        }
        System.out.println("Found: " + get(pos).screenString());
        if (!Menu.readYesNo("Remove this account?")) {
            System.out.println("Cancelled.");
            return false;
        }
        remove(pos);                       // remove(int) xóa theo chỉ số
        System.out.println("BankAccount removed.");
        return true;
    }

    // ============================================================
    //  6) CẬP NHẬT THEO MÃ  (menu option 10)
    // ============================================================

    /**
     * Cho sửa mọi trường, Enter = giữ giá trị cũ.
     * Quan trọng: thu thập và kiểm tra HẾT các giá trị mới TRƯỚC, rồi mới gán
     * vào đối tượng một lượt. Nhờ vậy nếu người dùng nhập dở thì dữ liệu cũ
     * không bị sửa nửa chừng.
     * Đổi accountID phải không trùng với tài khoản KHÁC (trùng với chính nó là được).
     */
    public boolean updateBankAccount() {
        System.out.println("--- Update BankAccount ---");
        String id = Menu.readString("accountID to update: ", null);
        int pos = searchID(id);
        if (pos == -1) {
            System.out.println("  BankAccount '" + id + "' not found.");
            return false;
        }
        BankAccount a = get(pos);
        System.out.println("Current: " + a.screenString());
        System.out.println("(press Enter to keep the current value)");

        // --- accountID: kiểm tra trùng với bản ghi KHÁC (other != pos) ---
        String newID;
        while (true) {
            newID = Menu.readString("accountID [" + a.getAccountID() + "]: ", a.getAccountID());
            int other = searchID(newID);
            if (other == -1 || other == pos) break;
            System.out.println("  Error: accountID '" + newID + "' already used by another account.");
        }
        // --- accountType: hỏi có đổi không, nếu có thì chọn từ menu ---
        AccountType newType = a.getAccountType();
        if (Menu.readYesNo("Change AccountType (currently " + newType.getTypeName() + ")?")) {
            AccountType chosen = accountTypeList.getUserChoice();
            if (chosen != null) newType = chosen;
        }
        // --- các trường còn lại ---
        String owner = Menu.readString("ownerName [" + a.getOwnerName() + "]: ", a.getOwnerName());
        String number = Menu.readString("accountNumber [" + a.getAccountNumber() + "]: ", a.getAccountNumber());
        double balance = Menu.readDouble("balance [" + a.getBalance() + "]: ", false, a.getBalance());

        // Tới đây mọi giá trị đều hợp lệ -> áp dụng vào đối tượng
        a.setAccountID(newID);
        a.setAccountType(newType);
        a.setOwnerName(owner);
        a.setAccountNumber(number);
        a.setBalance(balance);
        System.out.println("Updated: " + a.screenString());
        return true;
    }

    // ============================================================
    //  7) LIỆT KÊ / SẮP XẾP / TÌM THEO typeName
    // ============================================================

    /** Hàm dùng chung: in tiêu đề rồi từng dòng của một danh sách BankAccount. */
    private void printTable(List<BankAccount> rows) {
        System.out.println(BankAccount.screenHeader());
        for (BankAccount a : rows) System.out.println(a.screenString());
    }

    /** In toàn bộ tài khoản theo thứ tự hiện có (không sắp xếp). */
    public void listBankAccounts() {
        if (isEmpty()) {
            System.out.println("  (no BankAccount)");
            return;
        }
        printTable(this);
    }

    /**
     * In tài khoản đã sắp xếp theo typeName tăng dần, trùng thì theo accountID
     * (menu option 6). Sắp xếp trên BẢN SAO nên thứ tự trong danh sách gốc
     * (và file khi lưu) không bị thay đổi.
     * Collections.sort tự gọi BankAccount.compareTo() để so sánh.
     */
    public void printBasedAccountTypeName() {
        if (isEmpty()) {
            System.out.println("  (no BankAccount)");
            return;
        }
        ArrayList<BankAccount> sorted = new ArrayList<>(this);   // tạo bản sao
        Collections.sort(sorted);
        printTable(sorted);
    }

    /**
     * Tìm các tài khoản có typeName CHỨA từ khóa (menu option 7).
     * Đổi cả hai vế về chữ thường để không phân biệt hoa/thường,
     * ví dụ gõ "sav" vẫn ra "Savings". Kết quả cũng được sắp xếp.
     */
    public void searchByTypeName() {
        String kw = Menu.readString("Enter part of typeName: ", null).toLowerCase();
        ArrayList<BankAccount> found = new ArrayList<>();
        for (BankAccount a : this)
            if (a.getAccountType().getTypeName().toLowerCase().contains(kw)) found.add(a);
        if (found.isEmpty()) {
            System.out.println("  No BankAccount matches '" + kw + "'.");
            return;
        }
        Collections.sort(found);
        System.out.println(found.size() + " result(s):");
        printTable(found);
    }
}
