package managers.Command;

import collection.Vehicle;
import exceptions.NoArgumentException;
import exceptions.WrongArgumentException;
import managers.CollectionManager;
import managers.Receiver;
import system.Request;

import java.util.HashSet;

/**
 * Данная команда выводит колличество элементов, которые не совпадают с указанным типом топлива
 *
 * @see BaseCommand
 * @since 1.0
 */
public class CountLessThenFuelTypeCommand implements BaseCommand{
    @Override
    public String execute(Request request) throws Exception{
        try {
            return Receiver.countLessThenFuelType(request);
        } catch (Exception e) {
            return e.getMessage();
        }

    }

    @Override
    public String getName() {
        return "countLessThenFuelType fuelType ";
    }

    @Override
    public String getDescription() {
        return "number of elements that don't match by FuelType";
    }
}


