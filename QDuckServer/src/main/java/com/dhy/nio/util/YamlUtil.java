package com.dhy.nio.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 大忽悠
 * @create 2022/4/28 17:09
 */
@Slf4j
public class YamlUtil {
    private Yaml yaml;
    private Map<String, Map<String, Object>> yamlContent;
    private Map<String, String> resCache;
    private final String ymlFilePath;
    private final String KEY_DELIMITER = "\\.";
    private final String  NULL_KEY="NULL";

    public YamlUtil(String ymlFilePath) {
        this.ymlFilePath = ymlFilePath;
    }


    @SneakyThrows
    public String get(String key) {
        if (resCache != null && resCache.containsKey(key)) {
            return resCache.get(key).equals(NULL_KEY)?null:resCache.get(key);
        }
        //懒加载
        if (yaml == null) {
            //初始化yaml
            initYaml();
        }
        //查询,放入缓存
        return queryAndPutCache(key);
    }

    private void initYaml() throws FileNotFoundException {
        try {
            yaml = new Yaml();
            yamlContent = yaml.load(YamlUtil.class.getClassLoader().getResourceAsStream(ymlFilePath));
        } catch (YAMLException yamlException) {
            //尝试去文件系统中定位yaml文件
            File file = new File(ymlFilePath);
            if (!file.exists()) {
                throw new YAMLException("classPath和文件系统中无法找到名为" + ymlFilePath + "的文件");
            }
            yamlContent = yaml.load(new FileInputStream(file));
        }
    }

    private String queryAndPutCache(String key) {
        String[] keys = key.split(KEY_DELIMITER);
        String value = extractValue(keys, yamlContent, 0);
        if (resCache == null) {
            resCache = new ConcurrentHashMap<>();
        }
        resCache.put(key, value==null?NULL_KEY: value);
        return value;
    }

    private String extractValue(String[] keys, Map<String, Map<String, Object>> yamlContent, int index) {
        if (index == keys.length) {
            return null;
        }
        Object valueMap = yamlContent.get(keys[index]);
        if (valueMap == null) {
            return null;
        }
        if (!(valueMap instanceof Map)) {
            return valueMap instanceof String ? (String) valueMap : valueMap.toString();
        }
        return extractValue(keys, (Map<String, Map<String, Object>>) valueMap, index + 1);
    }
}
