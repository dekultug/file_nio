package giaidoan3.chatchit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class ChatClient1 {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) throws IOException {
        // Khởi tạo Selector
        Selector selector = Selector.open();
        // Kết nối đến server
        SocketChannel client = SocketChannel.open();
        client.configureBlocking(false);
        client.connect(new InetSocketAddress(HOST, PORT));
        client.register(selector, SelectionKey.OP_CONNECT);

        // Thread để đọc từ bàn phím
        Thread inputThread = new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                while (true) {
                    String message = reader.readLine();
                    if (message == null || message.equals("exit")) {
                        client.close();
                        break;
                    }
                    // Gắn tin nhắn để gửi
//                    for (SelectionKey key : selector.keys()) {
//                        if (key.isValid() && key.channel() == client) {
//                            key.attach(message);
//                            key.interestOps(SelectionKey.OP_WRITE);
//                            selector.wakeup();
//                        }
//                    }

                    SelectionKey key = selector.keys().iterator().next();
                    if (key.isValid() && key.channel() == client) {
                            key.attach(message);
                            key.interestOps(SelectionKey.OP_WRITE);
                            selector.wakeup();
                        }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        inputThread.start();

        // Vòng lặp xử lý sự kiện
        while (client.isOpen()) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();

                if (key.isConnectable()) {
                    // Hoàn thành kết nối
                    if (client.finishConnect()) {
                        System.out.println("Đã kết nối đến server");
                        key.interestOps(SelectionKey.OP_READ);
                    }
                } else if (key.isReadable()) {
                    // Nhận tin nhắn từ server
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    int bytesRead = channel.read(buffer);
                    if (bytesRead == -1) {
                        channel.close();
                        System.out.println("Ngắt kết nối với server");
                        break;
                    }
                    buffer.flip();
                    String message = new String(buffer.array(), 0, bytesRead, StandardCharsets.UTF_8);
                    System.out.println(message);
                } else if (key.isWritable()) {
                    // Gửi tin nhắn từ bàn phím
                    SocketChannel channel = (SocketChannel) key.channel();
                    String message = (String) key.attachment();
                    if (message != null) {
                        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
                        channel.write(buffer);
                        key.interestOps(SelectionKey.OP_READ);
                        key.attach(null);
                    }
                }
            }
        }
    }
}
