package system;

import collection.Vehicle;

import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 5266595875730820760L;
    private String message = null;
    private Vehicle vehicle = null;

    private String key = null;

    private String name = null;
    private char[] passwd = null;

    public Request(String message, Vehicle vehicle, String key, String name, char[] passwd) {
        this.message = message;
        this.vehicle = vehicle;
        this.key = key;
        this.name = name;
        this.passwd = passwd;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public char[] getPasswd() {
        return passwd;
    }

    public void setPasswd(char[] passwd) {
        this.passwd = passwd;
    }
}