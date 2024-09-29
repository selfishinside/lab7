package managers.Command;

import managers.Receiver;
import system.Request;

/**
 * Данная команда удаляет элемент из коллекции по его ID
 *
 * @see BaseCommand
 * @since 1.0
 */
public class RemoveById implements BaseCommand{
    @Override
    public String execute(Request request) throws Exception{
        try {
            return Receiver.removeById(request);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "removeById id ";
    }

    @Override
    public String getDescription() {
        return "Remove element by id";
    }
}
