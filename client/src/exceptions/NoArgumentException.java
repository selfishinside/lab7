package exceptions;

/**
 * Исключение если команда не имеет никаких аргументов
 *
 * @since 1.0
 */
public class NoArgumentException extends Exception{

    public NoArgumentException(String argument) {
        super("No argument in command: " + argument);
    }
}