package giaidoan2;

import java.nio.channels.*;
import java.nio.ByteBuffer;
import java.net.*;
import java.util.*;

public class NIOSelectorClient {
    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open();
        SocketChannel clientChannel = SocketChannel.open();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_CONNECT);
        clientChannel.connect(new InetSocketAddress("localhost", 12345));

        System.out.println("Client đang chạy...");

        while (true) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                // Kiểm tra key có hợp lệ không
                if (!key.isValid()) {
                    System.out.println("Key không hợp lệ, bỏ qua...");
                    continue;
                }

                if (key.isConnectable()) {
                    // Sự kiện: Kết nối tới server
                    SocketChannel client = (SocketChannel) key.channel();
                    if (client.finishConnect()) {
                        System.out.println("Client kết nối thành công tới server");
                        client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                    }
                } else if (key.isWritable()) {
                    // Sự kiện: Sẵn sàng ghi dữ liệu
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.wrap("Hello, Server!".getBytes());
                    client.write(buffer);
                    System.out.println("Client gửi: Hello, Server!");
                    // Hủy đăng ký OP_WRITE
                    key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                } else if (key.isReadable()) {
                    // Sự kiện: Có dữ liệu để đọc
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int bytesRead = client.read(buffer);
                    if (bytesRead == -1) {
                        System.out.println("Server ngắt kết nối");
                        client.close();
                        break; // Thoát vòng lặp
                    } else {
                        buffer.flip();
                        System.out.println("Client nhận: " + new String(buffer.array(), 0, bytesRead));
                        client.close(); // Đóng sau khi nhận phản hồi
                        break; // Thoát vòng lặp
                    }
                }
            }
        }
    }
}