package sources;


import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;

public class HttpClientMain {

    static String type = "";
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
                // sparar type så att vi kan använda den för att hämta filer i metoder
                type = urlArray[lastInArray];
                fileExtension = type;

            }

            switch (type) {

                case "png":
                    type = "image/png";
                    break;
                case "jpg":
                    type = "image/jpg";
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


            switch (requestType) {
                case 1:
                    GETRequest(url);
                case 2:
                    // HEADRequest(url);
                case 3:
                    // POSTRequest(url);
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


        if (url.equals("/storage")) {
            HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            List<Person> posts = mapper.readValue(response.body(), new TypeReference<>() {
            });

            posts.forEach(System.out::println);
        } else if (url.contains("?")) {
            HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            Person post = mapper.readValue(response.body(), new TypeReference<>() {
            });

            System.out.println(post);

        } else if (url.contains(".")) {
            HttpResponse<byte[]> response = client.send(getRequest, HttpResponse.BodyHandlers.ofByteArray());

            if (200 == response.statusCode()) {
                byte[] bytes = response.body();

                try (OutputStream out = new FileOutputStream(url)) {
                    out.write(bytes);
                }

            }


        }


    }


}



