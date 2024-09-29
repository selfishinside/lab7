package managers;

import collection.Vehicle;
import clientLog.ClientHandler;
import exceptions.NoElementException;
import exceptions.WrongArgumentException;
import managers.Command.BaseCommand;
import managers.DB.PostgreSQLManager;
import system.Request;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

public class Receiver {
    public static String addNewEl(Request request) throws WrongArgumentException {
        if (request.getMessage().split(" ").length == 1 ) {
            PostgreSQLManager manager = new PostgreSQLManager();
            Vehicle obj = request.getVehicle();
            obj.setCreationDate(new Date());
            long generatedId = manager.writeObjToDB(obj);
            if (generatedId != -1) {
                CollectionManager.getInstance().loadCollectionFromDB();
            }
            return "Element was added";
        }else throw new WrongArgumentException("Add command must not contain arguments");
    }

    public static String addIfMax(Request request) throws WrongArgumentException {
        if(request.getMessage().split(" ").length == 1) {
            return CollectionManager.addIfMax(request);
        } else {
            throw new WrongArgumentException("AddIfMax command must not contain arguments");
        }
    }

    public static String updateId(Request request) throws WrongArgumentException {
        if(request.getMessage().split(" ").length == 2) {
            PostgreSQLManager manager = new PostgreSQLManager();
            long inputEl = Long.parseLong(request.getKey());
            Vehicle obj = request.getVehicle();
            obj.setCreationDate(new Date());

            if (manager.isVehicleOwnedByUser(inputEl)) {
                if (manager.updateVehicle(obj)) {
                    CollectionManager.getInstance().loadCollectionFromDB();
                    return "element was updated";
                }
                return "Failed to update element";
            }
            else {
                return "Is not your vehicle";
            }

        } else {
            throw new WrongArgumentException("updateId command must contain only one required argument");
        }
    }

    public static String clearCollection(Request request) throws WrongArgumentException {
        if(request.getMessage().split(" ").length == 1) {
            PostgreSQLManager manager = new PostgreSQLManager();
            List<Long> deletedVehicleId = manager.clearVehiclesForUser();
            if(!deletedVehicleId.isEmpty()) {
                CollectionManager.getInstance().getVehicleCollection().removeIf(vehicle -> deletedVehicleId.contains(vehicle.getId()));
                return "Collection cleared";
            } else {
                return "Collection already cleared only your elements was removed";
            }
        } else {
            throw new WrongArgumentException("Clear command must not contain arguments");
        }
    }

    public static String exit(Request request) throws WrongArgumentException {
        if(request.getMessage().split(" ").length == 1) {
            System.exit(1);
            return "";
        } else {
            throw new WrongArgumentException("Exit command must not contain arguments");
        }
    }

    public static String register(Request request) {
        if (request.getName() != null && !request.getName().isEmpty() &&
                request.getPasswd() != null && request.getPasswd().length > 0) {
            ClientHandler clientHandler = new ClientHandler(request.getName(), request.getPasswd());
            if (clientHandler.regUser()) {
                return "Registration successful";
            } else {
                return "Registration failed: username may already exists or other DB error";
            }
        }
        return "Invalid input: username or password missing";
    }

    public static String login(Request request) {
        if (request.getName() != null && !request.getName().isEmpty() &&
                request.getPasswd() != null && request.getPasswd().length > 0) {
            ClientHandler clientHandler = new ClientHandler(request.getName(), request.getPasswd());
            if (clientHandler.authUser()) {
                return "Authentication successful. User ID: " + ClientHandler.getUserId();
            } else {
                return "Authentication failed. Wrong username or password.";
            }
        }
        return "Invalid input: username or password missing";
    }

    public static String removeById(Request request) throws WrongArgumentException, NoElementException{
        if(request.getMessage().split(" ").length == 2) {
            PostgreSQLManager manager = new PostgreSQLManager();
            long inputEl = Long.parseLong(request.getKey());
            if (manager.isVehicleOwnedByUser(inputEl)) {
                if (manager.removeVehicleById(inputEl)) {
                    CollectionManager.getInstance().loadCollectionFromDB();
                    return "Element was removed";
                }
            } else {
                return "Is not your vehicle";
            }

            return "No element with this id: " + inputEl;
        } else {
            throw new WrongArgumentException("removeById command must contain only one required argument");
        }
    }

