package src.main.BusBot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import src.main.BusBot.Test.HttpRequest;
import src.main.BusBot.config.BotConfig;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    static final String HELP_TEXT = "Вы нажали на помощь, но ее нет.\n\n" +
                                    "Надеюсь она когда-нибудь добавится.";
    public TelegramBot (BotConfig config){
        this.config = config;
        List listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start","get a welcome message"));
        listOfCommands.add(new BotCommand("/mydata","get your data store"));
        listOfCommands.add(new BotCommand("/schedule","show рассписание"));
        listOfCommands.add(new BotCommand("/help","помощь"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        }
        catch (TelegramApiException e){
            //   log.error("Error setting bot's command list:" + e.getMessage() );
        }
    }
    @Override
    public String getBotToken() {
        return config.getToken();
    }
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText){
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/schedule":
                    showSchedule(chatId, update.getMessage().getChat().getUserName());
                    break;
                case "/mydata":
                    sendInlineKeyBoardMessage(chatId);
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default: sendMessage(chatId, "Sorry, command don't work");
            }
        }
        else if(update.hasCallbackQuery()){
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if(callbackData.equals("Минск")){
                String text = "Минск";
                EditMessageText message = new EditMessageText();
                message.setChatId(String.valueOf(chatId));
                message.setText(text);
                message.setMessageId((int)(messageId));
                try {
                    execute(message);
                }
                catch (TelegramApiException e){
               //     log.error("Error occurred: " + e.getMessage());
                }

            }else if(callbackData.equals("Осиповичи")){
                String text = "Осиповичи";
                EditMessageText message = new EditMessageText();
                message.setChatId(String.valueOf(chatId));
                message.setText(text);
                message.setMessageId((int)(messageId));
                try {
                    execute(message);
                }
                catch (TelegramApiException e){
              //      log.error("Error occurred: " + e.getMessage());
                }

            }

        }
    }

 private void startCommandReceived(long chatId, String name){

    String answer = "Hi, " + name + ", nice to meet you!";

   // log.info ("Replied to user " + name);
    sendMessage(chatId, answer);
     System.out.println("LOX");
 }

    private void sendMessage(long chatId, String TextToSend){

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(TextToSend);
        try {
            execute(message);
        }
        catch (TelegramApiException e){
    //    log.error("Error occurred: " + e.getMessage());
        }
    }
    private void showSchedule(long chatId, String TextToSend) {

        HttpRequest httpRequest = new HttpRequest();
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(TextToSend+", рассписание маршруток Минск --> Осиповичи на "+ httpRequest.getParam5());
        try {
            execute(message);
        }
        catch (TelegramApiException e){
       //     log.error("Error occurred: " + e.getMessage());
        }


        sendMessage(chatId, httpRequest.HttpRequest());
    }

    private void sendInlineKeyBoardMessage(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите город отправления");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Минск");
        inlineKeyboardButton1.setCallbackData("Минск");
        inlineKeyboardButton2.setText("Осиповичи");
        inlineKeyboardButton2.setCallbackData("Осиповичи");

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();

        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow1.add(inlineKeyboardButton2);


        rowList.add(keyboardButtonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);

        message.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(message);
        }
        catch (TelegramApiException e){
    //        log.error("Error occurred: " + e.getMessage());
        }


        //return new SendMessage().setChatId(String.valueOf(chatId)).setText("Пример").setReplyMarkup(inlineKeyboardMarkup);
    }
}
