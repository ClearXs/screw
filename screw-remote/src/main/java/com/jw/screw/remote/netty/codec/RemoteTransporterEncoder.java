package com.jw.screw.remote.netty.codec;

import com.jw.screw.common.serialization.SerializerHolders;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.modle.RemoteTransporter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RemoteTransporterEncoder extends MessageToByteEncoder<RemoteTransporter> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RemoteTransporter msg, ByteBuf out) throws Exception {
        // 对象序列化
        byte[] bytes = SerializerHolders.serializer().serialization(msg);
        msg.setBytes(bytes);
        // 发送请求头
        out.writeInt(Protocol.MAGIC)
                .writeByte(msg.getTransporterType())
                .writeByte(msg.getCode())
                .writeLong(msg.getUnique())
                .writeInt(msg.length())
                .writeBytes(bytes);
    }
}
