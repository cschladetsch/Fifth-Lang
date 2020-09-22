import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class App {
    private static ILogger log;

    public static void main(String[] argv) {
        try {
            System.exit(new App().run(argv));
        } catch (Exception e) {
            for (StackTraceElement frame : e.getStackTrace()) {
                log.error(frame.toString());
            }
            System.exit(-1);
        }
    }

    private boolean stageFailed(String fileName, ProcessBase process) {
        if (process.run() && !process.hasFailed()) {
            return false;
        }

        log.debug(process.toString());
        log.debug(process.getClass().getSimpleName() + " Failed");
        return true;
    }

    private int run(String fileName) {
        Optional<List<String>> lines = fileContents(fileName);
        log.debug("File: " + fileName);
        if (!lines.isPresent()) {
            log.error("Failed to read " + fileName);
            return -1;
        }

        Lexer lexer = new Lexer(log, lines.get());
        if (stageFailed(fileName, lexer)) {
            return -1;
        }

        Parser parser = new Parser(lexer);
        if (stageFailed(fileName, parser)) {
            return -1;
        }

        Translator translator = new Translator(parser);
        if (stageFailed(fileName, translator)) {
            return -1;
        }

        Executor executor = new Executor(log);
        executor.contextPush(translator.getContinuation());
        if (stageFailed(fileName, executor)) {
            return -1;
        }

        log.verbose(10, lexer.toString());
        log.verbose(10, parser.toString());
        log.verbose(10, translator.toString());
        log.verbose(10, executor.toString());

        return 0;
    }

    private int run(String[] argv) {
        log = new Logger();
        log.info("Fifth-lang Repl");

        for (String fileName : argv) {
            if (run(fileName) != 0) {
                return -1;
            }
        }

        //Repl();

        return 0;
    }

    private void Repl()
    {
        Executor executor = new Executor(log);
        while (true) {
            System.out.print("λ ");
            String text = System.console().readLine();

            Lexer lexer = new Lexer(log, text);
            if (!lexer.run()) {
                continue;
            }

            Parser parser = new Parser(lexer);
            if (!parser.run()) {
                continue;
            }

            Translator translator = new Translator(parser);
            if (!translator.run()) {
                continue;
            }

            if (!executor.run(translator.getContinuation())) {
                continue;
            }

            int n = 0;
            for (Object obj : executor.getDataStack()) {
                System.out.printf("[%d]: %s%n", n++, obj.toString());
            }
        }
    }

    private Optional<List<String>> fileContents(String fileName) {
        try {
            return Optional.of(Files.readAllLines(Paths.get(fileName)));
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return Optional.empty();
    }
}
