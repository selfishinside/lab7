package managers;
import collection.Vehicle;
import Comparators.VehicleEnginePowerComparator;
import managers.DB.PostgreSQLManager;
import system.Request;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Класс отвечает за взаимодействие с коллекцией на базовом уровне
 *
 * @see Vehicle

 * @since 1.0
 */

public class CollectionManager {

    private static HashSet<Vehicle> vehicleCollection;
    private final java.util.Date creationDate;
    private static CollectionManager instance;
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Базовый конструктор
     *
     * @since 1.0
     */
    private CollectionManager() {
        vehicleCollection = new HashSet<>();
        creationDate = new java.sql.Date(new Date().getTime());
    }

    public static synchronized CollectionManager getInstance() {
        if (instance == null) {
            instance = new CollectionManager();
        }
        return instance;
    }

    public void loadCollectionFromDB() {
        lock.lock();
        try {
            PostgreSQLManager manager = new PostgreSQLManager();
            HashSet<Vehicle> vehicles = manager.getCollectionFromDB();
            vehicleCollection.clear();
            vehicleCollection.addAll(vehicles);
            Logger.getLogger(CollectionManager.class.getName()).info("Collection reloaded from DB");
    } finally {
            lock.unlock();
        }
    }

    public void writeCollectionToDB() {
        PostgreSQLManager dbmanager = new PostgreSQLManager();
        dbmanager.writeCollectionToDB();
    }

    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Получить коллекцию
     *
     * @return коллекция со всеми элементами
     */
    public HashSet<Vehicle> getVehicleCollection() {
        lock.lock();
        try {
            return vehicleCollection;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Установить коллекцию
     *
     */
    public void setVehicleCollection(HashSet<Vehicle> newVehicleCollection) {
        lock.lock();
        try {
            vehicleCollection.clear();
            vehicleCollection.addAll(newVehicleCollection);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Добавить элемент в коллекцию
     *
     */
    public static void addToCollection(Vehicle vehicle) {
        CollectionManager instance = CollectionManager.getInstance();
        instance.lock.lock();
        try {
            if(!vehicleCollection.contains(vehicle)) {
                vehicleCollection.add(vehicle);
                Logger.getLogger(CollectionManager.class.getName()).info("Vehicle was added" + vehicle);
            } else {
                Logger.getLogger(CollectionManager.class.getName()).warning("Attempt to add duplicate vehicle: " + vehicle);
            }
        } finally {
            instance.lock.unlock();
        }
    }


    /**
     * Показать все элементы коллекции
     *
     */
    public String showCollection() {
        if (vehicleCollection.isEmpty()) {
            return "Collection is empty";
        }

        String result = vehicleCollection.stream()
                .map(Vehicle::toString)
                .collect(Collectors.joining("\n"));
        return result;
    }


    public static List<Long> removeLower(Request request) {
        List<Long> lowerEPVehicleIds = new ArrayList<>();
        try {
            VehicleEnginePowerComparator vehicleEnginePowerComparator = new VehicleEnginePowerComparator();
            HashSet<Vehicle> vehicleCollection = CollectionManager.getInstance().getVehicleCollection();

            long inputEl = Long.parseLong(request.getKey());

            Vehicle referenceVehicle = vehicleCollection.stream()
                    .filter(vehicle -> vehicle.getId() == inputEl)
                    .findFirst()
                    .orElse(null);

            lowerEPVehicleIds = vehicleCollection.stream()
                    .filter(vehicle -> vehicleEnginePowerComparator.compare(vehicle, referenceVehicle) < 0)
                    .map(Vehicle::getId)
                    .collect(Collectors.toList());

            if (referenceVehicle == null) {
                return lowerEPVehicleIds;
            }

            if (lowerEPVehicleIds.isEmpty()) {
                return lowerEPVehicleIds;
            }

        }catch (NumberFormatException e) {
            return lowerEPVehicleIds;
        }catch (NullPointerException e) {
            return lowerEPVehicleIds;
        }
        return lowerEPVehicleIds;
    }

    public static String GroupCountingByCreationDate(Request request) {
        HashSet<Vehicle> vehicleCollection = CollectionManager.getInstance().getVehicleCollection();
        if (vehicleCollection.isEmpty()) {
            return "Collection is empty";
        } else {
            Map<Date, Long> groupedByCreatioonDate = vehicleCollection.stream()
                    .collect(Collectors.groupingBy(Vehicle::getCreationDate,Collectors.counting()));
            String result = groupedByCreatioonDate.entrySet().stream()
                    .map(entry -> entry.getKey() + ": " + entry.getValue())
                    .collect(Collectors.joining("\n"));
            return result;
        }
    }


    public static String countByFuelType(Request request) {
        HashSet<Vehicle> vehicleCollection = CollectionManager.getInstance().getVehicleCollection();
        String enterFuelType = request.getKey();

        long count = vehicleCollection.stream()
                .filter(vehicle -> vehicle.getFuelType().toString().equalsIgnoreCase(enterFuelType))
                .count();
        return ("Coincidences: " + count);
    }

    public static String countLessThenFuelType(Request request) {
        HashSet<Vehicle> vehicleCollection = CollectionManager.getInstance().getVehicleCollection();
        String enterFuelType = request.getKey();

        long count = vehicleCollection.stream()
                .filter(vehicle -> !(vehicle.getFuelType().toString().equalsIgnoreCase(enterFuelType)))
                .count();
        return ("Do not match: " + count);
    }

    public static String addIfMax(Request request) {
        VehicleEnginePowerComparator comparator = new VehicleEnginePowerComparator();
        if(vehicleCollection.isEmpty() || vehicleCollection.stream().allMatch(vehicle -> comparator.compare(vehicle,request.getVehicle()) < 0)) {
            PostgreSQLManager manager = new PostgreSQLManager();
            Vehicle obj = request.getVehicle();
            obj.setCreationDate(new Date());
            long generatedId = manager.writeObjToDB(obj);
            if (generatedId != -1) {
                CollectionManager.getInstance().loadCollectionFromDB();
            }
        } else {
            return "new vehicle has lower engine power!";
        }
        return "successfully added";
    }

}