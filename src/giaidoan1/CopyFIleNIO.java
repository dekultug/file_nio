package giaidoan1;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class CopyFIleNIO {
    public static void main(String[] args) throws IOException {
        Path pathFile100Mb = Paths.get("video.mp4");
        Path copy = Paths.get("videoCopy100mb.mp4");
        FileChannel ifc = FileChannel.open(pathFile100Mb, StandardOpenOption.READ);
        FileChannel ofc = FileChannel.open(copy, StandardOpenOption.WRITE,StandardOpenOption.CREATE);
        ifc.transferTo(0,ifc.size(), ofc);
    }
}
