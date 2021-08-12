package com.jw.screw.storage.properties;

import com.zzht.patrol.screw.common.constant.StringPool;
import lombok.Data;

@Data
public class FileProperties {

    /**
     * 输出的路径
     */
    private String printPath;

    /**
     * 文件后缀
     */
    private String fileSuffix = StringPool.FileType.TEXT;

    /**
     * 堆积量，当内存缓存中，日志数据达到这个量级，才会进行io输出，创建文件
     * 值越小，io交换越频繁，对系统性能将会造成影响
     * 值过大，消息堆积越多，将造成：
     *  <p>1.消息堆积过多，内存占用过多</p>
     *  <p>2.不是最新的数据</p>
     */
    private int accumulation = 10;

    /**
     * 消息数据分割
     */
    private String split = StringPool.COMMA;
}
