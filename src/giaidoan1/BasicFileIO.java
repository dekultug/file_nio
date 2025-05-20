package giaidoan1;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class BasicFileIO {

    public static void main(String[] args) {
        // Tạo và quản lý file/thư mục
        File file = new File("test.txt");
        File dir = new File("data");
        try {
            if (file.createNewFile()) {
                System.out.println("Tạo file thành công");
            }
            if (dir.mkdir()) {
                System.out.println("Tạo thư mục thành công");
            }
            System.out.println("File tồn tại: " + file.exists() + " "  + file.getAbsoluteFile());
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    System.out.println(f.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Ghi file văn bản
        try (FileWriter writer = new FileWriter("test.txt")) {
            writer.write("Xin chào, đây là file văn bản!\n");
            writer.write("Dòng thứ hai.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Đọc file văn bản
        try (FileReader reader = new FileReader("test.txt")) {
            int ch;
            while ((ch = reader.read()) != -1) {
                System.out.print((char) ch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
