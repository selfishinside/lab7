package managers;

import collection.FuelType;
import collection.Vehicle;
import collection.VehicleType;
import exceptions.WrongArgumentException;

import java.util.HashSet;

/**
 * Класс для проверки на валидность полей объекта Vehicle и для проверки входных данных из файла
 *
 * @since 1.0
 */
public class Validator {
    HashSet<Vehicle> vehicleList;

    public Validator(HashSet<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }

    /**
     * Проверяет, что значение строки не null
     *
     * @param arg  аргумент строки
     * @param data что из Organization мы проверяем
     * @throws WrongArgumentException если значение arg null
     */
    public static void inputNotEmpty(String arg, String data) throws WrongArgumentException {
        if (arg.isEmpty() || arg.trim().isEmpty()) {
            throw new WrongArgumentException(data);
        }
    }

    /**
     * Проверяет корректность координат по X
     *
     * @param arg аргумент строки
     * @throws WrongArgumentException если координата некорректна
     * @see collection.Coordinates
     */
    public static void xGood(String arg) throws WrongArgumentException {
        long v = Long.parseLong(arg);
        if (v <= -952 || (v >= 9223372036854775807l)) {
            throw new WrongArgumentException("x");
        }
    }

    /**
     * Проверяет корректность координат по Y
     *
     * @param arg аргумент строки
     * @throws WrongArgumentException если координата некорректна
     * @see collection.Coordinates
     */
    public static void yGood(String arg) throws WrongArgumentException {
        int v = Integer.parseInt(arg);
        if ((v <= -109) || (v >= 2147483647)) {
            throw new WrongArgumentException("y");
        }
    }

    /**
     * Проверяет корректность значения engine power
     *
     * @param arg аргумент строки
     * @throws WrongArgumentException если некорректно
     * @see Vehicle
     */
    public static void enginePowerGood(String arg) throws WrongArgumentException {
        int v = Integer.parseInt(arg);
        if (v <= 0) {
            throw new WrongArgumentException("engine power");
        }
    }

    /**
     * Проверяет корректность значения Vehicle type
     *
     * @param arg аргумент строки
     * @throws WrongArgumentException если некорректно
     * @see VehicleType
     */
    public static void typeGood(String arg) throws WrongArgumentException {
        try {
            VehicleType.valueOf(arg.toUpperCase());
        } catch (Exception e) {
            throw new WrongArgumentException("vehicle type");
        }
    }

    /**
     * Проверяет корректность значения Fuel Type
     *
     * @param arg аргумент строки
     * @throws WrongArgumentException если некорректно
     * @see FuelType
     */
    public static void fuelTypeGood(String arg) throws WrongArgumentException {
        try {
            FuelType.valueOf(arg.toUpperCase());
        } catch (Exception e) {
            throw new WrongArgumentException("fuel type");
        }
    }
}