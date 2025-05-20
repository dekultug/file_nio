package giaidoan3.chatchit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class ChatServer {
    private static final int PORT = 8080;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) throws IOException {
        // Khởi tạo Selector
        Selector selector = Selector.open();
        // Khởi tạo ServerSocketChannel
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(PORT));
        serverSocket.configureBlocking(false);
        // Đăng ký OP_ACCEPT để chấp nhận kết nối
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server khởi động tại port " + PORT);

        while (true) {
            // Chờ sự kiện
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();

                if (key.isAcceptable()) {
                    // Chấp nhận kết nối mới
                    SocketChannel client = serverSocket.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                    System.out.println("Client mới kết nối: " + client.getRemoteAddress());
                } else if (key.isReadable()) {
                    // Đọc tin nhắn từ client
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    try {
                        int bytesRead = client.read(buffer);
                        if (bytesRead == -1) {
                            // Client ngắt kết nối
                            System.out.println("Client ngắt kết nối: " + client.getRemoteAddress());
                            client.close();
                            continue;
                        }
                        buffer.flip();
                        String message = new String(buffer.array(), 0, bytesRead, StandardCharsets.UTF_8);
                        System.out.println("Nhận từ " + client.getRemoteAddress() + ": " + message);

                        // Gắn tin nhắn vào tất cả các key để gửi
                        for (SelectionKey k : selector.keys()) {
                            if (k.isValid() && k.channel() instanceof SocketChannel && k.channel() != client) {
                                k.attach("Từ " + client.getRemoteAddress() + ": " + message);
                                k.interestOps(SelectionKey.OP_WRITE);
                            }
                        }
                        // Gửi lại tin nhắn cho client gửi (echo)
                        key.attach("Bạn: " + message);
                        key.interestOps(SelectionKey.OP_WRITE);
                    } catch (IOException e) {
                        System.out.println("Lỗi với client: " + client.getRemoteAddress());
                        client.close();
                    }
                } else if (key.isWritable()) {
                    // Gửi tin nhắn đến client
                    SocketChannel client = (SocketChannel) key.channel();
                    String message = (String) key.attachment();
                    if (message != null) {
                        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
                        client.write(buffer);
                        // Quay lại OP_READ sau khi gửi
                        key.interestOps(SelectionKey.OP_READ);
                        key.attach(null); // Xóa attachment
                    }
                }
            }
        }
    }
}
