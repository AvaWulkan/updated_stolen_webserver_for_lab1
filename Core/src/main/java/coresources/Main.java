package coresources;

import com.google.gson.Gson;
import domain.WebserverDbEntity;
import sourcepackage.DBConnection;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    static String url = "";
    static String urlType = "";

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
            urlType = inputHeader.split(" ")[0];
            url = inputHeader.split(" ")[1];

            OutputStream outputToClient = client.getOutputStream();

            if (urlType.equals("GET") || urlType.equals("HEAD")) {
                if (url.contains("storage")) {
                    if (url.startsWith("/storage?id=")) {
                        findById(url, outputToClient);
                    } else if (url.equals("/storage")) {
                        sendJsonResponse(outputToClient, urlType);
                    }
                }
                sendGETResponse(outputToClient, urlType);
            } else if (urlType.equals("POST")) {
                url = EncodingDecoding.decode(url);
                if (url.startsWith("/storage?id=")) {
                    String nameValue = createRequestBody(inputFromClient);
                    changeNameByIdWithPOST(nameValue, url, outputToClient);
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

    private static void changeNameByIdWithPOST(String nameValue, String url, OutputStream outputToClient) throws IOException {
        String id = url.replaceAll("[^0-9.]", "");
        int dbid = Integer.parseInt(id);

        WebserverDbEntity person = DBConnection.sendIdResponse(dbid);

        person = DBConnection.sendNameUpdate(dbid, nameValue);

        Gson gson = new Gson();
        String json = gson.toJson(person);
        System.out.println(json);

        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        String header = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-length: " + data.length + "\r\n\r\n";

        outputToClient.write(header.getBytes());
        outputToClient.write(data);
        outputToClient.flush();
    }

    private static void findById(String url, OutputStream outputToClient) throws IOException {
        String id = url.replaceAll("[^0-9.]", "");
        int dbid = Integer.parseInt(id);

        WebserverDbEntity person = DBConnection.sendIdResponse(dbid);

        Gson gson = new Gson();
        String json = gson.toJson(person);
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

    private static void sendGETResponse(OutputStream outputToClient, String urlType) throws IOException {
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
        if (urlType.equals("GET")) {
            outputToClient.write(data);
            outputToClient.flush();
        }
    }

    private static void sendJsonResponse(OutputStream outputToClient, String urlType) throws IOException {
        List<WebserverDbEntity> persons = DBConnection.getPeopleFromDb();

        Gson gson = new Gson();
        String json = gson.toJson(persons);
        System.out.println(json);

        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        String header = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-length: " + data.length + "\r\n\r\n";

        outputToClient.write(header.getBytes());
        if (urlType.equals("GET") || urlType.equals("POST")) {
            outputToClient.write(data);
            outputToClient.flush();
        }
    }


    private static String readRequest(BufferedReader inputFromClient) throws IOException {
        String urlType = "";
        String url = "";

        while (true) {
            String line = inputFromClient.readLine();
            if (line.startsWith("GET")) {
                urlType = "GET";
                url = line.split(" ")[1];
            } else if (line.startsWith("HEAD")) {
                urlType = "HEAD";
                url = line.split(" ")[1];
            } else if (line.startsWith("POST")) {
                urlType = "POST";
                url = line.split(" ")[1];
            }
            if(url.endsWith("/")){
                StringBuffer sb= new StringBuffer(url);
                sb.deleteCharAt(sb.length()-1);
                url = sb.toString();
            }
            return urlType + " " + url;
        }
    }

    private static String createRequestBody(BufferedReader inputFromClient) throws IOException {
        StringBuffer buffer = new StringBuffer();
        String string = null;
        int bodyLength = 0;
        while (!(string = inputFromClient.readLine()).equals("")) {
            buffer.append(string + "");
            if (string.startsWith("Content-Length:")) {
                bodyLength = Integer.valueOf(string.substring(string.indexOf(' ') + 1, string.length()));
            }
        }
        char[] body = new char[bodyLength];
        inputFromClient.read(body, 0, bodyLength);
        String requestBody = new String(body);
        String nameValue = requestBody.split(":")[1];
        nameValue = nameValue.split("\"")[1];
        return nameValue;
    }
}
