package com.dhy.nio.config;

import com.dhy.nio.util.YamlUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static com.dhy.nio.constants.ConfigConstants.*;

/**
 * <p>
 *     QDuck的配置信息
 * </p>
 * @author 大忽悠
 * @create 2022/6/1 11:45
 */
@Data
@Slf4j
public class QDuckConfig {
    private final YamlUtil yamlUtil;

    /**
     * 先尝试从环境变量中读取配置文件路径,如果环境变量中不存在,则使用默认配置文件路径
     */
    public QDuckConfig() {
        String configPath = System.getProperty(DEFAULT_CONFIG_PROPERTY);
        yamlUtil = new YamlUtil(configPath == null ? DEFAULT_CONFIG_YML_PATH : configPath);
    }

    public String getValue(String key){
        return yamlUtil.get(key);
    }
}
