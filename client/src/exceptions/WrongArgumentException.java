package exceptions;
/**
 * Обеспечивает исключение, если такой возникла ошибка с аргументом
 *
 * @since 1.0
 */
public class WrongArgumentException extends Exception{
    /**
     * @param argument название аргумента, который был введен неправильно
     * @since 1.0
     */
    public WrongArgumentException(String argument){
        super("Something wrong with input argument: " + argument);
    }
}