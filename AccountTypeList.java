import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * ============================================================
 *  CLASS: AccountTypeList  (Danh sách các AccountType)
 * ============================================================
 * Kế thừa ArrayList<AccountType> nên có sẵn add(), get(), size(),
 * isEmpty(), remove()... và được bổ sung các chức năng nghiệp vụ:
 * nạp/ghi file, tìm kiếm, thêm, cập nhật, liệt kê.
 */
public class AccountTypeList extends ArrayList<AccountType> {
    private static final long serialVersionUID = 1L;   // yêu cầu của Java khi kế thừa lớp Serializable

    public AccountTypeList() { super(); }

    // ============================================================
    //  1) NẠP DỮ LIỆU TỪ FILE
    // ============================================================

    /**
     * Đọc file accounttypes.txt, mỗi dòng dạng: ID, typeName, currency, interestRate
     *
     * Cách xử lý lỗi (không để chương trình sập):
     *   - File không tồn tại / không đọc được -> báo lỗi, trả về false.
     *   - Dòng rỗng                           -> bỏ qua.
     *   - Dòng sai số trường / sai kiểu số / vi phạm ràng buộc / trùng mã
     *       -> in rõ SỐ DÒNG và LÝ DO, bỏ qua dòng đó, các dòng khác vẫn nạp bình thường.
     *
     * @return true nếu đọc được file (kể cả khi có vài dòng lỗi), false nếu không mở được file.
     */
    public boolean loadFromFile(String filename) {
        clear();                       // xóa dữ liệu cũ để nạp lại từ đầu
        int lineNo = 0, errors = 0;
        // try-with-resources: BufferedReader tự động đóng dù có lỗi hay không
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filename), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                lineNo++;
                // File lưu từ Notepad có thể có ký tự BOM ở đầu file -> bỏ đi
                if (lineNo == 1 && line.startsWith("\uFEFF")) line = line.substring(1);
                if (line.trim().isEmpty()) continue;                 // bỏ qua dòng trống

