import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class TelegramInterface extends TelegramLongPollingBot {
    // Mappa per memorizzare i dati del quiz
    private final Map<String, List<String>> quizArticlesMap = new HashMap<>();
    private final Map<String, List<String>> quizLinksMap = new HashMap<>();
    private final Map<String, String> fakeTitleMap = new HashMap<>();

    @Override
    public String getBotUsername() {
        return "@FakeOrFact_bot"; // Nome del bot
    }

    @Override
    public String getBotToken() {
        return "7971290608:AAG1t9-Y7LsQdM2-Mnp79jYk5SanJVBckM4"; // Token fornito da BotFather
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String receivedText = update.getMessage().getText();

            if (receivedText.equals("/start")) {
                String welcomeMessage = """
                        Benvenuto nel Quiz delle Notizie! 📰✨
                        
                        Il gioco consiste nel seguente:
                        - Ti verranno presentate 4 notizie.
                        - Una di queste è FALSA! ❌
                        - Il tuo compito è indovinare quale sia la notizia fake. ✅
                        
                        Sei pronto? Iniziamo il quiz! 🕹️
                        """;
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText(welcomeMessage);

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

            if (receivedText.equals("/quiz")) {
                String question = "Quale di queste notizie è FALSA?";

                ArrayList<realArticle> articoliReal = Main.db.selectRealArticles();
                fakeArticle articoloFake = Main.db.selectFakeArticle();

                ArrayList<String> articoli = new ArrayList<>();
                ArrayList<String> links = new ArrayList<>();
                for (realArticle articolo : articoliReal) {
                    articoli.add(articolo.title);
                    links.add(articolo.link);
                }
                articoli.add(articoloFake.title);
                links.add(articoloFake.link);

                Collections.shuffle(articoli);

                StringBuilder quizMessage = new StringBuilder(question + "\n\n");
                List<InlineKeyboardButton> buttons = new ArrayList<>();

                for (int i = 0; i < articoli.size(); i++) {
                    quizMessage.append((i + 1)).append(". ").append(articoli.get(i)).append("\n");

                    InlineKeyboardButton button = new InlineKeyboardButton(String.valueOf(i + 1));
                    button.setCallbackData("Answer_" + i);
                    buttons.add(button);
                }

                InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                rowsInline.add(buttons);
                keyboardMarkup.setKeyboard(rowsInline);

                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText(quizMessage.toString());
                message.setReplyMarkup(keyboardMarkup);

                try {
                    execute(message);
                    // Salva i dati del quiz nella mappa
                    quizArticlesMap.put(chatId, articoli);
                    quizLinksMap.put(chatId, links);
                    fakeTitleMap.put(chatId, articoloFake.title);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }

        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

            List<String> articoli = quizArticlesMap.get(chatId);
            List<String> links = quizLinksMap.get(chatId);
            String fakeTitle = fakeTitleMap.get(chatId);

            if (articoli == null || links == null || fakeTitle == null) {
                return; // Nessun quiz associato a questa chat
            }

            int index = Integer.parseInt(callbackData.split("_")[1]);
            String selectedTitle = articoli.get(index);
            String link = links.get(index);

            String responseText;
            if (selectedTitle.equals(fakeTitle)) {
                responseText = "\u2728 Bravo! Hai scelto la notizia falsa correttamente!\n\nEcco il link che sfata l'articolo: " + link;
            } else {
                responseText = "\u274c Oh no! La notizia era vera.\n\nLink all'articolo: " + link;
            }

            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(chatId);
            editMessage.setMessageId(messageId);
            editMessage.setText(responseText);

            try {
                execute(editMessage);
                // Rimuovi i dati del quiz dalla mappa
                quizArticlesMap.remove(chatId);
                quizLinksMap.remove(chatId);
                fakeTitleMap.remove(chatId);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}