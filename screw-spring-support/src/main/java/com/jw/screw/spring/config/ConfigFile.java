package com.jw.screw.spring.config;

import com.jw.screw.common.parser.FormatParser;
import com.jw.screw.common.util.FileUtils;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 配置文件对于的实体对象，提供一些关于配置文件的操作
 * @author jiangw
 * @date 2021/4/16 16:13
 * @since 1.0
 */
public class ConfigFile {

    private static ConfigFile configFile;

    private static String rootPath;

    private static String fileName = "offline.yaml";

    private static final ReentrantLock LOCK = new ReentrantLock();

    static {
        try {
            rootPath = ResourceUtils.getURL("classpath:").getPath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private ConfigFile() {

    }

    public String readConfig() throws IOException {
        byte[] bytes = FileUtils.readFileByNIO(rootPath + fileName);
        String configYaml = new String(bytes);
        return FormatParser.yamlToJson(configYaml);
    }

    public void writeConfig(String config) throws IOException {
        LOCK.lock();
        try {
            String configYaml = FormatParser.jsonToYaml(config);
            FileUtils.FileEntity fileEntity = new FileUtils.FileEntity();
            fileEntity.setPath(rootPath);
            fileEntity.setName(fileName);
            fileEntity.setContent(configYaml.getBytes());
            FileUtils.writeFileByNIO(fileEntity);
        } finally {
            LOCK.unlock();
        }
    }

    public static ConfigFile newInstance() {
        if (configFile == null) {
            synchronized (ConfigFile.class) {
                if (configFile == null) {
                    configFile = new ConfigFile();
                }
            }
        }
        return configFile;
    }
}
