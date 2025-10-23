package dev.mkeo102.logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomLoggerTest {

    @Test
    void testMultipleOutput() throws Exception {

      Logger logger = Logger.getLogger("Test");

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      final String utf8 = StandardCharsets.UTF_8.name();

      PrintStream ps = new PrintStream(baos,true,utf8);

      logger.addOutput(ps);
      logger.info("This is a test");

      assert baos.toString(utf8).contains("This is a test");
    }

    @Test
    void testDebugEffectiveness() throws Exception{
      {
        Logger logger = Logger.getLogger("Test", false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();

        PrintStream ps = new PrintStream(baos, true, utf8);

        logger.addOutput(ps);
        logger.debug("This is a test");

        assert !baos.toString(utf8).contains("This is a test");
      }

      {
        Logger logger = Logger.getLogger("Test", true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();

        PrintStream ps = new PrintStream(baos, true, utf8);

        logger.addOutput(ps);
        logger.debug("This is a test");

        assert baos.toString(utf8).contains("This is a test");
      }

    }

    @Test
    public void testExceptionPrinter() throws Throwable {
        {
            Logger logger = Logger.getLogger("Test", true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final String utf8 = StandardCharsets.UTF_8.name();

            PrintStream ps = new PrintStream(baos, true, utf8);

            logger.addOutput(ps);
            logger.exception(new IOException("This is a test exception!"));

            assert baos.toString(utf8).contains("This is a test exception!") && baos.toString(utf8).contains("testExceptionPrinter");
        }

    }

    @Test
    public void testFormatter() {
        {
            String correct = String.format("This is a test of the formatter with a String : %s", "Hello World!");
            String custom = Logger.format("This is a test of the formatter with a String : {}", "Hello World!");

            assertEquals(correct, custom, "Failed to format with a String");
        }

        {
            String correct = String.format("This is a test of the formatter with null : %s", (Object) null);
            String custom = Logger.format("This is a test of the formatter with null : {}", (Object) null);

            assertEquals(correct, custom, "Failed to format with null");
        }

        {
            String correct = String.format("This is a test of the formatter with an int : %s", 5);
            String custom = Logger.format("This is a test of the formatter with an int : {}", 5);

            assertEquals(correct, custom, "Failed to format with an int");
        }

        {
            String correct = String.format("This is a test of the formatter with a long : %s", 5L);
            String custom = Logger.format("This is a test of the formatter with a long : {}", 5L);

            assertEquals(correct, custom, "Failed to format with a long");
        }

        {
            String correct = String.format("This is a test of the formatter with a float : %s", 5.5f);
            String custom = Logger.format("This is a test of the formatter with a float : {}", 5.5f);

            assertEquals(correct, custom, "Failed to format with a float");
        }

        {
            String correct = String.format("This is a test of the formatter with a double : %s", 5.5D);
            String custom = Logger.format("This is a test of the formatter with a double : {}", 5.5D);

            assertEquals(correct, custom, "Failed to format with a double");
        }

        {
            String correct = String.format("This is a test of the formatter with a boolean : %s", true);
            String custom = Logger.format("This is a test of the formatter with a boolean : {}", true);

            assertEquals(correct, custom, "Failed to format with a boolean");
        }

        {
            String correct = String.format("This is a test of the formatter's escaping : {} %s", true);
            String custom = Logger.format("This is a test of the formatter's escaping : \\{} {}", true);

            assertEquals(correct, custom, "Failed to format with a boolean");
        }
    }

    @Test
    public void testMuting() throws UnsupportedEncodingException {
        Logger.setMuted(true);
        {
            Logger logger = Logger.getLogger("Test");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final String utf8 = StandardCharsets.UTF_8.name();

            PrintStream ps = new PrintStream(baos, true, utf8);

            logger.addOutput(ps);
            logger.info("This is a test");

            assert baos.toString(utf8).isEmpty();
        }

        Logger.setMuted(false);

        {
            Logger logger = Logger.getLogger("Test");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final String utf8 = StandardCharsets.UTF_8.name();

            PrintStream ps = new PrintStream(baos, true, utf8);

            logger.addOutput(ps);
            logger.info("This is a test");

            assert baos.toString(utf8).contains("This is a test");
        }

        Logger.setMuted(true);

        {
            Logger logger = Logger.getLogger("Test");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final String utf8 = StandardCharsets.UTF_8.name();

            PrintStream ps = new PrintStream(baos, true, utf8);

            logger.addOutput(ps);
            logger.info("This is a test");

            assert baos.toString(utf8).isEmpty();
        }

        Logger.setMuted(false);

    }

}
