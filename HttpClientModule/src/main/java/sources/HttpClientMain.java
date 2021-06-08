package sources;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpClientMain {

    static String type = "";
    static String url = "";
    static boolean loop = true;
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {

        while (loop) {

            System.out.println("Select type of request:");
            System.out.println("1. GET");
            System.out.println("2. HEAD");
            System.out.println("3. POST");
            int requestType = sc.nextInt();
            sc.nextLine();

            System.out.println("Enter your url (leave empty for database pull):");
            url = sc.nextLine();

            switch (requestType) {
                case 1:
                    GETRequest(url);
                case 2:
                    // HEADRequest(url);
                case 3:
                    // POSTRequest(url);
            }

            if (url.contains(".")) {                                                                                    // stöd för filer, t.ex. dog.jpg

                String[] urlArray = url.split("\\.");
                int lastInArray = urlArray.length - 1;
                type = urlArray[lastInArray];                                                                           // sista strängen i arrayen separerad med ''.'', t.ex. jpg

            }

            switch (type) {                                                                                             // sätter type till rätt HTTP Content Type

                case "png":
                    type = "image/png";
                    break;
                case "img":
                    type = "image/img";
                    break;
                case "html":
                    type = "application/html";
                    break;
                case "pdf":
                    type = "application/pdf";
                    break;
                case "js":
                    type = "text/javascript";
                    break;
                case "css":
                    type = "text/css";
                    break;
                default:
                    type = "application/json";
                    break;
            }

        }
    }

    private static void GETRequest(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest getRequest = HttpRequest.newBuilder()
                .GET()
                .header("accept", type)
                .uri(URI.create("http://localhost/" + url))
                .build();

        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        // Allt nedan rör GSON till JSON-konvertering.

        if (url.equals("/storage")) {

            ObjectMapper mapper = new ObjectMapper();
            List<Person> posts = mapper.readValue(response.body(), new TypeReference<>() {
            });

            posts.forEach(System.out::println);
        } else if (url.contains("?")) {
            ObjectMapper mapper = new ObjectMapper();
            Person post = mapper.readValue(response.body(), new TypeReference<>() {
            });

            System.out.println(post);
        }

    }
}

