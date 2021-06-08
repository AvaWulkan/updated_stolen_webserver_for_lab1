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

    static String url = "";
    static String type = "";

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
            type = inputHeader.split(" ")[0];
            url = inputHeader.split(" ")[1];

            OutputStream outputToClient = client.getOutputStream();


            if (type.equals("GET") || type.equals("HEAD")) {
                if (url.contains("storage")) {
                    if (url.startsWith("/storage?id=")) {
                        findById(url,outputToClient, type);
                    } else if (url.equals("/storage")) {
                        sendJsonResponse(outputToClient, type);
                    }
                }
                sendGETResponse(outputToClient, type);
            } else if (type.equals("POST")) {
                if(url.equals("/storage")) {
                    sendJsonResponse(outputToClient, type);
                }
                else if(url.startsWith("/storage?id=")) {
                    findById(url, outputToClient, type);
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

    private static void findById(String url, OutputStream outputToClient, String type) throws IOException {
        String id = url.replaceAll("[^0-9.]", "");
        int dbid = Integer.parseInt(id);

        WebserverDbEntity person = DBConnection.sendIdResponse(dbid);
        String parameter = "";
        if (url.contains("&")) {
            parameter = url.split("&")[1];
        }
        if (parameter.startsWith("changename")) {
            String name = parameter.split("=")[1];

            person = DBConnection.sendNameUpdate(dbid, name);
        }

        Gson gson = new Gson();
        String json = gson.toJson(person);
        json = EncodingDecoding.encode(json);
        System.out.println(json);

        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        String header = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-length: " + data.length + "\r\n\r\n";

        outputToClient.write(header.getBytes());
        outputToClient.write(data);
        outputToClient.flush();
    }

    private static void send404Response(OutputStream outputToClient) throws IOException {
        String header = "HTTP/1.1 404 Not Found\r\nContent-length: 0\r\n\r\n";
        outputToClient.write(header.getBytes());
        outputToClient.flush();
    }

    private static void sendGETResponse(OutputStream outputToClient, String type) throws IOException {
        String header = "";
        byte[] data = new byte[0];

        File f = Path.of("Core", "target", "web", url).toFile();
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

    private static void sendJsonResponse(OutputStream outputToClient, String type) throws IOException {

        List<WebserverDbEntity> persons = DBConnection.getPeopleFromDb();

        Gson gson = new Gson();
        String json = gson.toJson(persons);
        System.out.println(json);

        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        String header = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-length: " + data.length + "\r\n\r\n";

        outputToClient.write(header.getBytes());
        if (type.equals("GET") || type.equals("POST")) {
            outputToClient.write(data);
            outputToClient.flush();
        }
    }


    private static String readRequest(BufferedReader inputFromClient) throws IOException {

        String type = "";
        String url = "";

        while (true) {
            String line = inputFromClient.readLine();
            line = EncodingDecoding.decode(line);
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
