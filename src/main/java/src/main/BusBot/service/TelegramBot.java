package src.main.BusBot.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import src.main.BusBot.Test.responseFromServer;
import src.main.BusBot.config.BotConfig;
import src.main.BusBot.entity.City;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    private final CityModeService cityModeService = CityModeService.getInstance();



    static final String HELP_TEXT = "Вы нажали на помощь, но ее нет.\n\n" +
                                    "Надеюсь она когда-нибудь добавится.";
    public TelegramBot (BotConfig config){
        this.config = config;
        List listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start","get a welcome message"));
        listOfCommands.add(new BotCommand("/set_route","get your data store"));
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
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        } else if (update.hasMessage()) {
            handleMessage(update.getMessage());
        }

    }

    @SneakyThrows
    private void handleMessage(Message message) {
        // handle command
        if (message.hasText() && message.hasEntities()) {
            Optional<MessageEntity> commandEntity =
                    message.getEntities().stream().filter(e -> "bot_command".equals(e.getType())).findFirst();
            if (commandEntity.isPresent()) {
                String command =
                        message
                                .getText()
                                .substring(commandEntity.get().getOffset(), commandEntity.get().getLength());
                switch (command) {
                    case "/set_route":
                        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
                        City cityFrom = cityModeService.getCityFrom(message.getChatId());
                        City cityTo = cityModeService.getCityTo(message.getChatId());
                        for (City city : City.values()) {
                            buttons.add(
                                    Arrays.asList(
                                            InlineKeyboardButton.builder()
                                                    .text(getCityButton(cityFrom, city))
                                                    .callbackData("FROM:" + city)
                                                    .build(),
                                            InlineKeyboardButton.builder()
                                                    .text(getCityButton(cityTo, city))
                                                    .callbackData("TO:" + city)
                                                    .build()));
                        }
                        execute(
                                SendMessage.builder()
                                        .text("Выберите город отправления и город прибытия")
                                        .chatId(message.getChatId().toString())
                                        .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                                        .build());
                        return;
                }
            }
        }
        if (message.hasText()) {
            String messageText = message.getText();
         //   String value = parseString(messageText);
            City cityFrom = cityModeService.getCityFrom(message.getChatId());
            City cityTo = cityModeService.getCityTo(message.getChatId());
     //       double ratio = currencyConversionService.getConversionRatio(originalCurrency, targetCurrency);

            if (!messageText.isEmpty()) {
                if(cityFrom == cityTo || (cityFrom.getId() + cityTo.getId() == 5)){
                    execute(
                            SendMessage.builder()
                                    .chatId(message.getChatId().toString())
                                    .text("Направления "+ cityFrom +" ➡️ " + cityTo + " не существует, попробуйте еще раз /set_route")
                                    .build());
                }else {
                    execute(
                            SendMessage.builder()
                                    .chatId(message.getChatId().toString())
                                    .text(getInfo(cityFrom, cityTo))
                                    .build());
                }
                }
        }
    }

    @SneakyThrows
    private void handleCallback(CallbackQuery callbackQuery) {
            Message message = callbackQuery.getMessage();
            String[] param = callbackQuery.getData().split(":");
            String action = param[0];
            City newCity = City.valueOf(param[1]);
            switch (action) {
                case "FROM":
                    cityModeService.setCityFrom(message.getChatId(), newCity);
                    break;
                case "TO":
                    cityModeService.setCityTo(message.getChatId(), newCity);
                    break;
            }
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            City cityFrom = cityModeService.getCityFrom(message.getChatId());
            City cityTo = cityModeService.getCityTo(message.getChatId());
            for (City city : City.values()) {
                buttons.add(
                        Arrays.asList(
                                InlineKeyboardButton.builder()
                                        .text(getCityButton(cityFrom, city))
                                        .callbackData("FROM:" + city)
                                        .build(),
                                InlineKeyboardButton.builder()
                                        .text(getCityButton(cityTo, city))
                                        .callbackData("TO:" + city)
                                        .build()));
            }
            execute(
                    EditMessageReplyMarkup.builder()
                            .chatId(message.getChatId().toString())
                            .messageId(message.getMessageId())
                            .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                            .build());
        }
    private String getCityButton(City saved, City current) {
        return saved == current ? current + " ✅" : current.name();
    }

    private String getInfo(City from, City to) {
        String uri = "https://bilet.osipovichi-minsk.by/schedules";
          // The parameters to include in the request
           String param1 = "station_from_id=0";
          String param2 = "station_to_id=0";
          String param3 = String.valueOf(from.getId());
      //  System.out.println(param3);
           String param4 = String.valueOf(to.getId());
      //  System.out.println(param4);
            String param5 = "19.03";
           String param6 = "time=00:00";
            String param7 = "places=1";

        String race = "Расписание на "+ param5 + ".2023\nНаправление: "+ from +" ➡️ " +to +"\n";

            String queryString = param1 + "&" +param2 + "&city_from_id=" +param3 + "&city_to_id=" +param4 + "&date=" +param5 + ".2023&" +param6 + "&" +param7;

            // Append the query string to the URL
            uri += "?" + queryString;

            URL url = null;
            try {
                url = new URL(uri);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                con.setRequestMethod("GET");
            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            }

            try {
                int status = con.getResponseCode(); // совершаем запрос
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            BufferedReader in = null; // читаем ответ
            try {
                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String inputLine;
            StringBuffer content = new StringBuffer();
            while (true) {
                try {
                    if (!((inputLine = in.readLine()) != null)) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                content.append(inputLine);
            }
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            responseFromServer response = gson.fromJson(String.valueOf(content), responseFromServer.class);


            Document doc = Jsoup.parse(response.html);
            Elements div = doc.getElementsByClass("nf-route__time");
            String divText = div.text();
            //    System.out.println(div.get(1).text());

            int i=0;

            for (Element element : div) {
                for (Node child : element.childNodes()) {

                    if (child instanceof TextNode textNode) {
                        if (i == 0) {
                            race += textNode.getWholeText() + " - ";
                        }
                        if (i == 1) {
                            race += textNode.getWholeText();
                        }
                        if (i == 2) {
                            i = -1;
                            race += "\n";
                            textNode.getWholeText();

                        }
                        i++;
                    }
                }

            }
            con.disconnect();
            return race;
        }

    }


