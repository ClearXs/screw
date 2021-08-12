package com.jw.screw.remote.netty.codec;

import com.alibaba.fastjson.JSON;
import com.jw.screw.common.serialization.SerializerHolders;
import com.jw.screw.logging.core.constant.LogSource;
import com.jw.screw.logging.core.model.Message;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.storage.Executor;
import com.jw.screw.storage.ExecutorHousekeeper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RemoteTransporterEncoder extends MessageToByteEncoder<RemoteTransporter> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RemoteTransporter msg, ByteBuf out) throws Exception {
        Message message = new Message();
        message.setContent(JSON.toJSONString(msg));
        message.setSource(LogSource.RPC);
        message.setType(Protocol.Code.transfer(msg.getCode()));
        Executor executor = ExecutorHousekeeper.getExecutor();
        if (executor != null) {
            executor.record(Message.class, message);
        }
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
