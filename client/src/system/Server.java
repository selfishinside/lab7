package system;

import client.Client;
import collection.Vehicle;
import exceptions.NoArgumentException;
import exceptions.WrongArgumentException;
import generators.VehicleAsker;
import managers.ExecuteScriptCommand;
import managers.HistoryCommand;

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Server {
    private static SocketChannel socket;
    private static Selector selector;
    private static Deque<String> lastTwelveCommands = new LinkedList<>();

    public void initialize(String host, int port) throws IOException {
        try {
            InetSocketAddress address = new InetSocketAddress(host, port); // создаем адрес сокета (IP-адрес и порт)
            socket = SocketChannel.open();
            socket.connect(address);
            selector = Selector.open();
            socket.configureBlocking(false); // неблокирующий режим ввода-вывода, когда идет ввод вывод паралельно идет другие операции 
        } catch (RuntimeException | ConnectException e) {
            System.out.println("Server " + host + " on port " + port + " is not available");
            System.exit(1);
        }
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("Welcome to app!");
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine().trim();
                try {
                    if (command.equals("exit")) {
                        System.exit(1);
                    }
                    if (!Client.isAuth()) {
                        if (command.equals("register") || command.equals("login")) {
                            AuthCommandsHandler(command, scanner);
                        } else {
                            System.out.println("Unauthorized access. Please login or register to proceed.");
                        }
                    } else {
                        try {
                            if (command.equals("register") || command.equals("login")) {
                                System.out.println("You are already logged in");
                                continue;
                            }
                                otherCommandHandler(command, scanner);
                        } catch (IllegalStateException e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Server is not availdable");
                    System.out.println(e.getMessage() + " " + e);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (NullPointerException e) {
            System.out.println("");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void AuthCommandsHandler(String command, Scanner scanner) throws IOException {
        try {
            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Enter password: ");
            char[] password = scanner.nextLine().trim().toCharArray();
            Client client = Client.getInstance(username, password);
            Request request = new Request(command, null, null, username, password);
            sendRequest(request);
        } catch (IOException e) {
            System.out.println("Something wrong");
        }
    }

    private void otherCommandHandler(String command, Scanner scanner) throws IOException {
        try {
            if (!command.isEmpty()) {
                synchronized (lastTwelveCommands) {
                    if (lastTwelveCommands.size() >= 12) {
                        lastTwelveCommands.removeLast();
                    }
                    lastTwelveCommands.addFirst(command);
                }
                Vehicle vehicle = new Vehicle();
                String key = null;
                boolean isClientCommand = false;
                if (command.equals("add") || command.equals("addIfMax") || command.equals("addIfMin")) {
                    vehicle = VehicleAsker.createVehicle();
                } else if (command.split(" ")[0].equals("execute_script")) {
                    ExecuteScriptCommand.execute(command);
                    isClientCommand = true;
                } else if (command.split(" ")[0].equals("updateId")) {
                    if (command.split(" ").length == 2) {
                        key = command.split(" ")[1];
                    }else {
                        throw new NoArgumentException("id");
                    }
                    vehicle = VehicleAsker.createVehicle();
                    vehicle.setId(Long.parseLong(key));
                } else if (command.contains("rbid") || command.contains("count_by_house") || command.contains("filter_contains_name") || command.contains("filter_starts_with_name")) {
                    if (command.split(" ").length == 2) {
                        key = command.split(" ")[1];
                    }
                } else if (command.split(" ")[0].equals("history")) {
                    if (command.split(" ").length == 1) {
                        HistoryCommand.execute(lastTwelveCommands);
                        isClientCommand = true;
                    } else {
                        throw new WrongArgumentException(command.split(" ")[1]);
                    }
                }
                if (!isClientCommand) {
                    Request request = new Request(command, vehicle, key, Client.getInstance().getName(), Client.getInstance().getPasswd());
                    sendRequest(request);
                }
            }
        } catch (IOException e) {
            System.out.println("Server is not available");
            System.out.println(e.getMessage() + " " + e);
        } catch (WrongArgumentException | NoArgumentException e) {
            System.out.println(e.getMessage());
        } catch (NullPointerException e) {
            System.out.println("");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void sendRequest(Request request) throws IOException {
        ObjectOutputStream objectOutputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(request);
        objectOutputStream.close();
        ByteBuffer buffer = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());

        // Отправляем данные
        while (buffer.hasRemaining()) {
            socket.write(buffer);
        }

        try {
            Request request_server = getAnswer();
            if (request_server.getMessage().contains("Authentication successful")) {
                Client.getInstance(request_server.getName(), request_server.getPasswd()).setAuth(true);
            }
            System.out.println("Server answer: \n" + request_server.getMessage());
        } catch (ClassNotFoundException e) {
            // Обработка исключения, если класс Request не найден
            System.out.println("Wrong answer from server");
        } catch (IOException e) {
            // Обработка исключения ввода-вывода при чтении объекта
            System.out.println("Something wrong while reading answer from server");
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public static Request getAnswer() throws IOException, InterruptedException, ClassNotFoundException {
        Thread.sleep(2000);
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        while (socket.read(buffer) > 0) {
            buffer.flip();
            try (ByteArrayInputStream bi = new ByteArrayInputStream(buffer.array(), 0, buffer.limit());
                 ObjectInputStream oi = new ObjectInputStream(bi)) {
                return (Request) oi.readObject();
            } catch (ClassNotFoundException | IOException e) {
                System.out.println("Error receiving response from server: " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    public static Deque<String> getLastTwelveCommands() {
        return lastTwelveCommands;
    }
}

