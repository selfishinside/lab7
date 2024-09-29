package managers.DB;

import collection.Coordinates;
import collection.FuelType;
import collection.Vehicle;
import collection.VehicleType;
import clientLog.ClientHandler;
import clientLog.PasswdHandler;
import managers.CollectionManager;

import java.security.SecureRandom;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PostgreSQLManager implements dbManager {
    @Override
    public HashSet<Vehicle> getCollectionFromDB() {
        HashSet<Vehicle> data = new HashSet<>();

        try (Connection connection = ConnectionFactory.getConnection();
             Statement statement = connection.createStatement()) {

            String query = "SELECT v.*, co.x, co.y " +
                    "FROM vehicle v " +
                    "JOIN coordinates co ON v.coordinates_id = co.id";

            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String name = resultSet.getString("name");
                Coordinates coordinates = new Coordinates(resultSet.getLong("x"), resultSet.getInt("y"));
                Date creationDate = resultSet.getDate("creation_date");
                Integer enginePower = resultSet.getInt("engine_power");
                VehicleType vehicleType = VehicleType.valueOf(resultSet.getString("vehicle_type"));
                FuelType fuelType = FuelType.valueOf(resultSet.getString("fuel_type"));
                Vehicle vehicle = new Vehicle(id, name, coordinates, creationDate, enginePower, vehicleType, fuelType);
                data.add(vehicle);
            }
            return data;
        }catch (SQLException e) {
            Logger.getLogger(PostgreSQLManager.class.getName()).warning("Something wrong" + e.getMessage());
            e.printStackTrace();
        }
        return data;
    }

    public long writeObjToDB(Vehicle vehicle) {
        long generatedId = -1;
        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            generatedId = addElementToDB(vehicle, connection);
            connection.commit();
        } catch (SQLException e) {
            Logger.getLogger(PostgreSQLManager.class.getName()).warning("Something wrong" + e.getMessage());
        }
        return generatedId;
    }

    @Override
    public void writeCollectionToDB() {
        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            Set<Long> existVehicleId = new HashSet<>();
            String getVehicleIdQuery = "SELECT id FROM vehicle";
            PreparedStatement getVehicleIdStatement = connection.prepareStatement(getVehicleIdQuery);
            ResultSet vehicleIdResultSet = getVehicleIdStatement.executeQuery();
            while (vehicleIdResultSet.next()) {
                existVehicleId.add(vehicleIdResultSet.getLong("id"));
            }

            for (Vehicle vehicle : CollectionManager.getInstance().getVehicleCollection()) {
                if(!existVehicleId.contains(vehicle.getId())) {
                    vehicle.setId(addElementToDB(vehicle, connection));
                }
            }
            connection.commit();
        }catch (SQLException e) {
            Logger.getLogger(PostgreSQLManager.class.getName()).warning("Something wrong" + e.getMessage());
        }
    }

    public long addElementToDB(Vehicle vehicle, Connection connection) {
        long generatedId = -1;
        PreparedStatement inCoordStatement = null;
        PreparedStatement inVehicleStatement = null;
        PreparedStatement inCreatorStatement = null;

        try {
            connection.setAutoCommit(false);
            String inCoordQuery = "INSERT INTO coordinates (x, y) VALUES (?, ?) RETURNING id";
            inCoordStatement = connection.prepareStatement(inCoordQuery);
            inCoordStatement.setLong(1,vehicle.getCoordinates().getX());
            inCoordStatement.setInt(2, vehicle.getCoordinates().getY());
            ResultSet coordResultSet = inCoordStatement.executeQuery();
            Logger.getLogger(PostgreSQLManager.class.getName()).info("Coord was added");

            int coordId = -1;
            if (coordResultSet.next()) {
                coordId = coordResultSet.getInt(1);
            }

            String inVehicleQuery = "INSERT INTO vehicle (name, coordinates_id, creation_date, engine_power, vehicle_type, fuel_type)" +
                    "VALUES (?, ?, ?, ?, CAST(? AS vehicletype_enum), CAST(? AS fueltype_enum)) RETURNING id";
            inVehicleStatement = connection.prepareStatement(inVehicleQuery);
            inVehicleStatement.setString(1, vehicle.getName());
            inVehicleStatement.setInt(2, coordId);
            inVehicleStatement.setDate(3, new java.sql.Date(vehicle.getCreationDate().getTime()));
            inVehicleStatement.setInt(4,vehicle.getEnginePower());
            inVehicleStatement.setString(5, vehicle.getType().toString());
            inVehicleStatement.setString(6, vehicle.getFuelType().toString());
            ResultSet vehicleResultSet = inVehicleStatement.executeQuery();
            Logger.getLogger(PostgreSQLManager.class.getName()).info("vehicle was added");
            if (vehicleResultSet.next()) {
                generatedId = vehicleResultSet.getLong(1);
            }

            String inCreatorQuery = "INSERT INTO creator (user_id, vehicle_id) VALUES (?, ?) ON CONFLICT (vehicle_id) DO NOTHING";
            inCreatorStatement = connection.prepareStatement(inCreatorQuery);
            inCreatorStatement.setLong(1,ClientHandler.getUserId());
            inCreatorStatement.setLong(2, generatedId);
            inCreatorStatement.executeUpdate();
            Logger.getLogger(PostgreSQLManager.class.getName()).info("creator was added");


            connection.commit();
        } catch (SQLException e) {
            Logger.getLogger(PostgreSQLManager.class.getName()).warning("Error adding element to DB");
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException se) {
                Logger.getLogger(PostgreSQLManager.class.getName()).warning("Error on rollback: " + se.getMessage());
            }
        } finally {
            try {
                if (inCoordStatement != null) inCoordStatement.close();
                if (inVehicleStatement != null) inVehicleStatement.close();
                if (inCreatorStatement != null) inCreatorStatement.close();
            } catch (SQLException e) {
                Logger.getLogger(PostgreSQLManager.class.getName()).warning("Error on close: " + e.getMessage());
            }
        }
        return generatedId;
    }

    public boolean removeVehicleById(long vehicleId) {
        try (Connection connection = ConnectionFactory.getConnection()) {

            String deleteVehicleQuery = "DELETE FROM vehicle WHERE id = ? AND id IN (SELECT vehicle_id FROM creator WHERE user_id = ?)";
            String deleteCoordQuery = "DELETE FROM coordinates WHERE id = ?";


            PreparedStatement deleteVehicleStatement = connection.prepareStatement(deleteVehicleQuery);
            deleteVehicleStatement.setLong(1, vehicleId);
            deleteVehicleStatement.setLong(2, ClientHandler.getUserId());
            int rowsAffectedVehicle = deleteVehicleStatement.executeUpdate();

            PreparedStatement deleteCoordStatement = connection.prepareStatement(deleteCoordQuery);
            deleteCoordStatement.setLong(1, vehicleId);
            int rowsAffectedCoord = deleteCoordStatement.executeUpdate();

            return rowsAffectedVehicle > 0 && rowsAffectedCoord > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeVehiclesByList(List<Long> vehicleIds) {
        if (vehicleIds == null || vehicleIds.isEmpty()) {
            return false;
        }

        List<Long> authVehicleIds = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection()) {

            String selectVehiclesIdQuery = "SELECT vehicle_id FROM creator WHERE user_id = ?";
            PreparedStatement selectedVehiclesIdStatement = connection.prepareStatement(selectVehiclesIdQuery);
            selectedVehiclesIdStatement.setLong(1, ClientHandler.getUserId());
            ResultSet vehicleIdResultSet = selectedVehiclesIdStatement.executeQuery();

            List<Long> userVehiclesIds = new ArrayList<>();
            while (vehicleIdResultSet.next()) {
                userVehiclesIds.add(vehicleIdResultSet.getLong("vehicle_id"));
            }

            authVehicleIds = vehicleIds.stream()
                    .filter(userVehiclesIds::contains)
                    .collect(Collectors.toList());

            if (!authVehicleIds.isEmpty()) {
                String ids = authVehicleIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                String deleteVehQuery = "DELETE FROM vehicle WHERE id IN (" + ids + ")";
                PreparedStatement deleteVehStatement = connection.prepareStatement(deleteVehQuery);
                int vehDeleted = deleteVehStatement.executeUpdate();

                String deleteCoordQuery = "DELETE FROM coordinates WHERE id IN (" + ids + ")";
                PreparedStatement deleteCoordStatement = connection.prepareStatement(deleteCoordQuery);
                int coordsDeleted = deleteCoordStatement.executeUpdate();

                return coordsDeleted > 0 || vehDeleted > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public List<Long> clearVehiclesForUser() {
        long userId = ClientHandler.getUserId();
        List<Long> deletedVehiclesId = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection()) {

            String selectVehiclesIdQuery = "SELECT vehicle_id FROM creator WHERE user_id = ?";
            PreparedStatement selectedVehiclesIdStatement = connection.prepareStatement(selectVehiclesIdQuery);
            selectedVehiclesIdStatement.setLong(1,userId);
            ResultSet vehicleIdResultSet = selectedVehiclesIdStatement.executeQuery();
            List<Long> coordIds = new ArrayList<>();
            while (vehicleIdResultSet.next()) {
                deletedVehiclesId.add(vehicleIdResultSet.getLong("vehicle_id"));
                coordIds.add(vehicleIdResultSet.getLong("vehicle_id"));
            }

            String deleteVehicleQuery = "DELETE FROM vehicle WHERE id IN (SELECT vehicle_id FROM Creator WHERE user_id = ?)";
            PreparedStatement deleteVehiclesStatement = connection.prepareStatement(deleteVehicleQuery);
            deleteVehiclesStatement.setLong(1,userId);
            deleteVehiclesStatement.executeUpdate();

            if (!coordIds.isEmpty()) {
                String ids = coordIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                String deleteCoordQuery = "DELETE FROM coordinates WHERE id IN (" + ids + ")";
                PreparedStatement deleteCoordStatement = connection.prepareStatement(deleteCoordQuery);
                deleteCoordStatement.executeUpdate();
            }

        } catch (SQLException e) {
            Logger.getLogger(PostgreSQLManager.class.getName()).warning("Something wrong" + e.getMessage());
        }
        return deletedVehiclesId;
    }

    public boolean isVehicleOwnedByUser(long vehicleId) {
        try (Connection connection = ConnectionFactory.getConnection()) {

            String checkOwnerQuery = "SELECT COUNT(*) FROM creator WHERE vehicle_id = ? AND user_id = ?";
            PreparedStatement checkOwnerStatement = connection.prepareStatement(checkOwnerQuery);
            checkOwnerStatement.setLong(1, vehicleId);
            checkOwnerStatement.setLong(2,ClientHandler.getUserId());
            ResultSet resultSet = checkOwnerStatement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            Logger.getLogger(PostgreSQLManager.class.getName()).warning("Something wrong" + e.getMessage());
            return false;
        }
        return false;
    }

    public boolean updateVehicle(Vehicle vehicle) {
        try (Connection connection = ConnectionFactory.getConnection()) {

            connection.setAutoCommit(false);

            String updateCoordQuery = "UPDATE coordinates SET x = ?, y = ? FROM vehicle WHERE vehicle.coordinates_id = coordinates.id AND vehicle.id = ?";
            PreparedStatement updateCoordStatement = connection.prepareStatement(updateCoordQuery);
            updateCoordStatement.setLong(1, vehicle.getCoordinates().getX());
            updateCoordStatement.setInt(2, vehicle.getCoordinates().getY());
            updateCoordStatement.setLong(3, vehicle.getId());
            updateCoordStatement.executeUpdate();

            String updateVehicleQuery = "UPDATE vehicle SET name = ?, creation_date = ?, engine_power = ?, vehicle_type = CAST(? AS vehicletype_enum), fuel_type = CAST(? AS fueltype_enum) WHERE id = ?";
            PreparedStatement updateVehicleStatement = connection.prepareStatement(updateVehicleQuery);
            updateVehicleStatement.setString(1, vehicle.getName());
            updateVehicleStatement.setDate(2,new java.sql.Date(vehicle.getCreationDate().getTime()));
            updateVehicleStatement.setInt(3,vehicle.getEnginePower());
            updateVehicleStatement.setString(4, vehicle.getType().toString());
            updateVehicleStatement.setString(5, vehicle.getFuelType().toString());
            updateVehicleStatement.setLong(6, vehicle.getId());
            updateVehicleStatement.executeUpdate();

            connection.commit();
            return true;
        } catch (SQLException e) {
            Logger.getLogger(PostgreSQLManager.class.getName()).warning("Something wrong" + e.getMessage());
        }
        return false;
    }

    public long authUser(String name, char[] passwd) {
        try (Connection connection = ConnectionFactory.getConnection()) {

            String selectUserQuery = "SELECT id, passwd_hash, passwd_salt FROM \"User\" WHERE name = ?";
            PreparedStatement selectUserStatement = connection.prepareStatement(selectUserQuery);
            selectUserStatement.setString(1, name);
            ResultSet resultSet = selectUserStatement.executeQuery();

            if (resultSet.next()) {
                String passwdHash = resultSet.getString("passwd_hash");
                String passwdSalt = resultSet.getString("passwd_salt");
                String inputPasswdHash = PasswdHandler.hashPassword(passwd, passwdSalt);

                if (passwdHash.equals(inputPasswdHash)) {
                    return resultSet.getLong("id");
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(PostgreSQLManager.class.getName()).warning("Something went wrong ");
        }
        return -1;
    }

    public long regUser(String name, char[] passwd) {
        try (Connection connection = ConnectionFactory.getConnection()) {

            String selectUserQuery = "SELECT COUNT(*) FROM \"User\" WHERE name = ?";
            PreparedStatement selectUserStatement = connection.prepareStatement(selectUserQuery);
            selectUserStatement.setString(1, name);
            ResultSet resultSet = selectUserStatement.executeQuery();

            if (resultSet.next() && resultSet.getInt(1) > 0) {
                return -1;
            }

            SecureRandom random = new SecureRandom();
            byte[] saltBytes = new byte[16];
            random.nextBytes(saltBytes);
            String salt = Base64.getEncoder().encodeToString(saltBytes);

            String passwdHash = PasswdHandler.hashPassword(passwd, salt);

            String inUserQuery = "INSERT INTO \"User\" (name, passwd_hash, passwd_salt) VALUES (?, ?, ?)";
            PreparedStatement inUserStatement = connection.prepareStatement(inUserQuery, Statement.RETURN_GENERATED_KEYS);
            inUserStatement.setString(1, name);
            inUserStatement.setString(2, passwdHash);
            inUserStatement.setString(3, salt);

            int rowsAffected = inUserStatement.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = inUserStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(PostgreSQLManager.class.getName()).warning("Something wrong");
        }
        return -1;
    }
}