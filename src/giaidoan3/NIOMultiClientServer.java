package giaidoan3;

import java.nio.channels.*;
import java.nio.ByteBuffer;
import java.net.*;
import java.util.*;

public class NIOMultiClientServer {
    public static void main(String[] args) throws Exception {
        // Tạo Selector cho server
        Selector selector = Selector.open();
        System.out.println("Server Selector: " + selector);

        // Tạo và cấu hình ServerSocketChannel
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(12345));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server đang chạy trên port 12345...");

        while (true) {
            selector.select(); // Chờ sự kiện
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                // Kiểm tra key hợp lệ
                if (!key.isValid()) {
                    System.out.println("Server: Key không hợp lệ, bỏ qua...");
                    continue;
                }

                if (key.isAcceptable()) {
                    // Xử lý kết nối mới
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel client = server.accept();
                    client.configureBlocking(false);
                    // Đăng ký SocketChannel với OP_READ
                    client.register(selector, SelectionKey.OP_READ);
                    System.out.println("Server: Kết nối mới từ " + client.getRemoteAddress());
                } else if (key.isReadable()) {
                    // Xử lý dữ liệu từ client
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int bytesRead;
                    try {
                        bytesRead = client.read(buffer);
                    } catch (Exception e) {
                        System.out.println("Server: Lỗi khi đọc từ " + client.getRemoteAddress());
                        client.close();
                        continue;
                    }
                    if (bytesRead == -1) {
                        System.out.println("Server: Client ngắt kết nối: " + client.getRemoteAddress());
                        client.close();
                    } else {
                        buffer.flip();
                        String received = new String(buffer.array(), 0, bytesRead);
                        System.out.println("Server: Nhận từ " + client.getRemoteAddress() + ": " + received);
                        // Lưu dữ liệu để gửi phản hồi
                        key.attach("Echo: " + received);
                        // Chuyển sang OP_WRITE để gửi phản hồi
                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                } else if (key.isWritable()) {
                    // Gửi phản hồi tới client
                    SocketChannel client = (SocketChannel) key.channel();
                    String response = (String) key.attachment();
                    ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
                    client.write(buffer);
                    System.out.println("Server: Gửi tới " + client.getRemoteAddress() + ": " + response);
                    // Chuyển lại sang OP_READ
                    key.interestOps(SelectionKey.OP_READ);
                    key.attach(null); // Xóa attachment
                }
            }
        }
    }
}
