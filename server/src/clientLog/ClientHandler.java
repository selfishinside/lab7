package clientLog;

import managers.DB.PostgreSQLManager;

public class ClientHandler {
    private final String name;
    private final char[] passwd;
    private static long userId;

    public ClientHandler(String name, char[] passwd) {
        this.name = name;
        this.passwd = passwd;
    }

    public boolean regUser() {
        PostgreSQLManager manager = new PostgreSQLManager();
        long id = manager.regUser(name, passwd);
        if (id > 0) {
            userId = id;
            return true;
        }
        return false;
    }

    public boolean authUser() {
        PostgreSQLManager manager = new PostgreSQLManager();
        long id = manager.authUser(name, passwd);
        if (id > 0) {
            userId = id;
            return true;
        }
        return false;
    }

    public static void isAuthUserCommand(String name, char[] passwd) {
        PostgreSQLManager manager = new PostgreSQLManager();
        long id = manager.authUser(name, passwd);
        if (id > 0) {
            userId = id;
        } else {
            System.out.println("No such user");
        }
    }

    public static long getUserId() {
        return userId;
    }
}
