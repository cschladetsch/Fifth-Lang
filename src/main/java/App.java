//import net.sf.json.JSONObject;
//import org.apache.commons.lang.StringUtils;


import java.io.Console;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class App {
    private static ILogger log;
    private Object tests;
    private Object options;

    public App() {
        FileUtil.contents("config.json").ifPresent(this::parseConfig);
    }

    public static void main(String[] argv) {
        log = new Logger();
        log.info("fifth-sys v0.1");
        log.setVerbosity(0);

        // How to read a line ffs?
//        String text = System.console().readLine("λ ");
//        log.info("You entered: " + text);

        int result;
        try {
            result = new App().run(argv);
        } catch (Exception e) {
            log.error("Main: " + e.toString());
            for (StackTraceElement frame : e.getStackTrace()) {
                log.error("Main: " + frame.toString());
            }

            result = -1;
        }

        log.close();

        System.exit(result);
    }

    private void parseConfig(List<String> jsonText) {
//        String allText = StringUtils.join(jsonText, "");
//        JSONObject json = JSONObject.fromObject(allText);
//        tests = json.get("tests");
//        options = json.get("options");
    }

    private int run(String[] argv) {
        for (String fileName : argv) {
            if (!run(fileName)) {
                return -1;
            }
        }

        return 0;
    }

    private boolean run(String fileName) {
        File root = Paths.get(fileName).toFile();
        if (root.isDirectory()) {
            return runAll(root);
        }

        String extension = FileUtil.getAllFileExtension(fileName);
        switch (extension) {
            case "pi":
            case "md":
                break;
            case "out.md":
            default:
                return true;
        }

        CodeSource codeSource = new CodeSource(log, Paths.get(fileName));
        log.info("File: " + fileName);
        codeSource.run();
        if (codeSource.hasFailed()) {
            log.error("Couldn't read from " + fileName);
            return false;
        }

        PiExecutionContext context = new PiExecutionContext(log, codeSource.getCodeText());
        if (!context.run()) {
            log.info("Failed");
            log.debug(context);
        }

        codeSource.close();
        return true;
    }

    private boolean runAll(File root) {
        boolean success = true;
        for (File file : Objects.requireNonNull(root.listFiles())) {
            if (file.isFile()) {
                if (!run(file.getAbsolutePath())) {
                    success = false;
                }
            }
        }

        return success;
    }
}
