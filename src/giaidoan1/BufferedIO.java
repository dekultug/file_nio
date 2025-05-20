package giaidoan1;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BufferedIO {
    public static void main(String[] args) {
        // Ghi file lớn bằng BufferedWriter

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("large.txt"))) {
            for (int i = 0; i < 10000; i++) {
                writer.write("Dòng " + i);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Đọc file bằng BufferedReader
        long start = System.nanoTime();
        try (BufferedReader reader = new BufferedReader(new FileReader("large.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Xử lý dòng
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        long end = System.nanoTime();
        System.out.println("Thời gian đọc: " + (end - start) / 1_000_000.0 + " ms");

        // Serialization
        List<Student> students = new ArrayList<>();
        students.add(new Student("Nam", 1));
        students.add(new Student("Lan", 2));
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("students.dat"))) {
            out.writeObject(students);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("students.dat"))) {
            List<Student> readStudents = (List<Student>) in.readObject();
            readStudents.forEach(s -> System.out.println(s.name + ": " + s.id));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        // NIO: Đọc file
        try {
            List<String> lines = Files.readAllLines(Path.of("large.txt"), StandardCharsets.UTF_8);
            System.out.println("Số dòng: " + lines.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Student implements Serializable {
    String name;
    int id;

    public Student(String name, int id) {
        this.name = name;
        this.id = id;
    }
}