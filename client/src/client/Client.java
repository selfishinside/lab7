package client;

public class Client {
    private final String name;
    private final char[] passwd;
    private static Client instance;
    private static boolean isAuth = false;

    private Client(String name, char[] passwd) {
        this.name = name;
        this.passwd = passwd;
    }

    public static Client getInstance(String name, char[] passwd) {
        if (instance == null) {
            instance = new Client(name, passwd);
        }
        return instance;
    }

    public static Client getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Client is not initialized. Please login first");
        }
        return instance;
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    public String getName() {
        return name;
    }

    public char[] getPasswd() {
        return passwd;
    }

    public static boolean isAuth() {
        return isAuth;
    }

    public void setAuth(boolean auth) {
        isAuth = auth;
    }
}

