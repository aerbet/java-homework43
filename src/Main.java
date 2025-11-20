import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.nio.charset.StandardCharsets;
import com.sun.net.httpserver.Headers;
import java.nio.charset.Charset;
import server.BaseServer;
import java.net.URI;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    private static final Path ROOT = Paths.get("src/data");

    public static void main(String[] args) {
        try {
            HttpServer server = BaseServer.makeServer();
            initRoutes(server);
            server.start();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void initRoutes(HttpServer server) {
        server.createContext("/index.html", Main::handlePage);
        server.createContext("/", Main::handleRequest);
        server.createContext("/apps/", Main::handleRequestApps);
        server.createContext("/apps/profile", Main::handleRequestProfile);
    }

    private static void handlePage(HttpExchange exchange) {
        byte[] data;
        try {
            data = Files.readAllBytes(ROOT.resolve("index.html"));
        } catch (IOException ioe) {
            try {
                String errorMsg = "404 Not Found" + ROOT.resolve("index.html");
                exchange.sendResponseHeaders(404, errorMsg.getBytes(StandardCharsets.UTF_8).length);
                exchange.getResponseBody().write(errorMsg.getBytes(StandardCharsets.UTF_8));
            } catch (IOException innerIoe) {
                innerIoe.printStackTrace();
            }
            System.err.println("Error reading file: " + ROOT.resolve("index.html"));
            ioe.printStackTrace();
            return;
        }

        try {
            exchange.getResponseHeaders().add("Content-Type", "text/html;charset=utf-8");
            int responseCode = 200;
            int length = data.length;

            exchange.sendResponseHeaders(responseCode, length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(data);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void handleRequestProfile(HttpExchange exchange) {
        try {
            exchange.getResponseHeaders().add("Content-Type", "text/plain;charset=utf-8");
            int responseCode = 200;
            int length = 0;
            exchange.sendResponseHeaders(responseCode, length);

            try (PrintWriter writer = getWriterFrom(exchange)) {
                write(writer, "Pentagon", "President DNA is confirmed!");
                write(writer, "Pentagon", "Welcome Mr. President!");
                writer.flush();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void handleRequestApps(HttpExchange exchange) {
        try {
            exchange.getResponseHeaders().add("Content-Type", "text/plain;charset=utf-8");
            int responseCode = 200;
            int length = 0;
            exchange.sendResponseHeaders(responseCode, length);

            try (PrintWriter writer = getWriterFrom(exchange)) {
                write(writer, "Play market", "Your application is suspected of having virus!");
                write(writer, "Play market", "Fix your issue or we'll delete it from our market!");
                writer.flush();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void handleRequest(HttpExchange exchange) {
        try {
            exchange.getResponseHeaders().add("Content-Type", "text/plain;charset=utf-8");
            int responseCode = 200;
            int length = 0;
            exchange.sendResponseHeaders(responseCode, length);

            try (PrintWriter writer = getWriterFrom(exchange)) {
                String method = exchange.getRequestMethod();
                URI uri = exchange.getRequestURI();
                String ctxPath = exchange.getHttpContext().getPath();

                write(writer, "HTTP method", method);
                write(writer, "Request", uri.toString());
                write(writer, "Handler", ctxPath);
                writeHeaders(writer, "Request headers", exchange.getRequestHeaders());
                writeData(writer, exchange);
                writer.flush();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void writeHeaders(Writer writer, String type, Headers headers) {
        write(writer, type, "");
        headers.forEach((k, v) -> write(writer, "\t" + k, v.toString()));
    }

    private static void write(Writer writer, String msg, String method) {
        String data = String.format("%s: %s%n%n", msg, method);
        try {
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static PrintWriter getWriterFrom(HttpExchange exchange) {
        OutputStream outputStream = exchange.getResponseBody();
        Charset charset = StandardCharsets.UTF_8;
        return new PrintWriter(outputStream, false, charset);
    }

    private static BufferedReader getReader(HttpExchange exchange) {
        InputStream input = exchange.getRequestBody();
        Charset charset = StandardCharsets.UTF_8;
        InputStreamReader inputStreamReader = new InputStreamReader(input, charset);
        return new BufferedReader(inputStreamReader);
    }

    private static void writeData(Writer writer, HttpExchange exchange) {
        try (BufferedReader reader = getReader(exchange)) {
            if (!reader.ready()) {
                return;
            }
            write(writer, "Data", "");
            reader.lines().forEach(v -> write(writer, "\t", v));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}