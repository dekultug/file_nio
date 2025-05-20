package giaidoan1;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CreateFile100Mb {

    public static void main(String[] args) {
        try (FileOutputStream out = new FileOutputStream("video.dat")) {
            byte[] data = new byte[1024 * 1024]; // 1MB
            for (int i = 0; i < 100; i++) {
                out.write(data);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
