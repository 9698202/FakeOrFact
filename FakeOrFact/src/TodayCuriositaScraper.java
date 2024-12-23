import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TodayCuriositaScraper {

    String url = "https://www.today.it/tag/notizie-curiose/";
    String pag;
    ArrayList<realArticle> articles = new ArrayList<>();
    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> links = new ArrayList<>();

    public TodayCuriositaScraper() {

    }

    public ArrayList<realArticle> scrape() {
        for (int i = 1; i < 20; i++) {
            pag = "/pag/" + i + "/";
            try {
                // Effettua il parsing del documento HTML della pagina
                Document doc = Jsoup.connect(url + pag).get();

                // Seleziona gli elementi che rappresentano i titoli delle notizie
                Elements articleElements = doc.select("a.o-link-text");

                int c = 0;

                // Itera sugli elementi e aggiunge titoli e link alle rispettive liste
                for (Element element : articleElements) {
                    // Estrai il titolo
                    Element titleElement = element.selectFirst("h1.u-heading-04.u-mb-small.u-mt-none");
                    if (titleElement != null) {
                        titles.add(titleElement.text());
                    }

                    // Estrai il link
                    String href = element.attr("href");

                    /*i primi 2 link che non sono collegati agli articoli iniziano per user e tags quindi li escludo
                     poi essendo 25 articoli per pagina mi fermo a 27 perchè quelli dopo sono link di articoli secondari
                     */
                    if (!href.isEmpty() && !href.startsWith("/user") && !href.startsWith("/tags") && c < 27) {
                        links.add(href.startsWith("http") ? href : "https://www.today.it" + href);
                    }

                    c++;
                }

            } catch (IOException e) {
                System.out.println("Errore durante il collegamento al sito: " + e.getMessage());
            }
        }

        //creo i vari oggetti e li metto nell'Array così potro passarli uno alla volta al DataBase

        for (int i = 0; i < titles.size(); i++) {
            articles.add(new realArticle(titles.get(i), links.get(i)));
        }

        return articles;
    }
}
