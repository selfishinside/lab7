package managers;

import exceptions.UnknowCommandException;
import managers.Command.*;
import system.Request;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;

/**
 * Класс обеспечивает связь между командами и CollectionManager
 *
 * @see CollectionManager
 * @since 1.0
 */
public class CommandManager {
    private static LinkedHashMap<String,BaseCommand> commandList;
    public static ArrayDeque<BaseCommand> lastTwelveCommand = new ArrayDeque<>();

    public CommandManager() {
        commandList = new LinkedHashMap<>();
        commandList.put("help", new HelpCommand());
        commandList.put("info", new InfoCommand());
        commandList.put("show", new ShowCommand());
        commandList.put("removeById", new RemoveById());
        commandList.put("clear", new ClearCommand());
        commandList.put("save", new SaveCommand());
        commandList.put("exit", new ExitCommand());
        commandList.put("add", new AddCommand());
        commandList.put("removeLower", new RemoveLowerCommand());
        commandList.put("updateId", new UpdateIdCommand());
        commandList.put("addIfMax", new AddIfMaxCommand());
        commandList.put("register", new RegisterCommand());
        commandList.put("login", new LoginCommand());
        commandList.put("countByFuelType", new CountByFuelTypeCommand());
        commandList.put("countLessThenFuelType", new CountLessThenFuelTypeCommand());
        commandList.put("groupCountingByCreationDate", new GroupCountingByCreationDateCommand());
    }

    /**
     * Выполняет команду, сохраняя ее имя
     *
     * @since 1.0
     */
    public static String startExecute(Request request) throws Exception{
        String commandName = request.getMessage().split(" ")[0];
        if (!commandList.containsKey(commandName)) {
            throw new UnknowCommandException(commandName);
        }
        BaseCommand command = commandList.get(commandName);
        String message = command.execute(request);
        if (lastTwelveCommand.size() >= 12) {
            lastTwelveCommand.removeLast();
        } else {
            lastTwelveCommand.addFirst(command);
        }
        return message;
    }

    public static String startExecutingClientMode(Request request) {
        try {
            if(!request.getMessage().equals("exit") && !request.getMessage().equals("save")) {
                return startExecute(request);
            }
            return "Unknown command";
        }catch (Exception e) {
            return e.getMessage();
        }
    }

    public static void startExecutingServerMode(Request request) {
        try {
            if(request.getMessage().equals("exit") || request.getMessage().equals("save")) {
                startExecute(request);
            }
        }catch (Exception e) {
            System.out.println("Something wrong with initializing of server");
        }
    }

    public static LinkedHashMap<String, BaseCommand> getCommandList() {
        return commandList;
    }

}