package sources;


import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import coresources.EncodingDecoding;

public class HttpClientMain {

    static String contentType = "";
    static String url = "";
    static boolean loop = true;
    static Scanner sc = new Scanner(System.in);
    static String fileExtension = "";

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

            if (url.contains(".")) {

                String[] urlArray = url.split("\\.");
                int lastInArray = urlArray.length - 1;
                contentType = urlArray[lastInArray];
                fileExtension = contentType;

            }

            switch (contentType) {

                case "png":
                    contentType = "image/png";
                    break;
                case "jpg":
                    contentType = "image/jpg";
                    break;
                case "html":
                    contentType = "application/html";
                    break;
                case "pdf":
                    contentType = "application/pdf";
                    break;
                case "js":
                    contentType = "text/javascript";
                    break;
                case "css":
                    contentType = "text/css";
                    break;
                default:
                    contentType = "application/json";
                    break;
            }

            Map<String, String> bodyText = new HashMap<String, String>();

            String parameter = "";
            if (url.contains("&")) {
                parameter = url.split("&")[1];
            }
            if (parameter.startsWith("changename")) {
                String userNameInput = parameter.split("=")[1];
                bodyText.put("changename", userNameInput);
            }
            String urlType = "";
            switch (requestType) {
                case 1:
                    urlType = "GET";
                    GETAndHEADRequest(url, urlType);
                    break;
                case 2:
                    urlType = "HEAD";
                    GETAndHEADRequest(url, urlType);
                    break;
                case 3:
                    POSTRequest(bodyText);
                    break;
            }


        }
    }

    private static void GETAndHEADRequest(String url, String urlType) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest getRequest = HttpRequest.newBuilder()
                .GET()
                .header("accept", contentType)
                .uri(URI.create("http://localhost/" + url))
                .build();


        if (url.equals("storage")) {
            HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
            if (urlType.equals("HEAD")) {
                System.out.println(response.headers());
            } else {

                ObjectMapper mapper = new ObjectMapper();
                List<Person> posts = mapper.readValue(response.body(), new TypeReference<>() {
                });

                posts.forEach(System.out::println);
            }
        } else if (url.contains("?")) {
            HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
            if (urlType.equals("HEAD")) {
                System.out.println(response.headers());
            } else {

                ObjectMapper mapper = new ObjectMapper();
                Person post = mapper.readValue(response.body(), new TypeReference<>() {
                });

                System.out.println(post);
            }

        } else if (url.contains(".")) {
            HttpResponse<byte[]> response = client.send(getRequest, HttpResponse.BodyHandlers.ofByteArray());
            if (urlType.equals("HEAD")) {
                System.out.println(response.headers());
            } else {

                if (200 == response.statusCode()) {
                    byte[] bytes = response.body();

                    try (OutputStream out = new FileOutputStream(url)) {
                        out.write(bytes);
                    }
                }
            }
        }
    }

    public static CompletableFuture<Void> POSTRequest(Map<String, String> bodyText) throws IOException {



        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(bodyText);

        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create("http://localhost/" + url))
                .build();

        return HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::statusCode)
                .thenAccept(System.out::println);
    }


}



