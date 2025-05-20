package giaidoan3;

import java.nio.channels.*;
import java.nio.ByteBuffer;
import java.net.*;
import java.util.*;

public class NIOMultiClient {
    private static int clientId = 0; // Để tạo ID cho mỗi client

    public static void main(String[] args) throws Exception {
        // Tạo Selector cho client
        Selector selector = Selector.open();
        System.out.println("Client Selector: " + selector);

        // Tạo và cấu hình SocketChannel
        SocketChannel clientChannel = SocketChannel.open();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_CONNECT);
        clientChannel.connect(new InetSocketAddress("localhost", 12345));

        int myId = ++clientId; // Gán ID cho client
        System.out.println("Client " + myId + ": Đang kết nối...");

        while (true) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (!key.isValid()) {
                    System.out.println("Client " + myId + ": Key không hợp lệ, bỏ qua...");
                    continue;
                }

                if (key.isConnectable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    if (client.finishConnect()) {
                        System.out.println("Client " + myId + ": Kết nối thành công tới server");
                        client.register(selector, SelectionKey.OP_WRITE);
                    }
                } else if (key.isWritable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    String message = "Hello from Client " + myId;
                    ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                    client.write(buffer);
                    System.out.println("Client " + myId + ": Gửi: " + message);
                    key.interestOps(SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int bytesRead;
                    try {
                        bytesRead = client.read(buffer);
                    } catch (Exception e) {
                        System.out.println("Client " + myId + ": Lỗi khi đọc từ server");
                        client.close();
                        break;
                    }
                    if (bytesRead == -1) {
                        System.out.println("Client " + myId + ": Server ngắt kết nối");
                        client.close();
                        break;
                    } else {
                        buffer.flip();
                        System.out.println("Client " + myId + ": Nhận: " + new String(buffer.array(), 0, bytesRead));
                        client.close();
                        break;
                    }
                }
            }
        }
    }
}
