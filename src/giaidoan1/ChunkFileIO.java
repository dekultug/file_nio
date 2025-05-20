package giaidoan1;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ChunkFileIO {
    public static void main(String[] args) throws IOException {
        long chunkSize = 1024 * 1024;

        try {
            Path path = Paths.get("video.mp4");
            FileChannel inChanel = FileChannel.open(path, StandardOpenOption.READ);
            int indexChunk = 0;
            long positionBuffer = 0;
            Long size = inChanel.size();
            while (positionBuffer < size){
                Path outPath = Paths.get("chunk_" + indexChunk + ".bin");
                FileChannel outChanel = FileChannel.open(outPath, StandardOpenOption.CREATE,StandardOpenOption.WRITE);
                long remain = Math.min(chunkSize, size - positionBuffer);
                inChanel.transferTo(positionBuffer, remain,outChanel);
                positionBuffer+= remain;
                indexChunk++;
            }
        } catch (IOException e) {
            throw e;
        }catch (Exception e){
            throw e;
        }

        // Ghép file từ chunk
        try (FileChannel outChannel = FileChannel.open(Paths.get("reconstructed.mp4"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            for (int i = 0; ; i++) {
                File chunkFile = new File("chunk_" + i + ".bin");
                if (!chunkFile.exists()) break;
                try (FileChannel inChannel = FileChannel.open(chunkFile.toPath(), StandardOpenOption.READ)) {
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                    chunkFile.delete();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
