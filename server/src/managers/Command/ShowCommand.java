package managers.Command;

import collection.Vehicle;
import exceptions.WrongArgumentException;
import managers.CollectionManager;
import managers.Receiver;
import system.Request;

import java.util.HashSet;

/**
 * Данная команда выводит все элементы коллекции
 *
 * @see BaseCommand
 * @since 1.0
 */
public class ShowCommand implements BaseCommand{


    @Override
    public String execute(Request request) throws Exception{
        try {
            return Receiver.showData(request);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "show ";
    }

    @Override
    public String getDescription() {
        return "show data";
    }
}
