import com.google.gson.Gson;
import domain.WebserverDbEntity;
import sourcepackage.DBConnection;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {


        ExecutorService executorService = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(80)) {

            while (true) {
                Socket client = serverSocket.accept();
                executorService.submit(() -> handleConnection(client));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleConnection(Socket client) {

        try {

            BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String inputHeader = readRequest(inputFromClient);
            String type = inputHeader.split(" ")[0];
            String url = inputHeader.split(" ")[1];

            OutputStream outputToClient = client.getOutputStream();


            if (type.equals("GET") || type.equals("HEAD")) {
                switch (url) {
                    case "/cat.png":
                        sendImageResponse(outputToClient, type);
                        break;
                    case "/dog.jpg":
                        sendDogImageResponse(outputToClient, type);
                        break;
                    case "/index.html":
                        sendHtmlResponse(outputToClient, type);
                        break;
                    case "/Laboration1.pdf":
                        sendPdfResponse(outputToClient, type);
                        break;
                    default:
                        send404Response(outputToClient);
                        break;
                }
            } else if (type.equals("POST")) {
                if (url.equals("/storage")) {
                    sendJsonResponse(outputToClient, type);
                } else if (url.startsWith("/storage?id=")) {


                    String id = url.replaceAll("[^0-9.]", "");
                    int dbid = Integer.parseInt(id);

                    WebserverDbEntity person = DBConnection.sendIdResponse(dbid);

                    String parameter = url.split("&")[1];

                    if (parameter.startsWith("changename")) {
                        String name = parameter.split("=")[1];

                        person = DBConnection.sendNameUpdate(dbid, name);

                    }

                    Gson gson = new Gson();
                    String json = gson.toJson(person);
                    System.out.println(json);

                    byte[] data = json.getBytes(StandardCharsets.UTF_8);
                    String header = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-length: " + data.length + "\r\n\r\n";

                    outputToClient.write(header.getBytes());
                    outputToClient.write(data);
                    outputToClient.flush();

                } else {
                    send404Response(outputToClient);
                }
            } else {
                send404Response(outputToClient);
            }

            inputFromClient.close();
            outputToClient.close();
            client.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void send404Response(OutputStream outputToClient) throws IOException {
        String header = "HTTP/1.1 404 Not Found\r\nContent-length: 0\r\n\r\n";
        outputToClient.write(header.getBytes());
        outputToClient.flush();
    }

    private static void sendHtmlResponse(OutputStream outputToClient, String type) throws IOException {
        String header = "";
        byte[] data = new byte[0];

        File f = Path.of("Core", "target", "web", "index.html").toFile();
        if (!f.exists() && !f.isDirectory()) {
            header = "HTTP/1.1 404 Not Found\r\nContent-length: 0\r\n\r\n";
        } else {
            try (FileInputStream fileInputStream = new FileInputStream(f)) {
                data = new byte[(int) f.length()];
                fileInputStream.read(data);
                String contentType = Files.probeContentType(f.toPath());
                header = "HTTP/1.1 200 OK\r\nContent-type: " + contentType + "\r\nContent-length: " + data.length + "\r\n\r\n";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        outputToClient.write(header.getBytes());
        if (type.equals("GET")) {
            outputToClient.write(data);
            outputToClient.flush();
        }

    }


    private static void sendDogImageResponse(OutputStream outputToClient, String type) throws IOException {

        String header = "";
        byte[] data = new byte[0];

        File f = Path.of("Core", "target", "web", "dog.jpg").toFile();
        if (!f.exists() && !f.isDirectory()) {
            header = "HTTP/1.1 404 Not Found\r\nContent-length: 0\r\n\r\n";
        } else {

            try (FileInputStream fileInputStream = new FileInputStream(f)) {

                data = new byte[(int) f.length()];
                fileInputStream.read(data);
                String contentType = Files.probeContentType(f.toPath());
                header = "HTTP/1.1 200 OK\r\nContent-Type: " + contentType + "\r\nContent-length: " + data.length + "\r\n\r\n";
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        outputToClient.write(header.getBytes());
        if (type.equals("GET")) {
            outputToClient.write(data);
            outputToClient.flush();
        }
    }


    private static void sendImageResponse(OutputStream outputToClient, String type) throws IOException {

        String header = "";
        byte[] data = new byte[0];

        File f = Path.of("Core", "target", "web", "cat.png").toFile();
        if (!f.exists() && !f.isDirectory()) {
            header = "HTTP/1.1 404 Not Found\r\nContent-length: 0\r\n\r\n";
        } else {

            try (FileInputStream fileInputStream = new FileInputStream(f)) {

                data = new byte[(int) f.length()];
                fileInputStream.read(data);
                String contentType = Files.probeContentType(f.toPath());
                header = "HTTP/1.1 200 OK\r\nContent-Type: " + contentType + "\r\nContent-length: " + data.length + "\r\n\r\n";
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        outputToClient.write(header.getBytes());
        if (type.equals("GET")) {
            outputToClient.write(data);
            outputToClient.flush();
        }
    }

    private static void sendPdfResponse(OutputStream outputToClient, String type) throws IOException {

        String header = "";
        byte[] data = new byte[0];

        Path f = Paths.get("Core/target/web/Laboration1.pdf");
        if (f.getFileName() == null) {
            header = "HTTP/1.1 404 Not Found\r\nContent-length: 0\r\n\r\n";
        } else {

            try (FileInputStream fileInputStream = new FileInputStream(String.valueOf(f))) {

                data = Files.readAllBytes(f);
                fileInputStream.read(data);
                String contentType = Files.probeContentType(f);
                header = "HTTP/1.1 200 OK\r\nContent-Type: " + contentType + "\r\nContent-length: " + data.length + "\r\n\r\n";
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        outputToClient.write(header.getBytes());
        if (type.equals("GET")) {
            outputToClient.write(data);
            outputToClient.flush();
        }
    }

    private static void sendJsonResponse(OutputStream outputToClient, String type) throws IOException {


        List<WebserverDbEntity> persons = DBConnection.getPeopleFromDb();

        Gson gson = new Gson();
        String json = gson.toJson(persons);
        System.out.println(json);

        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        String header = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-length: " + data.length + "\r\n\r\n";

        outputToClient.write(header.getBytes());
        if (type.equals("GET")) {
            outputToClient.write(data);
            outputToClient.flush();
        }
    }


    private static String readRequest(BufferedReader inputFromClient) throws IOException {

        String type = "";
        String url = "";

        while (true) {
            String line = inputFromClient.readLine();
            if (line.startsWith("GET")) {
                type = "GET";
                url = line.split(" ")[1];

            } else if (line.startsWith("HEAD")) {
                type = "HEAD";
                url = line.split(" ")[1];

            } else if (line.startsWith("POST")) {
                type = "POST";
                url = line.split(" ")[1];
            }
            return type + " " + url;
        }
    }
}
