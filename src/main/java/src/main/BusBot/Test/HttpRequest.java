package src.main.BusBot.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class HttpRequest {

    private String uri = "https://bilet.osipovichi-minsk.by/schedules";

        // The parameters to include in the request
      private   String param1 = "station_from_id=0";
    private  String param2 = "station_to_id=0";
    private  String param3 = "city_from_id=2";
    private   String param4 = "city_to_id=1";

    public String getParam5() {
        return param5;
    }

    private    String param5 = "date=19.03.2023";
    private   String param6 = "time=00:00";
    private    String param7 = "places=1";

    String race = "" ;
        // Build the query string
    public String HttpRequest(){
        String queryString = param1 + "&" +param2 + "&" +param3 + "&" +param4 + "&" +param5 + "&" +param6 + "&" +param7;

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