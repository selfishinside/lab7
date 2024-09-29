package exceptions;

public class NoElementException extends Exception{
    /**
     * Исключение по ключу
     *
     * @param key ключ элемента
     * @since 1.0
     */
    public NoElementException(String key){
        super("Error with the key " + key);
    }
    /**
     * Конструктор с ID
     *
     * @param id ID элемента
     * @since 1.0
     */
    public NoElementException(Long id){
        super("No element in collection with id: " + id );
    }
}