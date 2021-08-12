package com.jw.screw.logging.core.recoder;

import com.jw.screw.common.constant.StringPool;
import com.jw.screw.common.util.FileUtils;
import com.jw.screw.logging.core.constant.TransferType;
import com.jw.screw.logging.core.model.Message;
import com.jw.screw.storage.QueryFilter;
import com.jw.screw.storage.properties.FileProperties;
import com.jw.screw.storage.properties.StorageProperties;
import com.jw.screw.storage.recoder.AbstractFileRecoder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 文件输出日志文件
 *
 */
public class FileMessageRecoder extends AbstractFileRecoder<Message> {

    /**
     * 堆积量
     */
    protected final int accumulation;

    protected final List<Message> messages = Collections.synchronizedList(new ArrayList<>());

    /**
     * 消息实体
     */
    private final FileUtils.FileEntity fileEntity = new FileUtils.FileEntity();

    private static final ThreadLocal<SimpleDateFormat> THREAD_LOCAL = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss"));

    /**
     * 文件内容分隔符
     */
    private final String contentSplit;

    public FileMessageRecoder(StorageProperties properties) {
        super(properties);
        FileProperties file = properties.getFile();
        String printPath = file.getPrintPath();
        this.fileEntity.setPath(printPath);
        this.accumulation = file.getAccumulation();
        this.contentSplit = file.getSplit();
    }

    @Override
    protected Object getInitConfig() {
        return null;
    }

    @Override
    protected void init(Object obj) throws IOException {

    }

    @Override
    public void record(Message message) throws Exception {
        message.setTransferType(TransferType.IO);
        messages.add(message);
        // 达到accumulation阈值时刷到磁盘上
        if (messages.size() == accumulation) {
            printDisk(messages);
        }
    }

    @Override
    public Message getMessage(String id) {
        return null;
    }

    @Override
    public List<Message> getAll() {
        return null;
    }

    @Override
    public List<Message> query(QueryFilter<Message> queryFilter) {
        return null;
    }

    @Override
    public void shutdownCallback() {
        if (com.jw.screw.common.util.Collections.isNotEmpty(messages)) {
            printDisk(messages);
        }
    }

    /**
     * 日志数据刷到磁盘
     * @return file
     */
    File printDisk(List<Message> messages) {
        StringBuffer print = new StringBuffer();
        messages.forEach(m -> {
            print.append(m.getId())
                    .append(contentSplit)
                    .append(m.getSource())
                    .append(contentSplit)
                    .append(m.getType())
                    .append(contentSplit)
                    .append(m.getContent())
                    .append(StringPool.NEWLINE);
        });
        SimpleDateFormat sdf = THREAD_LOCAL.get();
        fileEntity.setName(sdf.format(new Date()) + StringPool.FileType.TEXT);
        fileEntity.setContent(print.toString().getBytes());
        try {
            return FileUtils.writeFileByNIO(fileEntity);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            messages.clear();
        }
        return null;
    }
}
