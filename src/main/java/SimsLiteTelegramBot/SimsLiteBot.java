package SimsLiteTelegramBot;

import SimsLiteTelegramBot.Types.GenderType;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimsLiteBot extends TelegramLongPollingBot {
    private UserGameContext _currentUserGameContext;

    @Override
    public String getBotUsername() {
        return "";
    }

    @Override
    public String getBotToken() {
        return "";
    }

    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            UserGameContext userGameContext = Main.BotContext.UserGameContexts.get(chatId);
            if (userGameContext != null && !messageText.equals("Начать новую игру")) {
                _currentUserGameContext = userGameContext;
                Main.BotContext.UserGameContexts.put(chatId, userGameContext);
                messageSender(chatId, messageText);
            } else {
                userGameContext = new UserGameContext();
                _currentUserGameContext = userGameContext;
                Main.BotContext.UserGameContexts.put(chatId, userGameContext);
                messageSender(chatId, messageText);
            }
        }
    }

    private void messageSender(long chatId, String botMessageText) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        var result = setRegularMessage(message, botMessageText);
        if (result.isEmpty()) {
            return;
        }
        message.setText(result);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String setRegularMessage(SendMessage sendMessage, String userMessageText) {
        switch (userMessageText) {
            case ("/start"):
                return startGame(sendMessage);
            case ("Начать новую игру"), ("Нет, выбрать снова"):
                return setGender(sendMessage);
            case ("Продолжить игру"), ("Вернуться на экран выбора действий"):
                return setWhatDoing(sendMessage);
            case (GenderType.Female):
                _currentUserGameContext.Gender = GenderType.Female;
                return setIsYouSureAppearance(sendMessage);
            case (GenderType.Male):
                _currentUserGameContext.Gender = GenderType.Male;
                return setIsYouSureAppearance(sendMessage);
            case ("Да"):
                _currentUserGameContext.IsWaitingName = true;
                return "Как назовешь своего персонажа";
            case ("Первый дом"):
                _currentUserGameContext.Home = "Home_1";
                return setWhatDoing(sendMessage);
            case ("Второй дом"):
                _currentUserGameContext.Home = "Home_2";
                return setWhatDoing(sendMessage);
            case ("Режим строительства"):
                return setDoBuildHome(sendMessage);
            case ("Действия персонажа"):
                return setSimAction(sendMessage);
            case ("Пойти купаться в бассейн"):
                return setEnjoyInPool(sendMessage);
            case ("Выбраться из бассейна и вернуться на экран выбора действий"):
                if (_currentUserGameContext.IsPoolHaveStair) {
                    return setWhatDoing(sendMessage);
                } else {
                    return setDeathPoolEvent(sendMessage);
                }
            case ("Пойти погулять в парк"):
                return setWalkingEvent(sendMessage);
            case ("Пойти спать"):
                return setSleepSim(sendMessage);
            case ("Купить кровать"):
                _currentUserGameContext.HomeItems.add("Кровать");
                return setBuyBed(sendMessage);
            case ("Купить ноутбук"):
                _currentUserGameContext.HomeItems.add("Ноутбук");
                return setBuyNotebook(sendMessage);
            case ("Построить бассейн"):
                _currentUserGameContext.HomeItems.add("Бассейн");
                return setBuildStairPool(sendMessage);
            case ("Построить без лестницы"):
                _currentUserGameContext.IsPoolHaveStair = false;
                return setHappyBuildNewPool(sendMessage);
            case ("Построить с лестницей"):
                _currentUserGameContext.IsPoolHaveStair = true;
                return setHappyBuildNewPool(sendMessage);

            default:
                if (_currentUserGameContext.IsWaitingName) {
                    _currentUserGameContext.IsWaitingName = false;
                    _currentUserGameContext.Name = userMessageText;
                    return setSimHome(sendMessage);
                }
                return "Нет доступной команды";
        }
    }

    private String startGame(SendMessage sendMessage) {
        // Создаем клавиатуру
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        if (_currentUserGameContext.Name != null && _currentUserGameContext.Gender != null) {
            row.add(new KeyboardButton("Продолжить игру"));
        }

        // Добавляем кнопки в первый ряд
        row.add(new KeyboardButton("Начать новую игру"));
        keyboard.add(row);

        // Устанавливаем клавиатуру в сообщение
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        return "Добро пожаловать в SimsLiteBot! " +
                "\n Это совсем небольшой игровой телеграм бот по мотивам игры The Sims." +
                "Для начала игры нажмите на кнопочку снизу:";
    }

    private String setGender(SendMessage sendMessage) {
        var botMessage = "Пора создать твоего персонажа. Выбери ему внешность: ";

        var sendPhoto = new Utils().GetSendPhotoMessage(botMessage,
                sendMessage.getChatId(),
                "Genders.png");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton(GenderType.Female));
        row.add(new KeyboardButton(GenderType.Male));
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        sendPhoto.setReplyMarkup(keyboardMarkup);

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String setIsYouSureAppearance(SendMessage sendMessage) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        row.add(new KeyboardButton("Да"));
        row.add(new KeyboardButton("Нет, выбрать снова"));
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        return "Уверен в своем выборе?";
    }

    private String setSimHome(SendMessage sendMessage) {
        var botMessage = "Теперь выбери дом, в котором будет жить " + _currentUserGameContext.Name + ":";

        var sendPhoto = new Utils().GetSendPhotoMessage(botMessage,
                sendMessage.getChatId(),
                "Homes.png");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("Первый дом"));
        row.add(new KeyboardButton("Второй дом"));
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        sendPhoto.setReplyMarkup(keyboardMarkup);

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String setWhatDoing(SendMessage sendMessage) {
        var botMessage = "А вот и твой дом и " + _currentUserGameContext.Name + " ! Выбери чем хочешь заняться: ";

        var gender = _currentUserGameContext.Gender.equals(GenderType.Male) ? "Male" : "Female";
        var fileName = gender + _currentUserGameContext.Home + ".png";
        var sendPhoto = new Utils().GetSendPhotoMessage(botMessage,
                sendMessage.getChatId(),
                fileName);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        row.add(new KeyboardButton("Режим строительства"));
        row.add(new KeyboardButton("Действия персонажа"));
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        sendPhoto.setReplyMarkup(keyboardMarkup);
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String setDoBuildHome(SendMessage sendMessage) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        var botMessage = "Ты в режиме строительства. Что бы хотел купить на свой участок?";

        if (!_currentUserGameContext.HomeItems.contains("Бассейн")) {
            row.add(new KeyboardButton("Построить бассейн"));
        }

        if (!_currentUserGameContext.HomeItems.contains("Кровать")) {
            row.add(new KeyboardButton("Купить кровать"));
        }

        if (!_currentUserGameContext.HomeItems.contains("Ноутбук")) {
            row.add(new KeyboardButton("Купить ноутбук"));
        }

        if (row.isEmpty()) {
            botMessage = "Всё что нужно уже есть на участке! Ждите обновления.";
            row.add(new KeyboardButton("Вернуться на экран выбора действий"));
        }

        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        return botMessage;
    }

    private String setSimAction(SendMessage sendMessage) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        if (_currentUserGameContext.HomeItems.contains("Кровать")) {
            row.add(new KeyboardButton("Пойти спать"));
        }

        if (_currentUserGameContext.HomeItems.contains("Бассейн")) {
            row.add(new KeyboardButton("Пойти купаться в бассейн"));
        }

        if (_currentUserGameContext.HomeItems.contains("Ноутбук")) {
            row.add(new KeyboardButton("Сесть за ноутбук искать работу"));
        }

        row.add(new KeyboardButton("Пойти погулять в парк")); // TODO добавить рандомные истории
        row.add(new KeyboardButton("Вернуться на экран выбора действий"));
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        return "Выбери чем " + _currentUserGameContext.Name + " хочет сейчас заняться: ";
    }

    private String setBuildStairPool(SendMessage sendMessage) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        row.add(new KeyboardButton("Построить без лестницы"));
        row.add(new KeyboardButton("Построить с лестницей"));
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        return "Хотел бы ты добавить лестницу в свой бассейн?";
    }

    private String setHappyBuildNewPool(SendMessage sendMessage) {
        var botMessage = "Ура! Теперь " + _currentUserGameContext.Name + " может отправиться плавать в бассейне.";

        var fileName = _currentUserGameContext.IsPoolHaveStair ? "Buy_Pool_Stair.png" : "Buy_Pool.png";
        var sendPhoto = new Utils().GetSendPhotoMessage(botMessage,
                sendMessage.getChatId(),
                fileName);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("Вернуться на экран выбора действий"));
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        sendPhoto.setReplyMarkup(keyboardMarkup);

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String setSleepSim(SendMessage sendMessage) {
        var botMessage = _currentUserGameContext.Name + " спит! Баю-бай!";

        var fileName = _currentUserGameContext.Gender.equals(GenderType.Male) ? "Sleep_male.png" : "Sleep_female.png";
        var sendPhoto = new Utils().GetSendPhotoMessage(botMessage,
                sendMessage.getChatId(),
                fileName);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        row.add(new KeyboardButton("Вернуться на экран выбора действий"));
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        sendPhoto.setReplyMarkup(keyboardMarkup);

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String setBuyBed(SendMessage sendMessage) {
        var botMessage = "Теперь у " + _currentUserGameContext.Name + " есть кровать!";

        var sendPhoto = new Utils().GetSendPhotoMessage(botMessage,
                sendMessage.getChatId(),
                "Buy_Bed.png");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        row.add(new KeyboardButton("Вернуться на экран выбора действий"));
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        sendPhoto.setReplyMarkup(keyboardMarkup);

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String setBuyNotebook(SendMessage sendMessage) {

        var botMessage = "Теперь у " + _currentUserGameContext.Name
                + " есть ноутбук! \nС помощью него "
                + Utils.GetPronounceByGender(_currentUserGameContext.Gender) + " теперь может найти работу.";

        var sendPhoto = new Utils().GetSendPhotoMessage(botMessage,
                sendMessage.getChatId(),
                "Buy_Notebook.png");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        row.add(new KeyboardButton("Вернуться на экран выбора действий"));
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        sendPhoto.setReplyMarkup(keyboardMarkup);

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String setEnjoyInPool(SendMessage sendMessage) {
        var botMessage = _currentUserGameContext.Name + " отлично проводит время в бассейне!";

        var fileName = _currentUserGameContext.Gender.equals(GenderType.Male) ? "Pool_male.png" : "Pool_female.png";
        var sendPhoto = new Utils().GetSendPhotoMessage(botMessage,
                sendMessage.getChatId(),
                fileName);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        row.add(new KeyboardButton("Выбраться из бассейна и вернуться на экран выбора действий"));
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        sendPhoto.setReplyMarkup(keyboardMarkup);

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String setDeathPoolEvent(SendMessage sendMessage) {
        var botMessage = "О нет! " + _currentUserGameContext.Name +
                " не может выбраться из бассейна, так как вы не добавили туда лестницу!" +
                " Кажется теперь " + Utils.GetPronounceByGender(_currentUserGameContext.Gender) +
                " останется до конца своей жизни плавать в своём бассейне. \nНачните новую игру...";

        var sendPhoto = new Utils().GetSendPhotoMessage(botMessage,
                sendMessage.getChatId(),
                "Death.jpg");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        row.add(new KeyboardButton("Начать новую игру"));
        row.add(new KeyboardButton("Купить вторую жизнь ($$$)"));
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        sendPhoto.setReplyMarkup(keyboardMarkup);

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String setWalkingEvent(SendMessage sendMessage) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        Random random = new Random();
        int randomNumber = random.nextInt(100); // Генерируем число от 0 до 99

        String resultMessage;
        if (randomNumber < 22) {
            resultMessage =
                    "Во время прогулки повстречался сосед " +
                            "и рассказал страшную историю, как на прошлой недели утонул человек в бассейне в нашем районе. " +
                            "Сосед посоветовал всегда при покупке бассейна не забывать про лестницу."; // 22% шанс
        } else if (randomNumber < 47) {
            resultMessage = "На улице пошел дождь и пришлось пойти домой. Прогулка не удалась."; // 25% шанс
        } else {
            resultMessage =
                    "На улице прекрасная погода. " + _currentUserGameContext.Name +
                            " отлично проводит время на свежем воздухе. И " +
                            Utils.GetPronounceByGender(_currentUserGameContext.Gender) +
                            " возвращается обратно домой."; // 53% шанс (оставшиеся 100 - 22 - 25 = 53%)
        }

        row.add(new KeyboardButton("Вернуться на экран выбора действий"));
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);

        return resultMessage;
    }
}