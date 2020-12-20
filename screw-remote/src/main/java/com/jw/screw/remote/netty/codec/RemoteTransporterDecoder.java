package com.jw.screw.remote.netty.codec;

import com.jw.screw.common.exception.RemoteException;
import com.jw.screw.common.serialization.SerializerHolders;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.modle.RemoteTransporter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RemoteTransporterDecoder extends ReplayingDecoder<Protocol.State> {

    private static Logger logger = LoggerFactory.getLogger(RemoteTransporterDecoder.class);


    private final static int MAX_BODY_LENGTH = 20 * 1024 * 1024;

    private final Protocol header;

    public RemoteTransporterDecoder() {
        // 初始化时，记录bytebuf状态为模式
        super(Protocol.State.HEADER_MAGIC);
        header = new Protocol();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        switch (state()) {
            case HEADER_MAGIC:
                validateMagic(in.readInt());
                checkpoint(Protocol.State.HEADER_SIGN);
            case HEADER_SIGN:
                header.setSign(in.readByte());
                checkpoint(Protocol.State.HEADER_TYPE);
            case HEADER_TYPE:
                header.setType(in.readByte());
                checkpoint(Protocol.State.HEADER_INVOKER_ID);
            case HEADER_INVOKER_ID:
                header.setInvokeId(in.readLong());
                checkpoint(Protocol.State.HEADER_BODY_LENGTH);
            case HEADER_BODY_LENGTH:
                header.setBodyLength(in.readInt());
                checkpoint(Protocol.State.BODY);
            case BODY:
                byte[] bytes = new byte[checkBodyLength(header.getBodyLength())];
                in.readBytes(bytes);
                RemoteTransporter remoteTransporter = SerializerHolders.serializer().deserialization(bytes, RemoteTransporter.class);
                if (logger.isDebugEnabled()) {
                    logger.debug("decode remote transporter: {}", remoteTransporter);
                }
                if (remoteTransporter.getCode() != Protocol.Code.HEART_BEATS) {
                    out.add(remoteTransporter);
                }
                // 设置都指针断点，使下一次读取数据时能从MAGIC处开始读取。
                checkpoint(Protocol.State.HEADER_MAGIC);
                break;
            default:
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }

    private void validateMagic(int magic) throws RemoteException {
        if (magic != Protocol.MAGIC) {
            throw new RemoteException("magic validate error...");
        }
    }

    private int checkBodyLength(int bodyLength) throws RemoteException {
        if (bodyLength >= MAX_BODY_LENGTH) {
            throw new RemoteException("body bytes to more");
        }
        return bodyLength;
    }

}
