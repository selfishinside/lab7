package managers;


import collection.Vehicle;
import client.Client;
import exceptions.*;
import system.Request;
import system.Server;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Данная команда выполняет скрипт файл
 *
 * @since 1.0
 */
public class ExecuteScriptCommand {
    private static Stack<File> stack = new Stack<>();

    public static void execute(String command) throws Exception {
        try {

            String[] argums = command.trim().split("\\s+");
            if (argums.length > 2) {
                throw new WrongArgumentException(argums[2]);
            }
            if (argums.length < 2) {
                throw new NoArgumentException("script name");
            }
        }catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        File file = new File(command.trim().split("\\s+")[1]);
        try {
            if (!file.canRead() || !file.exists()) {
                throw new RootException();
            }
            else if (stack.contains(file)) {
                throw new RecursionException();
            }
            stack.add(file);

        } catch (RecursionException ex) {
            stack.pop();
            System.out.println(ex.getMessage());
            System.out.println("Script Executing finished with error");
            if (!stack.isEmpty()) {
                stack.pop();
            }
            return;
        } catch (RootException e) {
            System.out.println(e.getMessage());
            return;
        }

        String path = command.trim().split("\\s+")[1];
        var br = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
        String line;
        String[] vehicleData = new String[7];
        boolean isSpecialCommandExecuted = false;
        while ((line = br.readLine()) != null) {
            isSpecialCommandExecuted = false;
            String[] parts = line.split(" ", 2);
            switch (parts[0]) {
                case "add":
                    try {
                        if (parts.length > 1) {
                            System.out.println("The 'add' command should not have any arguments.\n" + "Script " + path + " executing finished with error");
                            stack.pop();
                            return;
                        }
                        for (int i = 0; i < vehicleData.length; i++) {
                            if (i == 6) {
                                vehicleData[i] = new Date(0).toString();
                            } else {
                                vehicleData[i] = br.readLine();
                                try {
                                    switch (i) {
                                        case 0:
                                            Validator.inputNotEmpty(vehicleData[i], "name");
                                            break;
                                        case 1:
                                            Validator.inputNotEmpty(vehicleData[i], "x");
                                            Validator.xGood(vehicleData[i]);
                                            break;
                                        case 2:
                                            Validator.inputNotEmpty(vehicleData[i], "y");
                                            Validator.yGood(vehicleData[i]);
                                        case 3:
                                            Validator.inputNotEmpty(vehicleData[i], "engine power");
                                            Validator.enginePowerGood(vehicleData[i]);
                                            break;
                                        case 4:
                                            Validator.inputNotEmpty(vehicleData[i], "vehicle type");
                                            Validator.typeGood(vehicleData[i]);
                                            break;
                                        case 5:
                                            Validator.inputNotEmpty(vehicleData[i], "fuel type");
                                            Validator.fuelTypeGood(vehicleData[i]);
                                            break;
                                    }
                                } catch (WrongArgumentException e) {
                                    System.out.println(e.getMessage());
                                    return;
                                }
                            }
                        }
                        isSpecialCommandExecuted = true;
                        Server.sendRequest(new Request("add", new Vehicle(vehicleData), null, Client.getInstance().getName(), Client.getInstance().getPasswd()));
                        vehicleData = new String[7];
                    }catch (NullPointerException e) {
                        System.out.println("Error in command: absent new data");
                        System.out.println("Script " + path + " executing finished with error");
                        stack.pop();
                        return;
                    }catch (Exception e) {
                        System.out.println("Error in command: " + e.getMessage());
                        System.out.println("Script " + path + " executing finished with error");
                        stack.pop();
                        return;
                    }
                    break;

                case "executeScript":
                    if (parts.length > 2 || parts.length == 1) {
                        System.out.println("The 'executeScript' command should have only 1 argument.\n" + "Script " + path + " executing finished with error");
                        stack.pop();
                        return;
                    }
                    try {

                        String scr = parts[1];
                        File scFile = new File(scr);
                        if (!scFile.canRead()) {
                            throw new RootException();
                        }
                        if (stack.contains(scFile)) {
                            throw new RecursionException();
                        } else {
                            execute(parts[0] + " " + parts[1]);
                        }
                    } catch (RecursionException e) {
                        stack.pop();
                        System.out.println(e.getMessage());
                        System.out.println("Script " + path + " executing finished with error");
                        if (!stack.isEmpty()) {
                            stack.pop();
                        }
                        return;
                    }
                    isSpecialCommandExecuted = true;
                    break;

                case "history":
                    try {
                        if (parts.length > 1) {
                            System.out.println("Command should not contain any argument\n" + "Script " + path + " executing finished with error");
                            stack.pop();
                            return;
                        }
                        HistoryCommand.execute(Server.getLastTwelveCommands());
                    }catch (Exception e) {
                        System.out.println("Error in command: " + e.getMessage());
                        System.out.println("Script " + path + " executing finished with error");
                        stack.pop();
                        return;
                    }
                    isSpecialCommandExecuted = true;
                    break;

                case "updateId":
                    try {
                        if (parts.length < 2) {
                            System.out.println("No argument in command: id\n" + "Script " + path + " executing finished with error");
                            stack.pop();
                            return;
                        }
                        String strId = parts[1];
                        long id = Long.parseLong(strId);

                        for (int i = 0; i < vehicleData.length; i++) {
                            vehicleData[i] = br.readLine();
                            try {
                                switch (i) {
                                    case 0:
                                        Validator.inputNotEmpty(vehicleData[i], "name");
                                        break;
                                    case 1:
                                        Validator.inputNotEmpty(vehicleData[i], "x");
                                        Validator.xGood(vehicleData[i]);
                                        break;
                                    case 2:
                                        Validator.inputNotEmpty(vehicleData[i], "y");
                                        Validator.yGood(vehicleData[i]);
                                    case 3:
                                        Validator.inputNotEmpty(vehicleData[i], "engine power");
                                        Validator.enginePowerGood(vehicleData[i]);
                                        break;
                                    case 4:
                                        Validator.inputNotEmpty(vehicleData[i], "vehicle type");
                                        Validator.typeGood(vehicleData[i]);
                                        break;
                                    case 5:
                                        Validator.inputNotEmpty(vehicleData[i], "fuel type");
                                        Validator.fuelTypeGood(vehicleData[i]);
                                        break;
                                }
                            } catch (WrongArgumentException e) {
                                System.out.println(e.getMessage());
                                return;
                            }
                        }
                        Server.sendRequest(new Request("updateId " + id, new Vehicle(vehicleData), strId, Client.getInstance().getName(), Client.getInstance().getPasswd()));
                        vehicleData = new String[7];
                        isSpecialCommandExecuted = true;

                    } catch (NumberFormatException e) {
                        System.out.println("ID must be a valid number: " + e.getMessage());
                        System.out.println("Script " + path + " executing finished with error");
                        stack.pop();
                        return;
                    } catch (NullPointerException e) {
                        System.out.println("Error in command: absent new data");
                        System.out.println("Script " + path + " executing finished with error");
                        stack.pop();
                        return;
                    }
                    catch (Exception e) {
                        System.out.println("Error while updating: " + e.getMessage());
                        System.out.println("Script " + path + " executing finished with error");
                        stack.pop();
                        return;
                    }
                    break;

                case "addIfMax":
                    try {
                        if (parts.length > 1) {
                            System.out.println("The 'addIfMax' command should not have any arguments.\n" + "Script " + path + " executing finished with error");
                            stack.pop();
                            return;
                        }
                        for (int i = 0; i < vehicleData.length; i++) {
                            if (i == 6) {
                                vehicleData[i] = new Date(0).toString();
                            } else {
                                vehicleData[i] = br.readLine();
                                try {
                                    switch (i) {
                                        case 0:
                                            Validator.inputNotEmpty(vehicleData[i], "name");
                                            break;
                                        case 1:
                                            Validator.inputNotEmpty(vehicleData[i], "x");
                                            Validator.xGood(vehicleData[i]);
                                            break;
                                        case 2:
                                            Validator.inputNotEmpty(vehicleData[i], "y");
                                            Validator.yGood(vehicleData[i]);
                                        case 3:
                                            Validator.inputNotEmpty(vehicleData[i], "engine power");
                                            Validator.enginePowerGood(vehicleData[i]);
                                            break;
                                        case 4:
                                            Validator.inputNotEmpty(vehicleData[i], "vehicle type");
                                            Validator.typeGood(vehicleData[i]);
                                            break;
                                        case 5:
                                            Validator.inputNotEmpty(vehicleData[i], "fuel type");
                                            Validator.fuelTypeGood(vehicleData[i]);
                                            break;
                                    }
                                } catch (WrongArgumentException e) {
                                    System.out.println(e.getMessage());
                                    return;
                                }
                            }
                        }
                        Server.sendRequest(new Request("addIfMax", new Vehicle(vehicleData), null, Client.getInstance().getName(), Client.getInstance().getPasswd()));
                        vehicleData = new String[7];
                        isSpecialCommandExecuted = true;
                    } catch (NumberFormatException e) {
                        System.out.println("ID must be a valid number: " + e.getMessage());
                        System.out.println("Script " + path + " executing finished with error");
                        return;
                    } catch (NoArgumentException e) {
                        System.out.println(e.getMessage());
                    }catch (NullPointerException e) {
                        System.out.println("Error in command: absent new data");
                        System.out.println("Script " + path + " executing finished with error");
                        stack.pop();
                        return;
                    }
                    catch (Exception e) {
                        System.out.println("Error in command: " + e.getMessage());
                        System.out.println("Script " + path + " executing finished with error");
                        stack.pop();
                        return;
                    }
                    break;
            }
            if(!isSpecialCommandExecuted) {
                try {
                    if (line.contains("removeById") || line.contains("removeLower") || line.contains("countByFuelType") || line.contains("countLessThenFuelType")) {
                        if (parts.length < 2) {
                            throw new NoArgumentException("key");
                        } else {
                            Server.sendRequest(new Request(line, null, parts[1], Client.getInstance().getName(), Client.getInstance().getPasswd()));
                        }
                    } else {
                        Server.sendRequest(new Request(line, null, null, Client.getInstance().getName(), Client.getInstance().getPasswd()));
                    }
                }catch (NoArgumentException e) {
                    System.out.println(e.getMessage());
                    System.out.println("Script " + path + " executing finished with error");
                    stack.pop();
                    return;
                }
            }
        }
        try {
            stack.pop();
            System.out.println("Script " + path + " was completed");
        }catch (EmptyStackException e) {
            System.out.println("");
        }
    }
}