package src.main.BusBot.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

class Main
{
    public static void main(String[] args) throws Exception {
        String uri = "https://bilet.osipovichi-minsk.by/schedules";

        // The parameters to include in the request
        String param1 = "0"; // station_from_id
        String param2 = "0"; // station_to_id=
        String param3 = "2"; // city_from_id= 1- OSP 2- Minsk 3 - BOBR 4 - M5
        String param4 = "1"; // city_to_id=
        String param5 = "18.03.2023"; // date=
        String param6 = "00:00"; // time=
        String param7 = "1"; // places=

        // Build the query string
        String queryString = "station_from_id=" + param1 + "&station_to_id=" +param2 + "&city_from_id=" +param3 + "&city_to_id=" +param4 + "&date=" +param5 + "&time=" +param6 + "&places=" +param7;

        // Append the query string to the URL
        uri += "?" + queryString;

        URL url = new URL(uri);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        int status = con.getResponseCode(); // совершаем запрос
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())); // читаем ответ
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
  //      System.out.println(content);

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            responseFromServer response = gson.fromJson(String.valueOf(content), responseFromServer.class);
    //        System.out.println(response.html);

            Document doc = Jsoup.parse(response.html);
            Elements div = doc.getElementsByClass("nf-route__time");
            String divText = div.text();
     //       System.out.println(div.get(1).text());
        int i=0;
        for (Element element : div) {
                   for (Node child : element.childNodes()) {

                    if (child instanceof TextNode textNode){

                        System.out.print(textNode.getWholeText() + " ");
                        i++;
                        }
                    if(i == 3){
                        i = 0;
                        System.out.println();
                }

            }
        }
        //   System.out.println(div);
     //       System.out.println(divText);

        con.disconnect();
    }
}



