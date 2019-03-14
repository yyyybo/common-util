package com.opc.common.mybatis.generator;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.util.List;

/**
 * 工具: 逆向工程工具
 *
 * @author 莫问
 */
@Slf4j
public class GeneratorUtil {

    private void generator() throws Exception {
        List<String> warnings = Lists.newArrayList();
        //指定 逆向工程配置文件

        File configFile = new File("generatorConfig.xml");
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(configFile);
        DefaultShellCallback callback = new DefaultShellCallback(Boolean.TRUE);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);
    }

    public static void main(String[] args) {
        try {
            new GeneratorUtil().generator();
        } catch (Exception e) {
            log.error("逆向工程生成出现异常...", e);
        }
    }
}