                // split với limit -1 để giữ cả trường rỗng cuối dòng (ví dụ "a,b,,")
                String[] p = line.split(",", -1);
                if (p.length != 4) {                                 // AccountType phải có đúng 4 trường
                    System.out.println("  [" + filename + " line " + lineNo + "] expected 4 fields but found " + p.length);
                    errors++;
                    continue;
                }
                try {
                    // Constructor gọi setter nên tự kiểm tra: rỗng, interestRate <= 0
                    AccountType t = new AccountType(p[0], p[1], p[2], Double.parseDouble(p[3].trim()));
                    if (searchID(t.getAccountTypeID()) != -1) {      // trùng mã với dòng trước đó
                        System.out.println("  [" + filename + " line " + lineNo + "] duplicate accountTypeID '" + t.getAccountTypeID() + "'");
                        errors++;
                    } else {
                        add(t);
                    }
                } catch (NumberFormatException e) {
                    // phải bắt NumberFormatException TRƯỚC IllegalArgumentException
                    // vì nó là lớp con của IllegalArgumentException
                    System.out.println("  [" + filename + " line " + lineNo + "] interestRate is not a number: '" + p[3].trim() + "'");
                    errors++;
                } catch (IllegalArgumentException e) {
                    System.out.println("  [" + filename + " line " + lineNo + "] " + e.getMessage());
                    errors++;
                }
            }
        } catch (IOException e) {
            System.out.println("  Error: cannot read '" + filename + "' ("
                    + (e instanceof java.nio.file.NoSuchFileException ? "file not found" : e.getMessage()) + ")");
            return false;
        }
        System.out.println("Loaded " + size() + " AccountType(s) from " + filename
                + (errors > 0 ? " (" + errors + " bad line(s) skipped)" : ""));
        return true;
    }

    // ============================================================
    //  2) GHI DỮ LIỆU RA FILE  (menu option 5)
    // ============================================================

    /**
     * Ghi toàn bộ danh sách ra file, mỗi AccountType một dòng (dùng toString()).
     * Ghi đè nội dung cũ của file. try-with-resources tự đóng file.
     */
    public boolean saveToFile(String filename) {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_8))) {
            for (AccountType t : this) pw.println(t.toString());
            // PrintWriter không ném IOException, phải tự kiểm tra cờ lỗi
            if (pw.checkError()) throw new IOException("write failed");
            return true;
        } catch (IOException e) {
            System.out.println("  Error: cannot write '" + filename + "' (" + e.getMessage() + ")");
            return false;
        }
    }

    // ============================================================
    //  3) TÌM KIẾM  (menu option 3, và dùng nội bộ ở nhiều chỗ)
    // ============================================================

    /**
     * Tìm AccountType theo mã (không phân biệt hoa/thường).
     * @return chỉ số trong danh sách, hoặc -1 nếu không tìm thấy.
     * Dùng equalsIgnoreCase (không dùng ==) để so sánh NỘI DUNG chuỗi.
     */
    public int searchID(String id) {
        if (id == null) return -1;
        for (int i = 0; i < size(); i++)
            if (get(i).getAccountTypeID().equalsIgnoreCase(id.trim())) return i;
        return -1;
    }

    /**
     * Cho người dùng chọn một AccountType từ menu đánh số.
     * Dùng khi thêm / đổi loại cho BankAccount.
     * @return AccountType được chọn, hoặc null nếu danh sách rỗng.
     */
    public AccountType getUserChoice() {
        if (isEmpty()) {
            System.out.println("  There is no AccountType yet. Add one first (option 2).");
            return null;
        }
        System.out.println("Choose an AccountType:");
        return Menu.ref_getChoice(this);
    }

    // ============================================================
    //  4) THÊM MỚI  (menu option 2)
    // ============================================================

    /**
     * Nhập từng trường, mỗi trường kiểm tra ngay và hỏi lại nếu sai:
     *   - accountTypeID : không rỗng, KHÔNG trùng mã đã có.
     *   - typeName, currency : không rỗng.
     *   - interestRate : là số và > 0.
     */
    public void addAccountType() {
        System.out.println("--- Add a new AccountType ---");
        String id;
        while (true) {                                    // lặp tới khi mã chưa tồn tại
            id = Menu.readString("accountTypeID: ", null);
            if (searchID(id) == -1) break;
            System.out.println("  Error: accountTypeID '" + id + "' already exists.");
        }
        String name = Menu.readString("typeName: ", null);
        String cur = Menu.readString("currency: ", null);
        double rate = Menu.readDouble("interestRate (> 0, e.g. 0.045): ", true, null);
        add(new AccountType(id, name, cur, rate));
        System.out.println("AccountType added.");
    }

    // ============================================================
    //  5) CẬP NHẬT  (menu option 4)
    // ============================================================

    /**
     * Cập nhật AccountType theo mã. Nhấn Enter ở trường nào thì giữ nguyên trường đó.
     * Không cho đổi accountTypeID vì các BankAccount đang tham chiếu tới loại này.
     * Sửa trực tiếp trên đối tượng trong danh sách; các BankAccount cùng trỏ tới đối tượng
     * này nên tự động thấy thông tin mới (không cần cập nhật riêng).
     */
    public void updateAccountType() {
        System.out.println("--- Update AccountType ---");
        String id = Menu.readString("accountTypeID to update: ", null);
        int pos = searchID(id);
        if (pos == -1) {
            System.out.println("  AccountType '" + id + "' not found.");
            return;
        }
        AccountType t = get(pos);
        System.out.println("Current: " + t.screenString());
        System.out.println("(press Enter to keep the current value)");
        t.setTypeName(Menu.readString("typeName [" + t.getTypeName() + "]: ", t.getTypeName()));
        t.setCurrency(Menu.readString("currency [" + t.getCurrency() + "]: ", t.getCurrency()));
        t.setInterestRate(Menu.readDouble("interestRate [" + t.getInterestRate() + "]: ",
                true, t.getInterestRate()));
        System.out.println("Updated: " + t.screenString());
    }

    // ============================================================
    //  6) LIỆT KÊ  (menu option 1)
    // ============================================================

    /** In bảng toàn bộ AccountType; báo "no AccountType" nếu danh sách rỗng. */
    public void listAccountTypes() {
        if (isEmpty()) {
            System.out.println("  (no AccountType)");
            return;
        }
        System.out.println(AccountType.screenHeader());
        for (AccountType t : this) System.out.println(t.screenString());
    }
}
