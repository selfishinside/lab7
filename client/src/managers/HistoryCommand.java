package managers;


import java.util.Deque;

/**
 * Данная команда выводит историю введенных команд
 * Максимум 12 последних команд
 *
 * @since 1.0
 */
public class HistoryCommand {
    public static void execute(Deque<String> historyCom) throws Exception {
        synchronized (historyCom) {
            if (historyCom.isEmpty()) {
                System.out.println("No commands have been executed");
            } else {
                historyCom.forEach(System.out::println);
            }
        }
    }

    public String getName() {
        return "history ";
    }

    public String getDescription() {
        return "show last twelve commands";
    }
}

