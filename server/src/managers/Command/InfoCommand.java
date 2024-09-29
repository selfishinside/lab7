package managers.Command;

import exceptions.WrongArgumentException;
import managers.CollectionManager;
import managers.Receiver;
import system.Request;

/**
 * Данная команда выводит информацию о коллекции
 *
 * @see BaseCommand
 * @since 1.0
 */
public class InfoCommand implements BaseCommand{
    @Override
    public String execute(Request request) throws Exception{
        try {
            return Receiver.getInfo(request);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "info ";
    }

    @Override
    public String getDescription() {
        return "information about collection";
    }
}
