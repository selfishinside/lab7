package system;


import clientLog.ClientHandler;
import collection.Vehicle;
import exceptions.ReadRequestException;
import exceptions.RootException;
import managers.CollectionManager;
import managers.CommandManager;


import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Logger;


public class Server {
    private InetSocketAddress address;
    private ServerSocketChannel chanel;
    private Selector selector;

    private ExecutorService readPool;
    private ExecutorService processPool;
    private ForkJoinPool sendResponsePool;

    public void initialize(int port) throws IOException, RootException {
        Logger.getLogger(Server.class.getName()).info("system.Server was started at address: " + address);
        this.address = new InetSocketAddress(port); // создаем адрес сокета (IP-адрес и порт)
        this.chanel = ServerSocketChannel.open();
        this.chanel.bind(address);
        this.chanel.configureBlocking(false); // неблокирующий режим ввода-вывода
        this.selector = Selector.open();
        this.readPool = Executors.newFixedThreadPool(10);
        this.processPool = Executors.newCachedThreadPool();
        this.sendResponsePool = new ForkJoinPool();

            new CommandManager();
            try {
                Logger.getLogger(Server.class.getName()).info("Downloading data from DB...");
                CollectionManager.getInstance().loadCollectionFromDB();
                Logger.getLogger(Server.class.getName()).info("Data was downloaded");
            } catch (Exception e) {
                Logger.getLogger(Server.class.getName()).warning("Error while reading DB\n");
                System.exit(0);
            }
        Logger.getLogger(Server.class.getName()).info("system.Server is initialized");
    }

    public void start() {
        Logger.getLogger(Server.class.getName()).info("system.Server is available");
        try {
            new Thread(() -> {
                BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    try {
                        String input = consoleReader.readLine();
                        if (input.equals("exit") || input.equals("save")) {
                            CommandManager.startExecutingServerMode(new Request(input, null, null, null, null));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start(); // поток для ввода команд на сервере

            chanel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                if (selector.select() > 0) {
                    Set<SelectionKey> selectedKeys = selector.selectedKeys(); // получаем список ключей от каналов, готовых к работеwhile (iter.hasNext()) {
                    Iterator<SelectionKey> iter = selectedKeys.iterator(); // получаем итератор ключей

                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (!key.isValid()) {
                            continue;
                        }
                        if (key.isAcceptable()) {
                            acceptClient(key);
                        } else if (key.isReadable()) {
                            Logger.getLogger(Server.class.getName()).info("request from client");
                            readPool.submit(() -> handleRead(key));
                        } else if (key.isWritable()) {
                            sendResponsePool.submit(() -> {
                                try {
                                    sendAnswer(key);
                                } catch (IOException e) {
                                    Logger.getLogger(Server.class.getName()).severe("Error sending answer: " + e.getMessage());
                                    key.cancel();
                                    try {
                                        key.channel().close();
                                    } catch (IOException ex) {
                                        Logger.getLogger(Server.class.getName()).severe("Failed to close the channel: " + ex.getMessage());
                                    }
                                }
                            });
                        }
                    }
                }
            }
        } catch (IOException e) {
            Logger.getLogger(Server.class.getName()).severe("Error in server operation: " + e.getMessage());
        }
    }

    private void acceptClient(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel client = serverChannel.accept();
        if (client != null) {
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            Logger.getLogger(Server.class.getName()).info("New client connected: " + client);
        }
    }

    private void handleRead(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        try {
            Request request = readRequest(client);
            if (request != null) {
                key.interestOps(0);
                processPool.submit(() -> {
                    try {
                        processRequest(client, request, key);
                    } catch (Exception e) {
                        Logger.getLogger(Server.class.getName()).severe("Error in processing request: " + e.getMessage());
                    } // обрабатываем запрос в другом потоке
                });
            } else {
                Logger.getLogger(Server.class.getName()).info("No data read, possibly connection was closed");
            }
        } catch (IOException | ReadRequestException e) {
                Logger.getLogger(Server.class.getName()).warning("Failed to read request: " + e.getMessage());
                try {
                    client.close();
                    key.cancel();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).severe("Failed to close client socket: " + ex.getMessage());
                }
            }
        }


    private Request readRequest(SocketChannel client) throws IOException, ReadRequestException {
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        buffer.clear();
        int bytesRead = client.read(buffer);
        Logger.getLogger(Server.class.getName()).info("Bytes read: " + bytesRead);

        if (bytesRead == -1) {
            Logger.getLogger(Server.class.getName()).info("Client closed connection");
            throw new IOException("Client closed connection");
        } else if (bytesRead == 0) {
            return null;
        }
        buffer.flip();
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer.array(), 0, buffer.limit()))) {
            return (Request) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new ReadRequestException();
        }
    }

    private void processRequest(SocketChannel client, Request request, SelectionKey key) {
        Logger.getLogger(Server.class.getName()).info("Processing request: " + request.getMessage());
        if (!request.getMessage().equals("register")) {
            ClientHandler.isAuthUserCommand(request.getName(), request.getPasswd());
        }
        try {
            String responseMessage = CommandManager.startExecutingClientMode(request);
            Request response = new Request(responseMessage, new Vehicle(), null, request.getName(), request.getPasswd());
            ByteBuffer buffer = ByteBuffer.wrap(serialize(response));
            Logger.getLogger(Server.class.getName()).info("Registering key for write operation");
            key.attach(buffer);
            key.interestOps(SelectionKey.OP_WRITE);
            selector.wakeup();
        } catch (IOException e) {
            Logger.getLogger(Server.class.getName()).warning("Something wrong with IO\n" + e.getMessage());
            try {
                client.close();
                key.cancel();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).severe("Failed to close client socket: " + ex.getMessage());
            }
        }

    }

    private byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        return baos.toByteArray();
    }

    public void sendAnswer(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel(); // получаем канал для работы
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        if (buffer == null) {
            Logger.getLogger(Server.class.getName()).severe("Buffer is null in sendAnswer method");
            return;
        }
        try {
            while (buffer.hasRemaining()) {
                client.write(buffer);
            }
            Logger.getLogger(Server.class.getName()).info("Answer sent to client: " + client);
            key.interestOps(SelectionKey.OP_READ);
            key.attach(null);
            selector.wakeup();
        } catch (IOException e) {
            Logger.getLogger(Server.class.getName()).severe("Error sending answer: " + e.getMessage());
            try {
                client.close();
                key.cancel();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).severe("Failed to close the channel: " + ex.getMessage());
            }
        }
    }
}