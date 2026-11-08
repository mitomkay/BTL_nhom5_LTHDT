import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * ============================================================
 *  CLASS: Menu  (Tiện ích nhập liệu từ bàn phím)
 * ============================================================
 * Vai trò: gom TOÀN BỘ việc đọc dữ liệu từ console vào một chỗ.
 * Mọi hàm đọc đều lặp lại cho tới khi người dùng nhập hợp lệ,
 * nên chương trình không bị dừng khi nhập sai (ví dụ nhập chữ
 * vào chỗ cần số).
 *
 * Tất cả method đều static: gọi trực tiếp Menu.readInt(...) mà
 * không cần tạo đối tượng. Chỉ dùng MỘT Scanner duy nhất cho cả
 * chương trình (tạo nhiều Scanner trên System.in dễ gây lỗi mất dữ liệu).
 */
public class Menu {

    /** Scanner dùng chung cho toàn chương trình. */
    private static final Scanner SC = new Scanner(System.in);

    // ============================================================
    //  CÁC HÀM ĐỌC CƠ BẢN
    // ============================================================

    /**
     * Hiện lời nhắc, đọc một dòng và trim().
     * Nếu luồng nhập bị đóng (ví dụ nhấn Ctrl+D, hoặc input đã hết khi chạy
     * bằng file) thì nextLine() ném NoSuchElementException: bắt lại và
     * thoát êm, KHÔNG ghi file.
     */
    public static String readLine(String prompt) {
        System.out.print(prompt);
        try {
            return SC.nextLine().trim();
        } catch (NoSuchElementException e) {
            System.out.println("\nInput closed. Exiting without saving.");
            System.exit(0);
            return "";   // không bao giờ tới đây, chỉ để compiler hài lòng
        }
    }

    /**
     * Đọc một chuỗi văn bản.
     *  - keep == null  : chuỗi BẮT BUỘC (không cho rỗng) -> dùng khi THÊM mới.
     *  - keep != null  : nhấn Enter (rỗng) sẽ GIỮ giá trị cũ -> dùng khi CẬP NHẬT.
     * Từ chối dấu ',' vì dấu phẩy là ký tự phân cách trong file .txt;
     * nếu cho phép sẽ làm hỏng định dạng khi ghi/đọc lại.
     */
    public static String readString(String prompt, String keep) {
        while (true) {
            String s = readLine(prompt);
            if (s.isEmpty()) {
                if (keep != null) return keep;            // cập nhật: giữ giá trị cũ
                System.out.println("  Error: value must not be empty.");
                continue;
            }
            if (s.contains(",")) {
                System.out.println("  Error: value must not contain ',' (it is the file separator).");
                continue;
            }
            return s;
        }
    }

    /**
     * Đọc một số thực.
     *  - mustBePositive = true  : yêu cầu giá trị > 0  (dùng cho interestRate).
     *  - mustBePositive = false : yêu cầu giá trị >= 0 (dùng cho balance).
     *  - keep != null : nhấn Enter để giữ giá trị cũ (dùng khi cập nhật).
     * Dùng regex để chỉ chấp nhận dạng số thông thường, tránh các chuỗi lạ
     * mà Double.parseDouble vẫn nhận (như "NaN", "1d", "0x1p3").
     */
    public static double readDouble(String prompt, boolean mustBePositive, Double keep) {
        while (true) {
            String s = readLine(prompt);
            if (s.isEmpty() && keep != null) return keep;       // cập nhật: giữ giá trị cũ
            if (!s.matches("-?\\d+(\\.\\d+)?")) {               // không phải số hợp lệ
                System.out.println("  Error: please enter a valid number.");
                continue;
            }
            double v = Double.parseDouble(s);
            if (mustBePositive && v <= 0) {
                System.out.println("  Error: value must be greater than 0.");
                continue;
            }
            if (!mustBePositive && v < 0) {
                System.out.println("  Error: value must not be negative.");
                continue;
            }
            return v;
        }
    }

    /** Đọc số nguyên trong khoảng [min, max]; nhập sai thì hỏi lại. */
    public static int readInt(String prompt, int min, int max) {
        while (true) {
            String s = readLine(prompt);
            try {
                int v = Integer.parseInt(s);
                if (v >= min && v <= max) return v;   // hợp lệ
            } catch (NumberFormatException e) {
                // nhập chữ -> rơi xuống thông báo lỗi bên dưới
            }
            System.out.println("  Error: please enter a whole number from " + min + " to " + max + ".");
        }
    }

    /** Hỏi câu có/không; chỉ chấp nhận y hoặc n (không phân biệt hoa/thường). */
    public static boolean readYesNo(String prompt) {
        while (true) {
            String s = readLine(prompt + " (y/n): ");
            if (s.equalsIgnoreCase("y")) return true;
            if (s.equalsIgnoreCase("n")) return false;
            System.out.println("  Error: please enter y or n.");
        }
    }

    // ============================================================
    //  CÁC HÀM CHỌN TỪ DANH SÁCH (theo thiết kế UML của đề)
    //  Dùng generic <E> nên dùng được cho ArrayList kiểu bất kỳ.
    // ============================================================

    /**
     * Hiện danh sách đánh số 1..n và trả về SỐ THỨ TỰ người dùng chọn.
     * Trả về -1 nếu danh sách rỗng (tránh vòng lặp vô hạn vì không có lựa chọn nào).
     */
    public static <E> int int_getChoice(ArrayList<E> options) {
        if (options == null || options.isEmpty()) return -1;
        for (int i = 0; i < options.size(); i++)
            System.out.println("  " + (i + 1) + ". " + options.get(i));   // gọi toString() của phần tử
        return readInt("Your choice (1-" + options.size() + "): ", 1, options.size());
    }

    /**
     * Giống int_getChoice nhưng trả về CHÍNH ĐỐI TƯỢNG được chọn
     * (hoặc null nếu danh sách rỗng).
     */
    public static <E> E ref_getChoice(ArrayList<E> options) {
        int c = int_getChoice(options);
        return (c == -1) ? null : options.get(c - 1);   // đổi số thứ tự (1..n) sang chỉ số (0..n-1)
    }
}
