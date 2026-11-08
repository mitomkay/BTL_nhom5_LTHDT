/**
 * ============================================================
 *  CLASS: BankAccountManager  (Chương trình chính - có hàm main)
 * ============================================================
 * Vai trò: điều phối. Không tự xử lý nghiệp vụ, chỉ:
 *   1) nạp dữ liệu từ file khi khởi động;
 *   2) hiện menu và gọi đúng method của AccountTypeList / BankAccountList;
 *   3) thoát khi người dùng chọn 0 (KHÔNG tự động ghi file).
 */
public class BankAccountManager {

    /** Tên file dữ liệu (đọc/ghi ở thư mục hiện tại khi chạy chương trình). */
    private static final String TYPE_FILE = "accounttypes.txt";
    private static final String ACCOUNT_FILE = "bankaccounts.txt";

    /** In menu chính gồm 11 chức năng và lựa chọn thoát. */
    private static void printMenu() {
        System.out.println();
        System.out.println("===== BANK ACCOUNT MANAGEMENT =====");
        System.out.println(" 1. List all AccountTypes");
        System.out.println(" 2. Add a new AccountType");
        System.out.println(" 3. Search AccountType by ID");
        System.out.println(" 4. Update AccountType");
        System.out.println(" 5. Save AccountTypes to accounttypes.txt");
        System.out.println(" 6. List all BankAccounts sorted by typeName");
        System.out.println(" 7. Search BankAccounts by partial typeName");
        System.out.println(" 8. Add a new BankAccount");
        System.out.println(" 9. Remove BankAccount by ID");
        System.out.println("10. Update BankAccount by ID");
        System.out.println("11. Save BankAccounts to bankaccounts.txt");
        System.out.println(" 0. Exit");
    }

    public static void main(String[] args) {
        // --- Bước 1: tạo các danh sách ---
        // BankAccountList cần AccountTypeList để tra cứu loại tài khoản.
        AccountTypeList types = new AccountTypeList();
        BankAccountList accounts = new BankAccountList(types);

        // --- Bước 2: nạp file. THỨ TỰ QUAN TRỌNG: loại tài khoản trước, tài khoản sau ---
        // Nếu thiếu file thì vẫn chạy tiếp với danh sách rỗng, để người dùng nhập mới.
        if (!types.loadFromFile(TYPE_FILE))
            System.out.println("Warning: starting with an empty AccountType list.");
        if (!accounts.loadFromFile(ACCOUNT_FILE))
            System.out.println("Warning: starting with an empty BankAccount list.");

        // --- Bước 3: vòng lặp menu, chạy tới khi người dùng chọn 0 ---
        int choice;
        do {
            printMenu();
            choice = Menu.readInt("Select an option (0-11): ", 0, 11);   // nhập sai -> tự hỏi lại
            System.out.println();
            switch (choice) {
                case 1:                                     // Liệt kê AccountType
                    types.listAccountTypes();
                    break;
                case 2:                                     // Thêm AccountType
                    types.addAccountType();
                    break;
                case 3:                                     // Tìm AccountType theo mã
                    String id = Menu.readString("accountTypeID to search: ", null);
                    int pos = types.searchID(id);           // -1 nghĩa là không có
                    if (pos == -1) System.out.println("  AccountType '" + id + "' not found.");
                    else {
                        System.out.println(AccountType.screenHeader());
                        System.out.println(types.get(pos).screenString());
                    }
                    break;
                case 4:                                     // Cập nhật AccountType
                    types.updateAccountType();
                    break;
                case 5:                                     // Lưu AccountType ra file
                    if (types.saveToFile(TYPE_FILE))
                        System.out.println("Saved " + types.size() + " AccountType(s) to " + TYPE_FILE);
                    break;
                case 6:                                     // Liệt kê BankAccount đã sắp xếp
                    accounts.printBasedAccountTypeName();
                    break;
                case 7:                                     // Tìm theo một phần typeName
                    accounts.searchByTypeName();
                    break;
                case 8:                                     // Thêm BankAccount
                    accounts.addBankAccount();
                    break;
                case 9:                                     // Xóa BankAccount
                    accounts.removeBankAccount();
                    break;
                case 10:                                    // Cập nhật BankAccount
                    accounts.updateBankAccount();
                    break;
                case 11:                                    // Lưu BankAccount ra file
                    if (accounts.saveToFile(ACCOUNT_FILE))
                        System.out.println("Saved " + accounts.size() + " BankAccount(s) to " + ACCOUNT_FILE);
                    break;
                default:                                    // 0: không làm gì, vòng lặp sẽ kết thúc
                    break;
            }
        } while (choice != 0);

        // Thoát KHÔNG tự ghi file: muốn lưu phải chọn option 5 / 11 trước đó.
        System.out.println("Goodbye! (Unsaved changes were not written to disk.)");
    }
}
