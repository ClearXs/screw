package com.jw.screw.common.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jw.screw.common.constant.StringPool;
import com.jw.screw.common.util.StringUtils;
import org.apache.commons.collections.list.GrowthList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.util.*;

/**
 * 格式解析器
 * yaml -> properties -> json
 * @author jiangw
 * @date 2021/1/21 17:43
 * @since 1.0
 */
public class FormatParser {

    private static final Logger logger = LoggerFactory.getLogger(FormatParser.class);

    /**
     * 形如spring.datasource=32转为 spring: datasource: 32形式
     * @param properties key=value格式
     * @return yaml格式数据
     */
    public static synchronized String propertiesToYaml(String properties) {
        String[] liens = properties.split(StringPool.NEWLINE);
        Map<String, Object> propertiesMap = new HashMap<>(liens.length);
        for (String lien : liens) {
            String[] keyValue = lien.split(StringPool.EQUALS);
            if (keyValue.length == 1) {
                continue;
            }
            String key = keyValue[0];
            String value = keyValue[1];
            propertiesMap.put(key, value);
        }
        return flattenedMapToYaml(propertiesMap);
    }

    /**
     * 形如spring: datasource: 32转为spring.datasource=32形式
     * @param yaml spring: datasource: 32格式
     * @return properties格式数据 a=1\nb=2格式
     */
    public static synchronized String yamlToProperties(String yaml) {
        Map<String, Object> propertiesMap = yamlToFlattenedMap(yaml);
        StringBuilder properties = new StringBuilder();
        for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
            properties
                    .append(entry.getKey())
                    .append(StringPool.EQUALS)
                    .append(entry.getValue())
                    .append(StringPool.NEWLINE);
        }
        return properties.toString();
    }

    /**
     * 1.把yaml转为多层map
     * 2.直接使用json库把map转为json
     * @param yaml spring: datasource: 32格式
     * @return {"registry":{"address":"localhost","port":8501},"provider":{"address":"localhost"}}格式
     */
    public static synchronized String yamlToJson(String yaml) {
        Map<String, Object> multilayerMap = yamlToMultilayerMap(yaml);
        return JSONObject.toJSON(multilayerMap).toString();
    }

    public static synchronized String jsonToYaml(String json) {
        Map<String, Object> multilayerMap = JSON.parseObject(json, Map.class);
        return multilayerMapToYaml(multilayerMap);
    }

    public static synchronized String propertiesToJson(String properties) {
        return yamlToJson(propertiesToYaml(properties));
    }

    public static synchronized String jsonToProperties(String json) {
        return yamlToProperties(jsonToYaml(json));
    }

    /**
     * yml文件流转成单层map
     * 转Properties 改变了顺序
     */
    public static Map<String, Object> yamlToFlattenedMap(String yamlContent) {
        Yaml yaml = createYaml();
        Map<String, Object> map = new HashMap<>();
        for (Object object : yaml.loadAll(yamlContent)) {
            if (object != null) {
                map = asMap(object);
                map = getFlattenedMap(map);
            }
        }
        return map;
    }

    /**
     * yml文件流转成多次嵌套map
     */
    static Map<String, Object> yamlToMultilayerMap(String yamlContent) {
        Yaml yaml = createYaml();
        Map<String, Object> result = new LinkedHashMap<>();
        for (Object object : yaml.loadAll(yamlContent)) {
            if (object != null) {
                result.putAll(asMap(object));
            }
        }
        return result;
    }

    /**
     * 多次嵌套map转成yml
     */
    static String multilayerMapToYaml(Map<String, Object> map) {
        Yaml yaml = createYaml();
        return yaml.dumpAsMap(map);
    }

    /**
     * 单层map转成yml
     */
    static String flattenedMapToYaml(Map<String, Object> map) {
        Yaml yaml = createYaml();
        return yaml.dumpAsMap(flattenedMapToMultilayerMap(map));
    }

    /**
     * 单层map转换多层map
     */
    static Map<String, Object> flattenedMapToMultilayerMap(Map<String, Object> map) {
        return getMultilayerMap(map);
    }

    static Yaml createYaml() {
        return new Yaml(new Constructor());
    }

    static Map<String, Object> asMap(Object object) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!(object instanceof Map)) {
            result.put("document", object);
            return result;
        }
        Map<Object, Object> map = (Map<Object, Object>) object;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                value = asMap(value);
            }
            Object key = entry.getKey();
            if (key instanceof CharSequence) {
                result.put(key.toString(), value);
            } else {
                result.put(StringPool.LEFT_SQ_BRACKET + key.toString() + StringPool.RIGHT_SQ_BRACKET, value);
            }
        }
        return result;
    }

    static Map<String, Object> getFlattenedMap(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        buildFlattenedMap(result, source, null);
        return result;
    }

    static void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, String path) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            if (!StringUtils.isBlank(path)) {
                if (key.startsWith(StringPool.LEFT_SQ_BRACKET)) {
                    key = path + key;
                } else {
                    key = path + StringPool.DOT + key;
                }
            }
            Object value = entry.getValue();
            if (value instanceof String) {
                result.put(key, value);
            } else if (value instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) value;
                buildFlattenedMap(result, map, key);
            } else if (value instanceof Collection) {
                Collection<Object> collection = (Collection<Object>) value;
                int count = 0;
                for (Object object : collection) {
                    buildFlattenedMap(result, Collections.singletonMap(StringPool.LEFT_SQ_BRACKET + (count++) + StringPool.RIGHT_SQ_BRACKET, object), key);
                }
            } else {
                result.put(key, (value != null ? value.toString() : ""));
            }
        }
    }

    static Map<String, Object> getMultilayerMap(Map<String, Object> source) {
        Map<String, Object> rootResult = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            buildMultilayerMap(rootResult, key,entry.getValue());
        }
        return rootResult;
    }

    static void buildMultilayerMap(Map<String, Object> parent, String path, Object value) {
        String[] keys = StringUtils.split(path,StringPool.DOT);
        String key = keys[0];
        if (key.endsWith(StringPool.RIGHT_SQ_BRACKET)) {
            String listKey = key.substring(0, key.indexOf(StringPool.LEFT_SQ_BRACKET));
            String listPath = path.substring(key.indexOf(StringPool.LEFT_SQ_BRACKET));
            List<Object> child =  buildChildList(parent, listKey);
            buildMultilayerList(child, listPath, value);
        } else {
            if (keys.length == 1) {
                parent.put(key, stringToObj(value.toString()));
            } else {
                String newPath = path.substring(path.indexOf(StringPool.DOT) + 1);
                Map<String, Object> child = buildChildMap(parent, key);;
                buildMultilayerMap(child, newPath, value);
            }
        }
    }

    static void buildMultilayerList(List<Object> parent, String path, Object value) {
        String[] keys = StringUtils.split(path, StringPool.DOT);
        String key = keys[0];
        int index = Integer.parseInt(key.replace(StringPool.LEFT_SQ_BRACKET, "").replace(StringPool.RIGHT_SQ_BRACKET, ""));
        if (keys.length == 1) {
            parent.add(index, stringToObj(value.toString()));
        } else {
            String newPath = path.substring(path.indexOf(StringPool.DOT) + 1);
            Map<String, Object> child = buildChildMap(parent, index);;
            buildMultilayerMap(child, newPath, value);
        }
    }

    static Map<String, Object> buildChildMap(Map<String, Object> parent, String key) {
        if (parent.containsKey(key)) {
            return (Map<String, Object>) parent.get(key);
        } else {
            Map<String, Object> child = new LinkedHashMap<>(16);
            parent.put(key, child);
            return child;
        }
    }

    static Map<String, Object> buildChildMap(List<Object> parent, int index) {
        Map<String, Object> child = null;
        try {
            Object obj = parent.get(index);
            if (null != obj) {
                child = (Map<String, Object>) obj;
            }
        } catch(Exception e) {
            logger.warn("get list error");
        }
        if (null == child) {
            child = new LinkedHashMap<>(16);
            parent.add(index, child);
        }
        return child;
    }

    static List<Object> buildChildList(Map<String, Object> parent, String key) {
        if (parent.containsKey(key)) {
            return (List<Object>) parent.get(key);
        } else {
            List<Object> child = new GrowthList(16);
            parent.put(key, child);
            return child;
        }
    }

    static Object stringToObj(String obj) {
        Object result;
        if (StringPool.TRUE.equals(obj) || StringPool.FALSE.equals(obj)) {
            result = Boolean.valueOf(obj);
        } else if (isBigDecimal(obj)) {
            if (!obj.contains(StringPool.DOT)) {
                result = Long.valueOf(obj);
            } else {
                result = Double.valueOf(obj);
            }
        } else {
            result = obj;
        }
        return result;
    }

    static boolean isBigDecimal(String str) {
        if (str == null || str.trim().length() == 0) {
            return false;
        }
        char[] chars = str.toCharArray();
        int sz = chars.length;
        int i = (chars[0] == '-') ? 1 : 0;
        if (i == sz) {
            return false;
        }
        //除了负号，第一位不能为'小数点'
        if (chars[i] == '.') {
            return false;
        }
        boolean radixPoint = false;
        for(; i < sz; i++) {
            if(chars[i] == '.') {
                if (radixPoint) {
                    return false;
                }
                radixPoint = true;
            } else if (!(chars[i] >= '0' && chars[i] <= '9')) {
                return false;
            }
        }
        return true;
    }

}
