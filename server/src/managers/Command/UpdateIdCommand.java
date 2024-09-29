package managers.Command;

import managers.Receiver;
import system.Request;

/**
 * Данная команда обновляет значения полей элемента по его ID
 *
 * @see BaseCommand
 * @since 1.0
 */
public class UpdateIdCommand implements BaseCommand{
    @Override
    public String execute(Request request) throws Exception{
        try {
            return Receiver.updateId(request);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "updateId {element} ";
    }

    @Override
    public String getDescription() {
        return "update element";
    }
}
