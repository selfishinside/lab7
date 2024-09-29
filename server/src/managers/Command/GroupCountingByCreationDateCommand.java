package managers.Command;

import collection.Vehicle;
import exceptions.WrongArgumentException;
import managers.CollectionManager;
import managers.Receiver;
import system.Request;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Данная команда группирует элементы коллекции по дате их создания
 *
 * @see BaseCommand
 * @since 1.0
 */
public class GroupCountingByCreationDateCommand implements BaseCommand{
    @Override
    public String execute(Request request) throws Exception{
        try {
            return Receiver.groupCountingByCreationDate(request);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "groupCountingByCreationDate ";
    }

    @Override
    public String getDescription() {
        return "grouping collection elements by creation Date field, " +
                "displaying the number of elements in each group";
    }
}
