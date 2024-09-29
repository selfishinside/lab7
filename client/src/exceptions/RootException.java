package exceptions;

import managers.ExecuteScriptCommand;

/**
 * Обеспечивает исключение, если программа не имеет прав доступа к какому-либо файлу
 * @see ExecuteScriptCommand
 * @since 1.0
 */
public class RootException extends Exception{

    public RootException() {
        super("You do not have enough rights to read the file");
    }
}
