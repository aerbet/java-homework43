import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import server.BaseServer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Main {
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
        server.createContext("/", Main::handleRequest);
        server.createContext("/apps/", Main::handleRequest);
        server.createContext("/apps/profile", Main::handleRequest);
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
}