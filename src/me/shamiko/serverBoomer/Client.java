package me.shamiko.serverBoomer;

import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Client {
    public String username;
    byte[] handshakePacket;
    byte[] loginPacket;
    String host;
    int port, version;
    public Client(String host, int port, int version) {
        this.username = usingRandom(6);
        this.host = host;
        this.port = port;
        this.version = version;
        new Thread(() -> {
            try {
                setHandshakeAndLogin();
                Socket s = new Socket(host, port);
                s.setSoTimeout(0);
                s.getOutputStream().write(handshakePacket);
                s.getOutputStream().write(loginPacket);
                s.getOutputStream().flush();
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    private void setHandshakeAndLogin() throws UnsupportedEncodingException {
        handshakePacket = encodeHandshakePacket();
        loginPacket = encodeLoginPacket();
    }

    private byte[] encodeVarint(int value) {
        List<Byte> bytes = new ArrayList<>();
        while (true) {
            byte temp = (byte) (value & 0x7F);
            value >>>= 7;
            if (value != 0) {
                temp |= 0x80;
            }
            bytes.add(temp);
            if (value == 0) {
                break;
            }
        }
        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            result[i] = bytes.get(i);
        }
        return result;
    }

    private byte[] encodeString(String value) throws UnsupportedEncodingException {
        byte[] data = value.getBytes("UTF-8");
        byte[] length = encodeVarint(data.length);
        ByteBuffer buffer = ByteBuffer.allocate(length.length + data.length);
        buffer.put(length).put(data);
        return buffer.array();
    }

    private byte[] encodeHandshakePacket() throws UnsupportedEncodingException {
        int packetId = 0;
        int protocolVersion = version;
        String serverAddress = host;
        int serverPort = port;
        int nextState = 2;

        byte[] packetIdBytes = encodeVarint(packetId);
        byte[] protocolVersionBytes = encodeVarint(protocolVersion);
        byte[] serverAddressBytes = encodeString(serverAddress);
        byte[] serverPortBytes = ByteBuffer.allocate(2).putShort((short) serverPort).array();
        byte[] nextStateBytes = encodeVarint(nextState);

        byte[] packetData = ByteBuffer.allocate(
                packetIdBytes.length + protocolVersionBytes.length + serverAddressBytes.length +
                        serverPortBytes.length + nextStateBytes.length
        ).put(packetIdBytes).put(protocolVersionBytes).put(serverAddressBytes).put(serverPortBytes).put(nextStateBytes).array();

        byte[] packetLength = encodeVarint(packetData.length);
        return ByteBuffer.allocate(packetLength.length + packetData.length).put(packetLength).put(packetData).array();
    }

    private byte[] encodeLoginPacket() throws UnsupportedEncodingException {
        int packetId = 0;

        byte[] packetIdBytes = encodeVarint(packetId);
        byte[] usernameBytes = encodeString(username);

        byte[] packetData = ByteBuffer.allocate(packetIdBytes.length + usernameBytes.length).put(packetIdBytes).put(usernameBytes).array();
        byte[] packetLength = encodeVarint(packetData.length);
        return ByteBuffer.allocate(packetLength.length + packetData.length).put(packetLength).put(packetData).array();
    }
    public String usingRandom(int length) {
        String alphabetsInUpperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String alphabetsInLowerCase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String allCharacters = alphabetsInLowerCase + alphabetsInUpperCase + numbers;
        StringBuffer randomString = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int randomIndex = new Random().nextInt(allCharacters.length());
            randomString.append(allCharacters.charAt(randomIndex));
        }
        return randomString.toString();
    }
}
