package dev.mkeo102.logger;

import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class Logger implements TerminalColors {
    private final ErrorPath errorPath;

    private static boolean muted = false;

    private List<PrintStream> outputs = new ArrayList<>();
    private final String name;

    private final boolean debug;

    public static class ErrorPath {
        private final String light;
        private final URL host;
        private final String key;

        public ErrorPath(String light, URL host, String key) {
            this.light = light;
            this.host = host;
            this.key = key;
        }

        public String getLight() {
            return light;
        }

        public URL getHost() {
            return host;
        }

        public String getKey() {
            return key;
        }
    }

    private Logger(Class<?> clazz, ErrorPath errorPath) {
        this.name = clazz.getName();
        this.debug = false;
        this.outputs.add(System.out);
        this.errorPath = errorPath;
    }

    private Logger(String name, ErrorPath errorPath) {
        this.name = name;
        this.debug = false;
        this.outputs.add(System.out);
        this.errorPath = errorPath;
    }

    private Logger(Class<?> clazz, boolean debug, ErrorPath errorPath) {
        this.name = clazz.getName();
        this.debug = debug;
        this.outputs.add(System.out);
        this.errorPath = errorPath;
    }

    private Logger(String name, boolean debug, ErrorPath errorPath) {
        this.name = name;
        this.debug = debug;
        this.outputs.add(System.out);
        this.errorPath = errorPath;
    }

    public void log(LoggerType type, String message) {
        if(muted()) return;
        // Using String.format here for the time formatting
        String formatted = String.format("%s[%s] [%tT] %s%s", type.getTerminalColor(), type.getTypeInfo(), LocalDateTime.now(), message, RESET);
        this.outputs.forEach(out -> out.println(formatted));
    }

    public void silentLog(LoggerType type, String message) {
        if(muted()) return;
        String formatted = format("{color} {message}{color-reset}", type.getTerminalColor(), message, RESET);
        outputs.forEach(out -> out.println(formatted));
    }

    public void silentLog(LoggerType type, String message, Object... formats) {
        silentLog(type, format(message, formats));
    }

    public void log(LoggerType type, String message, Object... formats) {
        log(type, format(message, formats));
    }

    public void info() {
        info("");
    }

    public void info(String message) {
        log(new InfoType(), message);
    }

    public void info(String message, Object... formats) {
        info(format(message, formats));
    }

    public void warning() {
        warning("");
    }

    public void warning(String message) {
        log(new WarningType(), message);
    }

    public void warning(String message, Object... formats) {

        warning(format(message, formats));
    }

    public void error(String message) {
        log(new ErrorType(errorPath), message);
    }

    public void error() {
        error("");
    }

    public void error(String message, Object... formats) {
        error(format(message, formats));
    }

    public void exception(Throwable t) {
        log(new ExceptionType(), "{type} {message}", t.getClass().getName(), t.getMessage());
        StackTraceElement[] stackTrace = t.getStackTrace();
        for (StackTraceElement ste : stackTrace) {
            log(new StackTraceType(), "    at {class}.{method}({file name}:{line})", ste.getClassName(), ste.getMethodName(), ste.getFileName(), ste.getLineNumber());
        }
    }

    public void debug(String message) {
        if (debug)
            log(new DebugType(), message);
    }

    public void debug() {
        debug("");
    }

    public void debug(String message, Object... formats) {
        debug(format(message, formats));
    }


    public static Logger getLogger(Class<?> clazz, ErrorPath errorPath) {
        return new Logger(clazz, errorPath);
    }

    static Logger getLogger(String name, ErrorPath errorPath) {
        return new Logger(name, errorPath);
    }

    public static Logger getLogger(Class<?> clazz, boolean debug, ErrorPath errorPath) {
        return new Logger(clazz, debug, errorPath);
    }

    static Logger getLogger(String name, boolean debug, ErrorPath errorPath) {
        return new Logger(name, debug, errorPath);
    }

    public void addOutput(PrintStream stream) {
        this.outputs.add(stream);
    }

    public void resetOutputs() {
        this.outputs = new ArrayList<>();
        this.outputs.add(System.out);
    }

    public void removeOutput(PrintStream stream) {
        this.outputs.remove(stream);
    }

    static String format(String format, Object... args) {
        if(args == null) args = new Object[]{null};

        Pattern replacePattern = Pattern.compile("(?<!\\\\)\\{[^}]*}");


        for(Object o : args) {
            Matcher match = replacePattern.matcher(format);
            String safeArg = o == null ? "null" : Matcher.quoteReplacement(escape(o.toString()));

            if(!match.find()) throw new IllegalArgumentException(format("Too many arguments provided for format string: {}", escape(format)));
            format = match.replaceFirst(safeArg);
        }

        if(replacePattern.matcher(format).find()) throw new MissingFormatArgumentException(format);

        format = format.replace("\\{", "{");

        return format;
    }

    static String escape(String input ){
        return input.replace("{", "\\{");
    }

    public static void setMuted(boolean muted) {
        Logger.muted = muted;
    }

    public static boolean muted() {
        return muted;
    }

    private static class InfoType extends LoggerType {
        public static final InfoType instance = new InfoType();
        public InfoType() {
            super("INFO", RESET);
        }
    }

    private static class WarningType extends LoggerType {
        public static final WarningType instance = new WarningType();
        public WarningType() {
            super("WARNING", YELLOW);
        }
    }

    private static class ErrorType extends LoggerType {
        public ErrorType(ErrorPath errorPath) {
            super("ERROR", RED);
            try {
                HttpURLConnection conn = (HttpURLConnection) errorPath.getHost().openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + errorPath.getKey());
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String payload = String.format(
                        "{ \"entity_id\": \"%s\", \"rgb_color\": [%d, %d, %d], \"brightness\": 255 }",
                        errorPath.light,
                        Color.RED.getRed(),
                        Color.RED.getGreen(),
                        Color.RED.getBlue()
                );

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                conn.getResponseCode();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    private static class DebugType extends LoggerType {
        public static final DebugType instance = new DebugType();
        public DebugType() {
            super("DEBUG", GREEN);
        }
    }

    private static class ExceptionType extends LoggerType {
        public static final ExceptionType instance = new ExceptionType();
        public ExceptionType() {
            super("EXCEPTION", RED);
        }
    }

    private static class StackTraceType extends LoggerType {
        public static final StackTraceType instance = new StackTraceType();

        public StackTraceType() {
            super("  TRACE  ", RED);
        }
    }


}
