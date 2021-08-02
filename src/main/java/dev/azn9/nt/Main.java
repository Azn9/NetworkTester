package dev.azn9.nt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
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
        while (true) {
            String input;

            try {
                input = scanner.nextLine();
            } catch (NoSuchElementException ignored) {
                continue;
            }

            if ("stop".equalsIgnoreCase(input))
                break;

            try {
                loop(logger);
            } catch (Exception e) {
                logger.error("Error", e);
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        logger.traceExit();

        System.exit(0);
    }

    private static void loop(Logger logger) throws MalformedURLException {
        logger.trace("Pinging 8.8.8.8...");
        long currentMilis = System.currentTimeMillis();
        InetAddress inet = null;
        try {
            inet = InetAddress.getByName("8.8.8.8");
        } catch (UnknownHostException e) {
            logger.fatal("Unknown host 8.8.8.8", e);
        }

        try {
            if (inet != null && !inet.isReachable(1000)) {
                if (does8ping) {
                    logger.fatal("Lost connection to 8.8.8.8 !");
                    does8ping = false;
                } else {
                    logger.info("8.8.8.8 is alive !");
                    does8ping = true;
                }
            }
        } catch (IOException e) {
            logger.fatal("IOE 8.8.8.8", e);
        }
        logger.trace("RTT : " + (System.currentTimeMillis() - currentMilis) + "ms");

        logger.trace("Pinging 1.1.1.1...");
        currentMilis = System.currentTimeMillis();
        inet = null;
        try {
            inet = InetAddress.getByName("1.1.1.1");
        } catch (UnknownHostException e) {
            logger.fatal("Unknown host 1.1.1.1", e);
        }

        try {
            if (inet != null && !inet.isReachable(1000)) {
                if (does1ping) {
                    logger.fatal("Lost connection to 1.1.1.1 !");
                    does1ping = false;
                } else {
                    logger.info("1.1.1.1 is alive !");
                    does1ping = true;
                }
            }
        } catch (IOException e) {
            logger.fatal("IOE 1.1.1.1", e);
        }
        logger.trace("RTT : " + (System.currentTimeMillis() - currentMilis) + "ms");

        logger.trace("Trying to get https://example.com/");
        currentMilis = System.currentTimeMillis();
        URL url = new URL("https://example.com/");
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
        } catch (IOException e) {
            logger.fatal("IOE", e);
        }

        if (connection != null) {
            try {
                int code;
                if ((code = connection.getResponseCode()) != 200) {
                    throw new IOException("Response code: " + code);
                }
            } catch (IOException e) {
                logger.fatal("IOE2", e);
            }
        }
        logger.trace("RTT : " + (System.currentTimeMillis() - currentMilis) + "ms");
    }

}