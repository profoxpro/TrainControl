package org.traincontrol.marklin.network;

import org.traincontrol.model.ModelListener;
import org.traincontrol.util.Conversion;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class facilitates communication with the Marklin CS2/CS3 over TCP
 */
public class TCPNetworkProxy implements NetworkProxy {

    // Connection configuration
    private final String initIP;
    private static final int CS2_PORT = 15731;
    private static final int TIMEOUT_MS = 5000;

    // Network connection
    private Socket tcpSocket;
    private InputStream in;
    private OutputStream out;
    private volatile boolean running = true;

    // Model listener class reference
    private ModelListener model;

    // Thread pool
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public TCPNetworkProxy(String initIP) {
        this.initIP = initIP;
    }

    @Override
    public void start() {
        connect();
        subscribe();
        startReadingThread();
        scheduleHeartbeats();
    }

    @Override
    public void setModel(ModelListener model) {
        // Set reference
        this.model = model;

        model.log("Initializing CAN listener on " + this.initIP + "...");
    }

    private void startReadingThread() {
        Executors.newSingleThreadExecutor().execute(() -> {
            byte[] buffer = new byte[CS2Message.MESSAGE_LENGTH * 2];
            while (running) {
                try {
                    int len = in.read(buffer);
                    if (len == -1) break;

                    for (int offset = 0; offset + CS2Message.MESSAGE_LENGTH <= len;
                         offset += CS2Message.MESSAGE_LENGTH) {
                        byte[] messageData = Arrays.copyOfRange(buffer, offset, offset + CS2Message.MESSAGE_LENGTH);
                        System.out.printf("Received raw data: " + Conversion.bytesToHex(messageData));

                        CS2Message message = new CS2Message(messageData);
                        System.out.printf(message.toString());
                        //receiveMessage(message);
                        model.receiveMessage(message);
                    }
                } catch (SocketTimeoutException e) {
                    continue;
                } catch (IOException e) {
                    if (running) System.out.printf("Read error: " + e.getMessage());
                    break;
                }
            }
            shutdown();
        });
    }

    @Override
    public boolean sendMessage(CS2Message message) {
        synchronized (out) {
            try {
                out.write(message.getRawMessage());
                out.flush();
                System.out.printf("Message sent: " + message);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private void connect() {
        System.out.println("Connecting to CS2 " + this.initIP + ":" + CS2_PORT);
        tcpSocket = new Socket();
        try {
            tcpSocket.connect(new InetSocketAddress(this.initIP, CS2_PORT), TIMEOUT_MS);
            tcpSocket.setSoTimeout(TIMEOUT_MS);
            in = tcpSocket.getInputStream();
            out = tcpSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Connection established");
        }
    }

    private void subscribe() {
        byte[] clientId = generateClientId();
        CS2Message subscribeMsg = new CS2Message(
                CS2Message.CMD_SYSTEM,
                CS2Message.ID_SYSTEM,
                new byte[]{
                        (byte) 0x03, // Subscribe to all events
                        (byte) 0x00, // Flags
                        clientId[0], clientId[1], clientId[2], clientId[3],
                        clientId[4], clientId[5], clientId[6], clientId[7]
                }
        );
        sendMessage(subscribeMsg);
        System.out.println("Subscription request sent");
    }

    private byte[] generateClientId() {
        byte[] id = new byte[8];
        new Random().nextBytes(id);
        return id;
    }

    @Override
    public void shutdown() {
        running = false;
        scheduler.shutdown();
        try {
            if (tcpSocket != null) tcpSocket.close();
            System.out.println("Connection closed");
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    private void scheduleHeartbeats() {
        scheduler.scheduleAtFixedRate(() -> {
            CS2Message heartbeat = new CS2Message(
                    CS2Message.CAN_CMD_PING,
                    CS2Message.CAN_ID_PING,
                    new byte[]{0x00, 0x00, 0x00, 0x00}
            );
            sendMessage(heartbeat);
        }, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public String getIP() {
        return this.initIP;
    }
}