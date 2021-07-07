package com.jw.screw.test.basic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jw.screw.spring.config.ConfigParser;
import org.junit.Test;

import java.util.Properties;

public class JsonTest {

    @Test
    public void testObject() {
        String json = "[{\"defaultCfg\":{\"cityName\":\"苏州\",\"transType\":\"svr\"}}]";
        JSONObject jsonObject = JSONObject.parseObject(json);
        System.out.println(jsonObject);
    }

    @Test
    public void testArray() {
        String json = "[{\"defaultCfg\":{\"cityName\":\"苏州\",\"transType\":\"svr\"}}]";
        JSONArray objects = JSONObject.parseArray(json);
        System.out.println(objects);
    }

    @Test
    public void parseProperties() {
        String s = "{\"address\":{\"defaultCfg\":{\"cityName\":\"苏州\",\"transType\":\"svr\"},\"types\":[{\"name\":\"百度\",\"type\":\"baidu\"},{\"name\":\"本地\",\"type\":\"local\"}],\"mapCfg\":\"extend:mapcfg\",\"polygon\":{\"fillcolor\":\"0,0,0,1\",\"linecolor\":\"0,0,0,1\"},\"polyline\":{\"linecolor\":\"0,0,0,1\",\"width\":2}}}\n";
        Properties properties = ConfigParser.getProperties(s);
        System.out.println(properties);
    }
}
