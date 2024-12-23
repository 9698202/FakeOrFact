import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    static DataBase db = new DataBase(3306, "root", "", "fakeorfact");

    public static void main(String[] args) {
        TodayCuriositaScraper todayScraper = new TodayCuriositaScraper();
        ArrayList<realArticle> realArticles = todayScraper.scrape();
        ArrayList<fakeArticle> fakeArticles = FactCheckerAPI.getArticles();



        for(int i = 0; i < realArticles.size(); i++){
            db.insertRealArticle(realArticles.get(i));
        }

        for(int i = 0; i < fakeArticles.size(); i++){
            db.insertFakeArticle(fakeArticles.get(i));
        }

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramInterface()); // Registra il bot
            System.out.println("Bot avviato con successo");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
