package managers.Command;

import exceptions.WrongArgumentException;
import managers.CollectionManager;
import managers.Receiver;
import system.Request;

/**
 * Данная команда очищает коллекцию
 *
 * @see BaseCommand
 * @since 1.0
 */
public class ClearCommand implements BaseCommand{
    @Override
    public String execute(Request request) throws Exception{
        try {
            return Receiver.clearCollection(request);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "clear ";
    }

    @Override
    public String getDescription() {
        return "Clears the collection";
    }
}
