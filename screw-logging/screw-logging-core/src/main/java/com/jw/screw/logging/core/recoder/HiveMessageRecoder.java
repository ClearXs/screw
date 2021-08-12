package com.jw.screw.logging.core.recoder;

import com.jw.screw.logging.core.model.Message;
import com.jw.screw.storage.hive.session.HqlSession;
import com.jw.screw.storage.properties.StorageProperties;
import com.jw.screw.storage.recoder.Recoder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Recoder.Callable(name = Recoder.HIVE)
public class HiveMessageRecoder extends FileMessageRecoder {

    private HqlSession hqlSession;

    private final String tableName;

    public HiveMessageRecoder(StorageProperties properties) {
        super(properties);
        this.tableName = properties.getHive().getTableName();
    }

    @Override
    protected void init(Object obj) throws IOException {
        if (obj instanceof HqlSession) {
            this.hqlSession = (HqlSession) obj;
        }
    }

    @Override
    public void record(Message message) throws Exception {
        if (messages.size() == accumulation) {
            File file = printDisk(messages);
            hqlSession.loadDataByTextFile(file.getPath(), tableName);
        } else {
            messages.add(message);
        }    }

    @Override
    public List<Message> getAll() {
        List<Map<String, Object>> query = hqlSession.query(tableName);
        System.out.println(query);
        return null;
    }
}
