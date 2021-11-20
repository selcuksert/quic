package com.corp.concepts.quic.common;

import net.luminis.quic.log.BaseLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;

public class QuicLogger extends BaseLogger {

    private static final Logger logger = LogManager.getLogger(QuicLogger.class);

    @Override
    protected void log(String message) {
        logger.info(message);
    }

    @Override
    protected void log(String message, Throwable ex) {
        logger.error(message, ex);
    }

    @Override
    protected void logWithHexDump(String message, byte[] data, int length) {
        logger.info(message, byteToHexBlock(data, length));
    }

    @Override
    protected void logWithHexDump(String message, ByteBuffer data, int offset, int length) {
        logger.info(message, byteToHexBlock(data, offset, length));
    }
}
