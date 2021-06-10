package clientsources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {

        try {
            Socket socket = new Socket("localhost", 80);

            PrintWriter output = new PrintWriter(socket.getOutputStream());
            output.print("HTTP/1.1\r\n");
            output.print("Host: LocalHost\r\n");
            output.print("\r\n");

            output.flush();

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while(true) {

                String line = input.readLine();
                if(line == null || line.isEmpty()) {
                    break;
                }
                System.out.println(line);
            }
            input.close();
            output.close();
            socket.close();

        }
        catch(IOException e) {

            e.printStackTrace();
        }
    }
}
