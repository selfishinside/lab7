package managers.Command;

import managers.CollectionManager;
import system.Request;



/**
 * Данная команда сохраняет коллекцию в XML файл
 *
 * @see BaseCommand
 * @since 1.0
 */
public class SaveCommand implements BaseCommand{
    @Override
    public String execute(Request request) throws Exception{
       CollectionManager writer = CollectionManager.getInstance();
       writer.writeCollectionToDB();
       return "Data was saved";
    }

    @Override
    public String getName() {
        return "save ";
    }

    @Override
    public String getDescription() {
        return "Save collection to xml file";
    }
}
