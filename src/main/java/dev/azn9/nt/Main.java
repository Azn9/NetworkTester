package dev.azn9.nt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {

    private static boolean does8ping = true;
    private static boolean does1ping = true;

    public static void main(String[] args) {
        Logger logger = LogManager.getLogger(Main.class);

        logger.info("");
        logger.info("=================");
        logger.info("  NetworkTester  ");
        logger.info("=================");
        logger.info("");

        Scanner scanner = new Scanner(System.in);

        Thread testThread = new Thread(() -> {
            while (true) {
                try {
                    Main.loop(logger);
                } catch (Exception exception) {
                    logger.error("Error", exception);
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
            }
        });
        testThread.setDaemon(true);
        testThread.start();

        while (true) {
            String input;

            try {
                input = scanner.nextLine();
            } catch (NoSuchElementException ignored) {
                continue;
            }

            if ("stop".equalsIgnoreCase(input)) {
                break;
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }

        testThread.interrupt();

        logger.traceExit();

        System.exit(0);
    }

    private static void loop(Logger logger) throws MalformedURLException {
        logger.trace("Pinging 8.8.8.8...");
        long currentMilis = System.currentTimeMillis();
        InetAddress inet = null;
        try {
            inet = InetAddress.getByName("8.8.8.8");
        } catch (UnknownHostException exception) {
            logger.fatal("Unknown host 8.8.8.8", exception);
        }

        try {
            if (inet != null && !inet.isReachable(1000)) {
                if (Main.does8ping) {
                    logger.fatal("Lost connection to 8.8.8.8 !");
                    Main.does8ping = false;
                } else {
                    logger.info("8.8.8.8 is alive !");
                    Main.does8ping = true;
                }
            }
        } catch (IOException exception) {
            logger.fatal("IOE 8.8.8.8", exception);
        }
        logger.trace("8.8.8.8 - RTT : " + (System.currentTimeMillis() - currentMilis) + "ms");

        logger.trace("Pinging 1.1.1.1...");
        currentMilis = System.currentTimeMillis();
        inet = null;
        try {
            inet = InetAddress.getByName("1.1.1.1");
        } catch (UnknownHostException exception) {
            logger.fatal("Unknown host 1.1.1.1", exception);
        }

        try {
            if (inet != null && !inet.isReachable(1000)) {
                if (Main.does1ping) {
                    logger.fatal("Lost connection to 1.1.1.1 !");
                    Main.does1ping = false;
                } else {
                    logger.info("1.1.1.1 is alive !");
                    Main.does1ping = true;
                }
            }
        } catch (IOException exception) {
            logger.fatal("IOE 1.1.1.1", exception);
        }
        logger.trace("1.1.1.1 - RTT : " + (System.currentTimeMillis() - currentMilis) + "ms");

        logger.trace("Trying to get https://example.com/");
        currentMilis = System.currentTimeMillis();
        URL url = new URL("https://example.com/");
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
        } catch (SocketTimeoutException timeoutException) {
            logger.fatal("Connection timed out !", timeoutException);
        } catch (IOException exception) {
            logger.fatal("IOE", exception);
        }

        if (connection != null) {
            try {
                int code;
                if ((code = connection.getResponseCode()) != 200) {
                    throw new IOException("Response code: " + code);
                }
            } catch (IOException exception) {
                logger.fatal("IOE2", exception);
            }
        }
        logger.trace("WEBSITE - RTT : " + (System.currentTimeMillis() - currentMilis) + "ms");
    }

}