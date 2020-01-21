package com.github.serezhka.jap2server.internal.handler.mirroring;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
class AirPlayHeader {
    private final int payloadSize;
    private final short payloadType;
    private final short payloadOption;

    private int widthSource;
    private int heightSource;
    private int width;
    private int height;

    AirPlayHeader(ByteBuf header) {
        this.payloadSize = (int) header.readUnsignedIntLE();
        this.payloadType = (short) (header.readUnsignedShortLE() & 0xff);
        this.payloadOption = (short) header.readUnsignedShortLE();

        if (payloadType == 1) {
            header.readerIndex(40);
            widthSource = (int) header.readFloatLE();
            heightSource = (int) header.readFloatLE();
            header.readerIndex(56);
            width = (int) header.readFloatLE();
            height = (int) header.readFloatLE();
        }
    }
}
