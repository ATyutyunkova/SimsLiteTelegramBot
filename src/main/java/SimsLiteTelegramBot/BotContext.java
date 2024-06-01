package SimsLiteTelegramBot;

import java.util.HashMap;

public class BotContext {
    public HashMap<Long, UserGameContext> UserGameContexts;
    BotContext(){
        UserGameContexts = new HashMap<Long, UserGameContext>();
    }
}