    public static String getInfo(Request request) throws WrongArgumentException {
        StringBuilder text = new StringBuilder("");
        if (request.getMessage().split(" ").length == 1) {
            text.append("Data type: " + CollectionManager.getInstance().getVehicleCollection().getClass());
            text.append("\nInit data: " + CollectionManager.getInstance().getCreationDate());
            text.append("\nSize: " + CollectionManager.getInstance().getVehicleCollection().size());
            return text.toString();
        } else {
            throw new WrongArgumentException("Info command must not contain arguments");
        }
    }

    public static String getHelp(Request request) throws WrongArgumentException {
        StringBuilder text = new StringBuilder("");
        if (request.getMessage().split(" ").length == 1) {
            LinkedHashMap<String, BaseCommand> commandList = CommandManager.getCommandList();
            int maxNameLenght = 0;
            for (String name : commandList.keySet()) {
                if (name.length() > maxNameLenght) {
                    maxNameLenght = name.length();
                }
            }
            String formatString = "%-" + (maxNameLenght + 2) + "s - %s\n";
            for (String name : commandList.keySet()) {
                if(!name.equals("save")) {
                    BaseCommand command = commandList.get(name);
                    text.append(String.format(formatString, command.getName(), command.getDescription()));
                }
            }
            String executeScriptDescription = "Execute script from file.";
            String historyDescription = "History of your twelve last commands";
            text.append(String.format(formatString, "executeScript {file}", executeScriptDescription));
            text.append(String.format(formatString, "history", historyDescription));
            return text.toString();
        } else {
            throw new WrongArgumentException("Help command must not contain arguments");
        }
    }

//    public static String saveData() throws IOException, RootException, WrongArgumentException {
////        if (request.getMessage().split(" ").length == 1) {
////            try {
////                FileManager.getInstance(Server.data_path).writeCollection(CollectionManager.getVehicleCollection());
////            }catch (Exception e) {
////                System.out.println(e.getMessage());
////                throw e;
////            }
////        } else {
////            throw new WrongArgumentException("Save command must not contain arguments");
////        }
////        return "Data was saved";
//    }

    public static String showData(Request request) throws WrongArgumentException {
        if (request.getMessage().split(" ").length == 1) {
            CollectionManager collectionManager = CollectionManager.getInstance();
            return collectionManager.showCollection();
        } else {
            throw new WrongArgumentException("show command must not contain arguments");
        }
    }

    public static String removeLower(Request request) throws WrongArgumentException {
        if (request.getMessage().split(" ").length == 2) {
            PostgreSQLManager manager = new PostgreSQLManager();
            if (manager.removeVehiclesByList(CollectionManager.removeLower(request))) {
                CollectionManager.getInstance().loadCollectionFromDB();
                return "all your lower elements removed";
            } else {
                return "elements not removed. You don't have enough rights to delete or there are no lower items.\n" +
                        "check that the item is in the collection";
            }
        } else {
            throw new WrongArgumentException("removeLower command must contain only one required argument");
        }
    }

    public static String groupCountingByCreationDate(Request request) throws WrongArgumentException {
        if (request.getMessage().split(" ").length == 1) {
            return CollectionManager.GroupCountingByCreationDate(request);
        } else {
            throw new WrongArgumentException("groupCountingByCreationDate command must not contain arguments");
        }
    }

    public static String countByFuelType(Request request) throws WrongArgumentException {
        if (request.getMessage().split(" ").length == 2) {
            return CollectionManager.countByFuelType(request);
        } else {
            throw new WrongArgumentException("countByFuelType command must contain only one required argument");
        }
    }

    public static String countLessThenFuelType(Request request) throws WrongArgumentException {
        if (request.getMessage().split(" ").length == 2) {
            return CollectionManager.countLessThenFuelType(request);
        } else {
            throw new WrongArgumentException("countLessThenFuelType command must contain only one required argument");
        }
    }







}