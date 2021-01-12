/*
 *  Copyright 2009 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.GeneratedKey;

/**
 * 
 * @author Jeff Butler
 * 
 */
public class InsertSelectiveElementGenerator extends
        AbstractXmlElementGenerator {

    public InsertSelectiveElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("insert"); //$NON-NLS-1$

        answer.addAttribute(new Attribute(
                "id", introspectedTable.getInsertSelectiveStatementId())); //$NON-NLS-1$

        FullyQualifiedJavaType parameterType = introspectedTable.getRules()
                .calculateAllFieldsClass();

        answer.addAttribute(new Attribute("parameterType", //$NON-NLS-1$
                parameterType.getFullyQualifiedName()));

        context.getCommentGenerator().addComment(answer);

        GeneratedKey gk = introspectedTable.getGeneratedKey();
        if (gk != null) {
            IntrospectedColumn introspectedColumn = introspectedTable
                .getColumn(gk.getColumn());
            // if the column is null, then it's a configuration error. The
            // warning has already been reported
            if (introspectedColumn != null) {
                if (gk.isJdbcStandard()) {
                    answer.addAttribute(new Attribute("useGeneratedKeys", "true")); //$NON-NLS-1$ //$NON-NLS-2$
                    answer.addAttribute(new Attribute("keyProperty", introspectedColumn.getJavaProperty())); //$NON-NLS-1$
                } else {
                    answer.addElement(getSelectKey(introspectedColumn, gk));
                }
            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append("insert into "); //$NON-NLS-1$
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        XmlElement insertTrimElement = new XmlElement("trim"); //$NON-NLS-1$
        insertTrimElement.addAttribute(new Attribute("prefix", "(")); //$NON-NLS-1$ //$NON-NLS-2$
        insertTrimElement.addAttribute(new Attribute("suffix", ")")); //$NON-NLS-1$ //$NON-NLS-2$
        insertTrimElement.addAttribute(new Attribute("suffixOverrides", ",")); //$NON-NLS-1$ //$NON-NLS-2$
        answer.addElement(insertTrimElement);

        XmlElement valuesTrimElement = new XmlElement("trim"); //$NON-NLS-1$
        valuesTrimElement.addAttribute(new Attribute("prefix", "values (")); //$NON-NLS-1$ //$NON-NLS-2$
        valuesTrimElement.addAttribute(new Attribute("suffix", ")")); //$NON-NLS-1$ //$NON-NLS-2$
        valuesTrimElement.addAttribute(new Attribute("suffixOverrides", ",")); //$NON-NLS-1$ //$NON-NLS-2$
        answer.addElement(valuesTrimElement);

        for (IntrospectedColumn introspectedColumn : introspectedTable
                .getAllColumns()) {
            if (introspectedColumn.isIdentity()) {
                // cannot set values on identity fields
                continue;
            }

            if (introspectedColumn.isSequenceColumn()
                    || introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
                // if it is a sequence column, it is not optional
                // This is required for MyBatis3 because MyBatis3 parses
                // and calculates the SQL before executing the selectKey
                
                // if it is primitive, we cannot do a null check
                sb.setLength(0);
                sb.append(MyBatis3FormattingUtilities
                    .getEscapedColumnName(introspectedColumn));
                sb.append(',');
                insertTrimElement.addElement(new TextElement(sb.toString()));

                sb.setLength(0);
                sb.append(MyBatis3FormattingUtilities
                    .getParameterClause(introspectedColumn));
                sb.append(',');
                valuesTrimElement.addElement(new TextElement(sb.toString()));

                continue;
            }            
            
            String colName = MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn);
            
            // Donot Add Fields Related to Integration into VO
            if("ORIGIN_BR_I".equalsIgnoreCase(colName) 
               || "ORIGIN_BR_U".equalsIgnoreCase(colName)
               || "ORIGIN_BR_D".equalsIgnoreCase(colName))
            {
        	continue;
            }
         // Denisk for DATE_UPDATED Field
            if("DATE_UPDATED".equalsIgnoreCase(colName))
            {
        	sb.setLength(0);
                sb.append(colName);
                sb.append(',');
                
                insertTrimElement.addElement(new TextElement(sb.toString()));
        	XmlElement chooseElement = new XmlElement("choose"); //$NON-NLS-1$
        	valuesTrimElement.addElement(chooseElement);
                
                XmlElement whenElement = new XmlElement("when");
                sb.setLength(0);
                sb.append("isSybase == 1"); //$NON-NLS-1$ 
                whenElement.addAttribute(new Attribute("test", sb.toString())); //$NON-NLS-1$
                chooseElement.addElement(whenElement);
                
                sb.setLength(0);
                sb.append(" GETDATE()"); //$NON-NLS-1$
                sb.append(',');
                whenElement.addElement(new TextElement(sb.toString())); 
                
                //otherwise
                XmlElement otherwiseElement = new XmlElement("otherwise");
                chooseElement.addElement(otherwiseElement);
                
                
                sb.setLength(0);
                sb.append(" SYSDATE"); 
                sb.append(',');
                //$NON-NLS-1$
                otherwiseElement.addElement(new TextElement(sb.toString()));
                
                
            }
            else
            {
                XmlElement insertNotNullElement = new XmlElement("if"); //$NON-NLS-1$
                sb.setLength(0);
                sb.append(introspectedColumn.getJavaProperty());
                sb.append(" != null"); //$NON-NLS-1$
                
//                System.out.println(" JAva Property ="+introspectedColumn.getJavaProperty()+ " JdbcType="+introspectedColumn.getJdbcTypeName());
                /**
              	 * Ghenoie New: 22-05-2012: adding the test of Numeric fields if different to emptyBigDecimalValue to set it to the parameter value
              	 */
                  if (introspectedColumn.getJdbcTypeName().equals("NUMERIC"))
                  {
                            sb.append(" and ").append(introspectedColumn.getJavaProperty()).append( " != emptyBigDecimalValue"); //$NON-NLS-1$
                  }
                  else // in case of Varchar need to verify if value empty to set itas Null in DB
                   if(introspectedColumn.getJdbcTypeName().equals("VARCHAR") || introspectedColumn.getJdbcTypeName().equals("CHAR") 
                	   || introspectedColumn.getJdbcTypeName().equals("NVARCHAR"))
                   {
                       sb.append(" and !&quot;&quot;.equals(").append(introspectedColumn.getJavaProperty()).append(")"); 
                   }
                
                insertNotNullElement.addAttribute(new Attribute(
                        "test", sb.toString())); //$NON-NLS-1$
                
                sb.setLength(0);
                sb.append(MyBatis3FormattingUtilities
                        .getEscapedColumnName(introspectedColumn));
                sb.append(',');
                insertNotNullElement.addElement(new TextElement(sb.toString()));
                insertTrimElement.addElement(insertNotNullElement);
    
                XmlElement valuesNotNullElement = new XmlElement("if"); //$NON-NLS-1$
                sb.setLength(0);
                sb.append(introspectedColumn.getJavaProperty());
                sb.append(" != null");
                /**
              	 * Ghenoie New: 22-05-2012: adding the test of Numeric fields if different to emptyBigDecimalValue to set it to the parameter value
              	 */
                  if (introspectedColumn.getJdbcTypeName().equals("NUMERIC"))
                  {
                            sb.append(" and ").append(introspectedColumn.getJavaProperty()).append( " != emptyBigDecimalValue"); //$NON-NLS-1$
                  } 
                  else // in case of Varchar need to verify if value empty to set itas Null in DB
                      if(introspectedColumn.getJdbcTypeName().equals("VARCHAR") || introspectedColumn.getJdbcTypeName().equals("CHAR")
                	      || introspectedColumn.getJdbcTypeName().equals("NVARCHAR"))
                      {
                          sb.append(" and !&quot;&quot;.equals(").append(introspectedColumn.getJavaProperty()).append(")"); 
                      }
                 
                valuesNotNullElement.addAttribute(new Attribute(
                        "test", sb.toString())); //$NON-NLS-1$
    
                sb.setLength(0);
                sb.append(MyBatis3FormattingUtilities
                        .getParameterClause(introspectedColumn));
                sb.append(',');
                valuesNotNullElement.addElement(new TextElement(sb.toString()));
                valuesTrimElement.addElement(valuesNotNullElement);
                
            }
        }

        if (context.getPlugins().sqlMapInsertSelectiveElementGenerated(
                answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
