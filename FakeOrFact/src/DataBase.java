import java.sql.*;
import java.util.ArrayList;

public class DataBase {
    public Connection con;

    public DataBase(int port, String username, String password, String database){
        String percorso = "jdbc:mysql://localhost:" + port + "/" + database;
        try{
            con = DriverManager.getConnection(percorso, username, password);
        }catch(SQLException e){
            System.out.println("Errore nella connessione al database: " + e);
        }
    }

    public String SelectAll(String tabella){
        StringBuilder risultato = new StringBuilder();
        String query = "SELECT * FROM " +  tabella;

        try(Statement statement = con.createStatement(); ResultSet rs = statement.executeQuery(query))
        {
            con.isValid(20);
            while(rs.next()){
                for (int i=1; i<=rs.getMetaData().getColumnCount(); i++){
                    risultato.append(rs.getString(i) + "\n");
                }
                risultato.append("\n----------------\n");
            }
        }catch(SQLException e){
            System.out.println("Errore nella selezioni dei dati del database: " + e);
        }

        return risultato.toString();
    }

    public void insertRealArticle(realArticle article){
        if(!isTitleInDatabase(article.title, "realnews")){
            String query = "INSERT INTO realnews" + "(title, link) VALUES (?,?)";

            try{
                PreparedStatement statement = con.prepareStatement(query);

                if (article.title.length() >= 255) {
                    article.title = article.title.substring(0, 251) + "..."; // Tronca la stringa se troppo lunga
                }
                statement.setString(1, article.title);

                statement.setString(2, article.link);

                statement.executeUpdate();

            }catch(SQLException e){
                System.out.println("Errore nell'inserimento dell'articolo: " + e);
            }
        }
    }

    public void insertFakeArticle(fakeArticle article){
        if(!isTitleInDatabase(article.title, "fakenews")){
            String query = "INSERT INTO fakenews" + "(title, link, rating) VALUES (?,?,?)";

            try{
                PreparedStatement statement = con.prepareStatement(query);

                if (article.title.length() >= 255) {
                    article.title = article.title.substring(0, 251) + "..."; // Tronca la stringa se troppo lunga
                }
                statement.setString(1, article.title);

                statement.setString(2, article.link);

                if (article.textRating.length() >= 255) {
                    article.textRating = article.textRating.substring(0, 251) + "..."; // Tronca la stringa se troppo lunga
                }

                statement.setString(3, article.textRating);

                statement.executeUpdate();

            }catch(SQLException e){
                System.out.println("Errore nell'inserimento dell' articolo: " + e);
            }
        }
    }

    public ArrayList<realArticle> selectRealArticles() {
        ArrayList<realArticle> risultati = new ArrayList<realArticle>();
        String query = "SELECT * FROM realnews ORDER BY RAND() LIMIT 3"; // Ordina casualmente e prendi 3 righe

        try (Statement statement = con.createStatement(); ResultSet rs = statement.executeQuery(query)) {
            con.isValid(20);
            while (rs.next()) {
                risultati.add(new realArticle(rs.getString(2), rs.getString(3)));
            }
        } catch (SQLException e) {
            System.out.println("Errore nella selezione delle notizie vere: " + e);
        }

        return risultati;
    }

    public fakeArticle selectFakeArticle() {
        ArrayList<fakeArticle> risultati = new ArrayList<fakeArticle>();
        String query = "SELECT * FROM fakenews ORDER BY RAND() LIMIT 1"; // Ordina casualmente e prendi 3 righe

        try (Statement statement = con.createStatement(); ResultSet rs = statement.executeQuery(query)) {
            con.isValid(20);
            while (rs.next()) {
                return new fakeArticle(rs.getString(2), rs.getString(3), rs.getString(4));
            }
        } catch (SQLException e) {
            System.out.println("Errore nella selezione delle notizie false: " + e);
        }

        return null;
    }


    private boolean isTitleInDatabase(String articleTitle, String table) {
        String query = "SELECT COUNT(*) FROM " + table + " WHERE title = ?";

            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setString(1, articleTitle);
                try( ResultSet rs = stmt.executeQuery()){
                    if (rs.next()) {
                        return rs.getInt(1) > 0; // Ritorna true se il conteggio è maggiore di 0
                    }
                }
            }catch (SQLException e) {
                System.out.println("Errore nella query di controllo: " + e);
            }
        return false;
    }
}