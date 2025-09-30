package dev.mkeo102.logger;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class Logger implements TerminalColors {

    private List<PrintStream> outputs = new ArrayList<>();
    private final String name;

    private final boolean debug;


    private Logger(Class<?> clazz) {
        this.name = clazz.getName();
        this.debug = false;
        this.outputs.add(System.out);
    }

    private Logger(String name) {
        this.name = name;
        this.debug = false;
        this.outputs.add(System.out);
    }

    private Logger(Class<?> clazz, boolean debug) {
        this.name = clazz.getName();
        this.debug = debug;
        this.outputs.add(System.out);
    }

    private Logger(String name, boolean debug) {
        this.name = name;
        this.debug = debug;
        this.outputs.add(System.out);
    }

    public void log(LoggerType type, String message) {
        String formatted = format("%s[%s] [%tT] %s%s", type.getTerminalColor(), type.getTypeInfo(), LocalDateTime.now(), message, RESET);
        this.outputs.forEach(out -> out.println(formatted));
    }

    public void silentLog(LoggerType type, String message) {
        String formatted = String.format("%s %s%s", type.getTerminalColor(), message, RESET);
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
        log(new ErrorType(), message);
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


    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz);
    }

    static Logger getLogger(String name) {
        return new Logger(name);
    }

    public static Logger getLogger(Class<?> clazz, boolean debug) {
        return new Logger(clazz, debug);
    }

    static Logger getLogger(String name, boolean debug) {
        return new Logger(name, debug);
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
            String safeArg = o == null ? "null" : Matcher.quoteReplacement(o.toString());

            if(!match.find()) throw new IllegalArgumentException(format("Too many arguments provided for format string: {}", format.replace("{", "\\{")));
            format = match.replaceFirst(safeArg);
        }

        if(replacePattern.matcher(format).find()) throw new MissingFormatArgumentException(format);

        format = format.replace("\\{", "{");

        return format;
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
        public static final ErrorType instance = new ErrorType();

        public ErrorType() {
            super("ERROR", RED);
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
