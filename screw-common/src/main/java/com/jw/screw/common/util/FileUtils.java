package com.jw.screw.common.util;

import com.jw.screw.common.constant.StringPool;
import lombok.Data;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystemException;

/**
 * 文件工具类型
 * @author jiangw
 * @date 2021/4/16 16:10
 * @since 1.0
 */
public class FileUtils {

    private final static int BUFFER_SIZE = 2048 * 2048;

    private final static String CLASSPATH_PREFIX = "classpath:";

    /**
     * 获取类路径
     */
    public static String getClassPath(String path) throws FileNotFoundException {
        path = path.substring(CLASSPATH_PREFIX.length());
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        if (url == null) {
            throw new FileNotFoundException(path);
        }
        return url.getPath();
    }

    /**
     * 非阻塞读取文件
     * @param path 文件路径
     * @return 读取到的文件字节数组
     * @throws IOException
     */
    public static byte[] readFileByNIO(String path) throws IOException {
        // 以下代码借鉴自Spring ResourceUtils.getURL()
        if (path.startsWith(CLASSPATH_PREFIX)) {
            path = getClassPath(path);
        }
        return readFileByNIO(new File(path, ""));
    }

    public static byte[] readFileByNIO(File file) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        FileInputStream inputStream = new FileInputStream(file);
        FileChannel channel = inputStream.getChannel();
        while (channel.read(buffer) != -1) {
        }
        buffer.flip();
        byte[] bytes = new byte[buffer.limit()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = buffer.get();
        }
        inputStream.close();
        return bytes;
    }

    /**
     * 非阻塞写入文件
     * @param fileEntity 文件的实体
     * @throws IOException
     */
    public static File writeFileByNIO(FileEntity fileEntity) throws IOException {
        String path = fileEntity.getPath();
        // 创建这个文件路径目录
        File dir = new File(path);
        if (!dir.exists()) {
            boolean mkdirs = dir.mkdirs();
            if (!mkdirs) {
                throw new FileSystemException("文件夹创建失败" + path);
            }
        }
        File writeFile = new File(path + StringPool.BACK_SLASH + fileEntity.getName());
        FileOutputStream outputStream = new FileOutputStream(writeFile);
        FileChannel channel = outputStream.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(fileEntity.getContent().length);
        buffer.put(fileEntity.getContent());
        buffer.flip();
        while (channel.write(buffer) != 0) {
        }
        channel.force(true);
        channel.close();
        outputStream.close();
        return writeFile;
    }

    @Data
    public static class FileEntity {

        /**
         * 文件名
         */
        private String name;

        /**
         * 文件所在的路径
         */
        private String path;

        /**
         * 文件的内容
         */
        private byte[] content;
    }
}
