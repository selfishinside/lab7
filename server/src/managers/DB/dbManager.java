package managers.DB;

import collection.Vehicle;

import java.util.Set;

public interface dbManager {
    Set<Vehicle> getCollectionFromDB();

    void writeCollectionToDB();
}

