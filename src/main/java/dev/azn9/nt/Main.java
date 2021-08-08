package dev.azn9.nt;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.UUID;

public class Main {

    private static final String token = System.getenv("INFLUX_TOKEN");
    private static final String bucket = "data";
    private static final String org = "azn9";
    private static final Map<UUID, Boolean> threadStatus = new HashMap<>();
    private static final Map<UUID, Thread> threads = new HashMap<>();

    private static WriteApi writeApi;
    private static Logger logger;

    public static void main(String[] args) {
        InfluxDBClient client = InfluxDBClientFactory.create("http://localhost:8086", token.toCharArray());
        Main.writeApi = client.getWriteApi();
        Main.logger = LogManager.getLogger(Main.class);

        logger.info("");
        logger.info("=================");
        logger.info("  NetworkTester  ");
        logger.info("=================");
        logger.info("");

        Scanner scanner = new Scanner(System.in);

        Main.createTest(false, "8.8.8.8", 200);
        Main.createTest(false, "1.1.1.1", 200);
        Main.createTest(true, "https://example.com", 200);
        Main.createTest(true, "https://api.twitch.tv/helix/streams/", 401);

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

        new HashMap<>(Main.threadStatus).forEach((uuid, aBoolean) -> {
            Main.threadStatus.put(uuid, false);
            try {
                Main.threads.remove(uuid).join();
            } catch (NullPointerException ignored) {
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        logger.traceExit();
        client.close();

        System.exit(0);
    }

    private static void createTest(boolean isUrl, String host, int code) {
        UUID threadUuid = UUID.randomUUID();

        Thread thread = new Thread(() -> {
            while (Main.threadStatus.getOrDefault(threadUuid, true)) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }

                long currentMilis = System.currentTimeMillis();

                if (isUrl) {
                    try {
                        URL url = new URL(host);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(5000);
                        connection.setReadTimeout(5000);

                        if (connection.getResponseCode() != code) {
                            throw new IOException("Invalid response code");
                        }
                    } catch (Exception e) {
                        logger.fatal("Error " + host, e);
                        continue;
                    }
                } else {
                    InetAddress inet;
                    try {
                        inet = InetAddress.getByName("8.8.8.8");
                    } catch (UnknownHostException exception) {
                        logger.fatal("Unknown host " + host, exception);
                        continue;
                    }

                    try {
                        inet.isReachable(5000);
                    } catch (IOException exception) {
                        logger.fatal("IOE " + host, exception);
                        continue;
                    }
                }

                Point point = Point
                        .measurement("ping")
                        .addTag("host", host)
                        .addField("rtt", System.currentTimeMillis() - currentMilis)
                        .time(Instant.now(), WritePrecision.MS);

                Main.writeApi.writePoint(bucket, org, point);
            }
        });
        thread.setDaemon(true);
        thread.start();

        Main.threadStatus.put(threadUuid, true);
        Main.threads.put(threadUuid, thread);
    }

}