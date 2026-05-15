package io.github.some_example_name.net;

import java.nio.ByteBuffer;

final class PlayerStatePacket {
    static final byte VERSION = 2;
    static final int BYTES = 1
        + Integer.BYTES
        + Float.BYTES
        + 1
        + Float.BYTES
        + Float.BYTES
        + Float.BYTES
        + Float.BYTES
        + Integer.BYTES
        + Integer.BYTES
        + Long.BYTES;

    final int playerId;
    final float paddleY;
    final boolean hasAuthoritativeState;
    final float ballX;
    final float ballY;
    final float ballVelX;
    final float ballVelY;
    final int leftScore;
    final int rightScore;
    final long timestamp;

    private PlayerStatePacket(
        int playerId,
        float paddleY,
        boolean hasAuthoritativeState,
        float ballX,
        float ballY,
        float ballVelX,
        float ballVelY,
        int leftScore,
        int rightScore,
        long timestamp
    ) {
        this.playerId = playerId;
        this.paddleY = paddleY;
        this.hasAuthoritativeState = hasAuthoritativeState;
        this.ballX = ballX;
        this.ballY = ballY;
        this.ballVelX = ballVelX;
        this.ballVelY = ballVelY;
        this.leftScore = leftScore;
        this.rightScore = rightScore;
        this.timestamp = timestamp;
    }

    static byte[] serialize(
        int playerId,
        float paddleY,
        boolean hasAuthoritativeState,
        float ballX,
        float ballY,
        float ballVelX,
        float ballVelY,
        int leftScore,
        int rightScore,
        long timestamp
    ) {
        ByteBuffer buffer = ByteBuffer.allocate(BYTES);
        buffer.put(VERSION);
        buffer.putInt(playerId);
        buffer.putFloat(paddleY);
        buffer.put((byte) (hasAuthoritativeState ? 1 : 0));
        buffer.putFloat(ballX);
        buffer.putFloat(ballY);
        buffer.putFloat(ballVelX);
        buffer.putFloat(ballVelY);
        buffer.putInt(leftScore);
        buffer.putInt(rightScore);
        buffer.putLong(timestamp);
        return buffer.array();
    }

    static PlayerStatePacket deserialize(byte[] data, int length) {
        if (length < BYTES) {
            throw new IllegalArgumentException("Pacote incompleto: " + length + " bytes");
        }

        ByteBuffer buffer = ByteBuffer.wrap(data, 0, length);
        byte version = buffer.get();
        if (version != VERSION) {
            throw new IllegalArgumentException("Versao de pacote nao suportada: " + version);
        }

        int playerId = buffer.getInt();
        float paddleY = buffer.getFloat();
        boolean hasAuthoritativeState = buffer.get() == 1;
        float ballX = buffer.getFloat();
        float ballY = buffer.getFloat();
        float ballVelX = buffer.getFloat();
        float ballVelY = buffer.getFloat();
        int leftScore = buffer.getInt();
        int rightScore = buffer.getInt();
        long timestamp = buffer.getLong();
        return new PlayerStatePacket(
            playerId,
            paddleY,
            hasAuthoritativeState,
            ballX,
            ballY,
            ballVelX,
            ballVelY,
            leftScore,
            rightScore,
            timestamp
        );
    }
}
