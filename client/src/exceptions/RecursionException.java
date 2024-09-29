package exceptions;

import managers.ExecuteScriptCommand;

/**
 * Обеспечивает исключение при появлении рекурсии во время выполнения скрипт файла
 * @see ExecuteScriptCommand
 * @since 1.0
 */
public class RecursionException extends Exception{
    public RecursionException(){
        super("Recursion in file");
    }
}
