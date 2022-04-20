package com.gitegg.platform.code.generator.engine;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.config.ConstVal;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

/**
 * Freemarker 自定义输出自定义模板文件
 * 重写父类方法是为了解决数字加逗号的问题，是配置文件可配置
 *
 * @author GitEgg
 * @since 2021-10-12
 */
public class GitEggFreemarkerTemplateEngine extends FreemarkerTemplateEngine {
    
    private Configuration configuration;
    
    @Override
    public @NotNull GitEggFreemarkerTemplateEngine init(@NotNull ConfigBuilder configBuilder) {
        configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        configuration.setDefaultEncoding(ConstVal.UTF8);
        configuration.setNumberFormat(StringPool.ZERO);
        configuration.setClassForTemplateLoading(FreemarkerTemplateEngine.class, StringPool.SLASH);
        return this;
    }
    
    
    @Override
    public void writer(@NotNull Map<String, Object> objectMap, @NotNull String templatePath, @NotNull File outputFile) throws Exception {
        Template template = configuration.getTemplate(templatePath);
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            template.process(objectMap, new OutputStreamWriter(fileOutputStream, ConstVal.UTF8));
        }
    }

    /**
     * 自定义输出自定义模板文件
     *
     * @param customFile 自定义配置模板文件信息
     * @param tableInfo  表信息
     * @param objectMap  渲染数据
     * @since 3.5.1
     */
    @Override
    protected void outputCustomFile( Map<String, String> customFile, TableInfo tableInfo, Map<String, Object> objectMap) {
        Map<String, String> customFilePath = (Map<String, String>)objectMap.get("customFilePathMap");
        customFile.forEach((key, value) -> {
            String otherPath = customFilePath.get(key);
            String fileName = String.format((otherPath + File.separator + "%s"), key);
            outputFile(new File(fileName), objectMap, value);
        });
    }
}
