package managers.Command;

import managers.Receiver;
import system.Request;

/**
 * Данная команда удаляет все элементы, значение engine power которых меньше чем у заданного
 *
 * @see BaseCommand
 * @since 1.0
 */

public class RemoveLowerCommand implements BaseCommand{
    @Override
    public String execute(Request request) throws Exception{
        try {
            return Receiver.removeLower(request);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "removeLower {element} ";
    }

    @Override
    public String getDescription() {
        return "remove elements with lower then the element";
    }
}
