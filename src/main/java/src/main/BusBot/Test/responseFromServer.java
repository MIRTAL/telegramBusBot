package src.main.BusBot.Test;

public class responseFromServer {

    public String result;
    public String html;
    public String return_html;

    // Конструктор
    public responseFromServer(String result, String html, String return_html){
        this.result = result;
        this.html = html;
        this.return_html= return_html;

    }
}
