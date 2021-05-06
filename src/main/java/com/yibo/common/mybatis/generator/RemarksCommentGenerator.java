package com.yibo.common.mybatis.generator;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.internal.DefaultCommentGenerator;
import org.mybatis.generator.internal.util.StringUtility;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * RemarksCommentGenerator
 *
 * @author 莫问
 * @date 2019-08-01
 */
public class RemarksCommentGenerator extends DefaultCommentGenerator {

    public static final String currentDateStr = (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());

    /**
     * @author 作者
     */
    public static final String AUTHOR = "莫问";

    /**
     * mybatis的Mapper.xml文件里面的注释
     *
     * @param xmlElement
     */
    @Override
    public void addComment(XmlElement xmlElement) {

    }

    @Override
    public void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addJavaDocLine("/**");

        String remarks = introspectedTable.getRemarks();
        if (StringUtility.stringHasValue(remarks)) {
            String[] remarkLines = remarks.split(System.getProperty("line.separator"));
            for (String remarkLine : remarkLines) {
                topLevelClass.addJavaDocLine(" * 数据层:" + remarkLine);
            }
        }

        topLevelClass.addJavaDocLine(" * ");
        StringBuilder sb = new StringBuilder();
        sb.append(" * ");
        sb.append("@author ");
        sb.append(AUTHOR);
        topLevelClass.addJavaDocLine(sb.toString());
        String sbb = " * " + "@date " + currentDateStr;
        topLevelClass.addJavaDocLine(sbb);
        topLevelClass.addJavaDocLine(" */");
    }

    @Override
    public void addGeneralMethodComment(Method method, IntrospectedTable introspectedTable) {
        method.addJavaDocLine("/**");

        String remarks = introspectedTable.getRemarks();

        if ("insert".equals(method.getName())) {
            method.addJavaDocLine(" * " + "新增数据");
            method.addJavaDocLine(" * ");
            method.addJavaDocLine(" * " + "@param record " + remarks + "实体");
            method.addJavaDocLine(" * " + "@return 影响行数");
        }
        if ("insertSelective".equals(method.getName())) {
            method.addJavaDocLine(" * " + "根据条件新增数据");
            method.addJavaDocLine(" * ");
            method.addJavaDocLine(" * " + "@param record " + remarks + "实体");
            method.addJavaDocLine(" * " + "@return 影响行数");
        }
        if ("selectByPrimaryKey".equals(method.getName())) {
            method.addJavaDocLine(" * " + "根据主键ID查询");
            method.addJavaDocLine(" * ");
            method.addJavaDocLine(" * " + "@param id 主键");
            method.addJavaDocLine(" * " + "@return 查询结果");
        }
        if ("updateByPrimaryKeySelective".equals(method.getName())) {
            method.addJavaDocLine(" * " + "根据主键有选择的更新数据");
            method.addJavaDocLine(" * ");
            method.addJavaDocLine(" * " + "@param record " + remarks + "实体");
            method.addJavaDocLine(" * " + "@return 影响行数");
        }

        method.addJavaDocLine(" */");
    }

    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable,
        IntrospectedColumn introspectedColumn) {
        field.addJavaDocLine("/**");


        String remarks = introspectedColumn.getRemarks();
        if (StringUtility.stringHasValue(remarks)) {
            String[] remarkLines = remarks.split(System.getProperty("line.separator"));
            for (String remarkLine : remarkLines) {
                field.addJavaDocLine(" * " + remarkLine);
            }
        }

        field.addJavaDocLine(" *");
        field.addJavaDocLine(" */");
    }

    @Override
    protected String getDateString() {
        return DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
    }
}
