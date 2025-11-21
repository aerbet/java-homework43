import com.sun.net.httpserver.HttpServer;
import server.BaseServer;
import java.io.*;

public class Main {
    private static final int PORT = 8089;

    public static void main(String[] args) {
        try {
            HttpServer server = BaseServer.makeServer();
            initRoutes(server);
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initRoutes(HttpServer server) {
        server.createContext("/apps/profile", new ProfileHandler());
        server.createContext("/apps/", new AppsHandler());
        server.createContext("/", new StaticFileHandler("src/data"));
    }
}