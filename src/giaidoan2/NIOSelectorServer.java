package giaidoan2;

import java.nio.channels.*;
import java.nio.ByteBuffer;
import java.net.*;
import java.util.*;

public class NIOSelectorServer {
    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(12345));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server đang chạy...");

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

                if (key.isAcceptable()) {
                    // Sự kiện: Có kết nối mới từ client
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel client = server.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    System.out.println("Kết nối mới từ: " + client.getRemoteAddress());
                } else if (key.isReadable()) {
                    // Sự kiện: Có dữ liệu để đọc
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int bytesRead = client.read(buffer);
                    if (bytesRead == -1) {
                        System.out.println("Client ngắt kết nối: " + client.getRemoteAddress());
                        client.close(); // Đóng channel, key sẽ không hợp lệ
                    } else {
                        buffer.flip();
                        System.out.println("Server nhận: " + new String(buffer.array(), 0, bytesRead));
                    }
                } else if (key.isWritable()) {
                    // Sự kiện: Sẵn sàng ghi dữ liệu
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.wrap("Echo from server".getBytes());
                    client.write(buffer);
                    System.out.println("Server gửi: Echo from server");
                    // Hủy đăng ký OP_WRITE để tránh lặp lại
                    key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                }
            }
        }
    }
}
