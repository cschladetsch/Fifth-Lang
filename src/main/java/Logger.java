import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger implements ILogger {
    //private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("mm:ss");
    private static final LocalDateTime startTime = LocalDateTime.now();

    public void debug(String text) {
        print(System.out, "debug", text);
    }

    public void warning(String text) {
        print(System.out, "warn", text);
    }

    public void info(String text) {
        print(System.out, "info", text);
    }

    public void error(String text) {
        print(System.err, "error", text);
    }

    public void error(Exception e) {
        error(e.toString());
        e.printStackTrace();
    }

    private String timeStamp() {
        LocalDateTime localDateTime = LocalDateTime.now();
        Duration since = Duration.between(startTime, localDateTime);
        long secs = since.getSeconds();
        long mins = secs/60;
        long millis = since.toMillis();
        return String.format("%2s:%2s:%3s", mins, secs, millis);
    }

    private void print(PrintStream out, String type, String text) {
        out.println(String.format("%s: %s: %s", timeStamp(), type, text));
    }
}
