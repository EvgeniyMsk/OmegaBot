package ou.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ou.entities.Car;
import ou.entities.Person;
import ou.tasks.OmegaTask;

import java.util.*;

@Component
@PropertySource("classpath:/application.yml")
@Slf4j
public class OmegaBot extends TelegramLongPollingBot {
    @Value("${telegramBot.token}")
    private String telegramBotToken;

    @Value("${telegramBot.name}")
    private String telegramBotName;

    @Value("${filePath}")
    private String path;

    @Value("${allowedChatIds}")
    private String[] allowedChatIds;
    Map<Long, OmegaTask> taskMap = new HashMap<>();

    private final String REGEXP_CAR = "^[АВЕКМНОРСТУХавекмнорстух]\\d{3}(?<!000)[АВЕКМНОРСТУХавекмнорстух]{2}\\d{2,3}$";

    private final String REGEXP_PERSON = "^[А-ЯЁ][а-яё]*([-][А-ЯЁ][а-яё]*)?\\s[А-ЯЁ][а-яё]*\\s[А-ЯЁ][а-яё]*\\s\\d{2}(?<!00).\\d{2}(?<!00).\\d{4}(?<!0000)$";

    @Override
    public String getBotToken() {
        return telegramBotToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        checkAllowed(update);
    }

    private void sendSelector(Update update, String text) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();

        inlineKeyboardButton1.setText("\uD83D\uDC65 Человек");
        inlineKeyboardButton1.setCallbackData("\\person");
        inlineKeyboardButton2.setText("\uD83D\uDE99 Авто");
        inlineKeyboardButton2.setCallbackData("\\auto");

        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();

        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow1.add(inlineKeyboardButton2);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId().toString());
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendStatusChecker(Update update) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();

        inlineKeyboardButton1.setText("\uD83D\uDD0E Узнать статус");
        inlineKeyboardButton1.setCallbackData("\\status");

        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();

        keyboardButtonsRow1.add(inlineKeyboardButton1);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Для проверки статуса нажмите \"Обновить\"");
        sendMessage.setChatId(update.getMessage().getChatId().toString());
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendNotice(Update update, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId().toString());
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendCallbackNotice(Update update, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean containsId(Long id) {
        return taskMap.containsKey(id);
    }

    public void removeId(Long id) {
        this.taskMap.remove(id);
    }

    private void checkAllowed(Update update) {
        Set<Long> tmp = new HashSet<>();
        for (String s : allowedChatIds)
            tmp.add(Long.parseLong(s));
        if (update.getMessage() != null)
        {
            try {
                Long id = update.getMessage().getFrom().getId();
                SendMessage message = new SendMessage();
                message.setChatId(Long.toString(update.getMessage().getChatId()));
                if (!tmp.contains(id)) {
                    sendNotice(update, String.format("Вы не зарегистрированы.:(\nВаш ID: %d. Для регистрации свяжитесь с администратором: ", update.getMessage().getFrom().getId()));
                    log.info("Левый запрос у бота");
                } else {
                    handleMessage(update);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        }
    }

    private void handleCallbackQuery(Update update) {
        Long id = update.getCallbackQuery().getFrom().getId();
        if (containsId(id))
        {
            sendCallbackNotice(update, String.format("Ваш запрос %s в работе! Значение счетчика:%d",
                    taskMap.get(id).getTaskId(),
                    taskMap.get(id).getCounter()));
        } else {
            String callbackData = update.getCallbackQuery().getData();
            switch (callbackData) {
                case "\\person": sendCallbackNotice(update, "Введите ФИО и дату рождения персоны\n" +
                        "Например:\nИванов Иван Иванович 12.12.2012"); break;
                case "\\auto": sendCallbackNotice(update, "Введите ГРЗ авто\n" +
                        "Например:\nТ123АМ777"); break;
                default:
                    System.out.println("Halt");
            }
        }
    }

    private void handleMessage(Update update) {
        Long id = update.getMessage().getFrom().getId();
        if (containsId(id))
        {
            sendNotice(update, String.format("Задание %s в работе! Значение счетчика:%d",
                    taskMap.get(id).getTaskId(),
                    taskMap.get(id).getCounter()));
        }
        else
        {
            sendSelector(update, "Выберите интересующую информацию");
            String taskId = UUID.randomUUID() + "_" + id + "[" + update.getMessage().getFrom().getFirstName() + "]";
            String text = update.getMessage().getText();
            if (text.matches(REGEXP_CAR))
            {
                sendNotice(update, "Запрос поставлен в очередь. Тип запроса: Авто");
                sendStatusChecker(update);
                taskMap.put(id, new OmegaTask(this, taskId, update, new Car(text)));
                Thread thread = new Thread(taskMap.get(id));
                thread.start();
            }
            else if (text.matches(REGEXP_PERSON))
            {
                sendNotice(update, "Запрос поставлен в очередь. Тип запроса: Персона");
                sendStatusChecker(update);
                taskMap.put(id, new OmegaTask(this, taskId, update, new Person(text)));
                Thread thread = new Thread(taskMap.get(id));
                thread.start();
            } else
                sendNotice(update, "Неверный формат запроса.");
        }
    }

    @Override
    public String getBotUsername() {
        return telegramBotName;
    }

    public String getPath() {
        return path;
    }
}
