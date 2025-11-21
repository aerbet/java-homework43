import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StaticFileHandler implements HttpHandler {
    private final Path baseDir;

    public StaticFileHandler(String baseDir) {
        this.baseDir = Paths.get(baseDir).toAbsolutePath().normalize();
    }

    public void handle(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();

        if (path.equals("/")) {
            path = "/index.html";
        }

        Path filePath = baseDir.resolve(path.substring(1)).normalize();

        if (!filePath.startsWith(baseDir)) {
            send404(exchange, "Access Denied");
            return;
        }

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            send404(exchange, "File not found: " + path);
            return;
        }

        String contentType = determineContentType(filePath);
        sendFile(exchange, filePath, contentType);
    }

    private String determineContentType(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".html")) return "text/html; charset=utf-8";
        if (fileName.endsWith(".css")) return "text/css; charset=utf-8";
        if (fileName.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (fileName.endsWith(".json")) return "application/json; charset=utf-8";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".svg")) return "image/svg+xml";

        return "text/plain; charset=utf-8";
    }

    private void sendFile(HttpExchange exchange, Path filePath, String contentType) throws IOException {
        try {
            byte[] fileBytes = Files.readAllBytes(filePath);
            exchange.getResponseHeaders().add("Content-Type", contentType);
            exchange.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
            send404(exchange, "Server Error reading file");
        }
    }

    private void send404(HttpExchange exchange, String message) throws IOException {
        String response = "<h1>404 Not Found</h1><p>" + message + "</p>";
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        byte[] bytes = response.getBytes();
        exchange.sendResponseHeaders(404, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
        System.err.println("404 Error: " + message);
    }
}