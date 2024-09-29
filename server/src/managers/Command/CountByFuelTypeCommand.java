package managers.Command;

import collection.Vehicle;
import exceptions.NoArgumentException;
import exceptions.NoElementException;
import exceptions.WrongArgumentException;
import managers.CollectionManager;
import managers.Receiver;
import system.Request;

import java.util.HashSet;

/**
 * Данная команда выводит колличество элементов с указанным типом топлива
 *
 * @see BaseCommand

 * @since 1.0
 */
public class CountByFuelTypeCommand implements BaseCommand{
    @Override
    public String execute(Request request) throws Exception{
        try {
            return Receiver.countByFuelType(request);
        } catch (Exception e) {
            return e.getMessage();
        }

    }

    @Override
    public String getName() {
        return "countByFuelType fuelType ";
    }

    @Override
    public String getDescription() {
        return "number of elements that match by FuelType";
    }
}
