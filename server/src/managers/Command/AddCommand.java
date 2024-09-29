package managers.Command;

import managers.Receiver;
import system.Request;

/**
 * Данная команда добавляет новый элемент в коллекцию
 *
 * @see BaseCommand
 * @since 1.0
 */
public class AddCommand implements BaseCommand{
    @Override
    public String execute(Request request) throws Exception{
        try {
            return Receiver.addNewEl(request);
        }catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "add {element} ";
    }

    @Override
    public String getDescription() {
        return "add new vehicle in collection";
    }
}
