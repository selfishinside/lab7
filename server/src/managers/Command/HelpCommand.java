package managers.Command;

import exceptions.WrongArgumentException;
import managers.CommandManager;
import managers.Receiver;
import system.Request;

import java.util.LinkedHashMap;

/**
 * Данная команда выводит все возможные команды и их описание
 *
 * @see BaseCommand
 * @since 1.0
 */
public class HelpCommand implements BaseCommand{

    @Override
    public String execute(Request request) throws Exception{
        try {
            return Receiver.getHelp(request);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "help ";
    }

    @Override
    public String getDescription() {
        return "Command to get information";
    }
}
