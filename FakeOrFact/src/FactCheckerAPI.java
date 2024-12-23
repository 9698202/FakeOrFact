import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class FactCheckerAPI {
    public static final String URL = "https://factchecktools.googleapis.com/v1alpha1/claims:search?languageCode=it";
    public static final String publisherSite = "bufale.net";
    //chiave necessaria ad utilizzare l'API di google (creata da Google Cloud Console)
    private static final String key = "AIzaSyBd6zPnuXvw7Rp2cikM7kbQPTD4BcJ3lUc";
    public static final Gson gson = new Gson();
    public static final HttpClient client = HttpClient.newHttpClient();

    public static ArrayList<fakeArticle> getArticles() {
        //filtro per notizie di bufale.net così da essere sicuro si trattino di fake news
        String articlesURL = URL + "&reviewPublisherSiteFilter=" + publisherSite + "&key=" + key;
        ArrayList<fakeArticle> articles = new ArrayList<>();
        String nextPageToken = null;
        String nextPageURL;

        for (int j = 0; j < 20; j++) {
            //alla fine del json della richiesta API c'è un token per poter andare alla pagina successiva se inserito come parametro
            if(nextPageToken != null) {
                 nextPageURL = articlesURL + "&pageToken=" + nextPageToken;
            }else{
                nextPageURL = articlesURL;
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(nextPageURL))
                    .build();

            HttpResponse response = null;

            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JsonObject jsonArticles = gson.fromJson(response.body().toString(), JsonObject.class);
                JsonArray claimsArray = jsonArticles.get("claims").getAsJsonArray();
                JsonObject claim;

                //di ogni articolo estraggo titolo, link e argomentazione della fake news
                for (int i = 0; i < claimsArray.size(); i++) {
                    claim = claimsArray.get(i).getAsJsonObject();
                    String title = claim.get("text").getAsString();
                    JsonObject claimReview = claim.get("claimReview").getAsJsonArray().get(0).getAsJsonObject();
                    String link = claimReview.get("url").getAsString();
                    String textRating = claimReview.get("textualRating").getAsString();
                    articles.add(new fakeArticle(title, link, textRating));
                }

                nextPageToken = jsonArticles.get("nextPageToken").getAsString();

            } catch (Exception e) {
                System.out.println("Errore nella richiesta API degli articoli: " + e);
                e.printStackTrace();
                return null;
            }
        }

        return articles;
    }
}