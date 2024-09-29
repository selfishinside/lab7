package managers.Command;

import managers.Receiver;
import system.Request;

public class LoginCommand implements BaseCommand{
    @Override
    public String execute(Request request) throws Exception {
        try {
            return Receiver.login(request);
        }catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "login";
    }

    @Override
    public String getDescription() {
        return "Command for auth user";
    }
}
