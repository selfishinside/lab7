package managers.Command;

import managers.Receiver;
import system.Request;

/**
 * Данная команда добавляет элемент в коллекцию, если его engine power больше всех в коллекции
 *
 * @see BaseCommand

 * @since 1.0
 */
public class AddIfMaxCommand implements BaseCommand{
    @Override
    public String execute(Request request) throws Exception{
        try {
            return Receiver.addIfMax(request);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "addIfMax ";
    }

    @Override
    public String getDescription() {
        return "adds an element to the collection if its Engine Power is greater than the existing ones";
    }
}
