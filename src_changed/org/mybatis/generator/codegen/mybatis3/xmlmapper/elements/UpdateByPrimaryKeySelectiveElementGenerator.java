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
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

/**
 * 
 * @author Jeff Butler
 * 
 */
public class UpdateByPrimaryKeySelectiveElementGenerator extends
        AbstractXmlElementGenerator {

    public UpdateByPrimaryKeySelectiveElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
    	
    	/**
    	 * Ghenoie New - 08-05-2012 : Creating a new <sql> for update statement
    	 */
        XmlElement answer = new XmlElement("sql"); 
        boolean isDateUpdatedExist = false;
 
        answer.addAttribute(new Attribute("id", "sql_".concat(introspectedTable.getUpdateByPrimaryKeySelectiveStatementId()))); //$NON-NLS-1$

        context.getCommentGenerator().addComment(answer);

        StringBuilder sb = new StringBuilder();

        sb.append("update "); //$NON-NLS-1$
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        XmlElement dynamicElement = new XmlElement("set"); //$NON-NLS-1$
        answer.addElement(dynamicElement);
        
        for (IntrospectedColumn introspectedColumn : introspectedTable.getNonPrimaryKeyColumns()) {

            
            String colName = MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn);
            
            // Donot Add Fields Related to Integration into VO
            if("ORIGIN_BR_I".equalsIgnoreCase(colName) 
               || "ORIGIN_BR_U".equalsIgnoreCase(colName)
               || "ORIGIN_BR_D".equalsIgnoreCase(colName))
            {
        	continue;
            }
            
            XmlElement isNotNullElement = new XmlElement("if"); //$NON-NLS-1$
           
            
             if(!"DATE_UPDATED".equalsIgnoreCase(colName))
              {
                 
        	  sb.setLength(0);
                  sb.append(introspectedColumn.getJavaProperty());
                  sb.append(" != null"); //$NON-NLS-1$ 
                  isNotNullElement.addAttribute(new Attribute("test", sb.toString())); //$NON-NLS-1$
                  dynamicElement.addElement(isNotNullElement);
              }
             else
             {
        	 isDateUpdatedExist = true;
             }
          	 /**
          	 * Ghenoie New: 09-05-2012: Testing Numeric fields if equal to emptyBigDecimalValue set it to null otherwise set it to the parameter value
          	 */
             // in case of Varchar also need to verify if the VAlue is Empty String then need to update to NULL
              if (introspectedColumn.getJdbcTypeName().equals("NUMERIC") || introspectedColumn.getJdbcTypeName().equals("VARCHAR") 
        	      || introspectedColumn.getJdbcTypeName().equals("CHAR")
        	      || introspectedColumn.getJdbcTypeName().equals("NVARCHAR"))
              {
            	  
                 XmlElement chooseElement = new XmlElement("choose"); //$NON-NLS-1$
                 isNotNullElement.addElement(chooseElement);
                 
                 XmlElement whenElement = new XmlElement("when");
                 sb.setLength(0);
                 sb.append(introspectedColumn.getJavaProperty());
      
                 if (introspectedColumn.getJdbcTypeName().equals("NUMERIC"))
                 {    
                 sb.append(" == emptyBigDecimalValue"); //$NON-NLS-1$ 
                 }
                 else
                 {
                     sb.append(".equals(&quot;&quot;)");
                 }
                 whenElement.addAttribute(new Attribute("test", sb.toString())); //$NON-NLS-1$
                 chooseElement.addElement(whenElement);

                 sb.setLength(0);
                 sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
                 sb.append(" = NULL "); //$NON-NLS-1$
                 sb.append(',');
                 whenElement.addElement(new TextElement(sb.toString())); 
                 
                 //otherwise
                 XmlElement otherwiseElement = new XmlElement("otherwise");
                 chooseElement.addElement(otherwiseElement);

                 sb.setLength(0);
                 sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
                 sb.append(" = "); //$NON-NLS-1$
                 sb.append(MyBatis3FormattingUtilities
                         .getParameterClause(introspectedColumn));
                 sb.append(',');
                 otherwiseElement.addElement(new TextElement(sb.toString())); 
                 
               
              }
              else

			/**
			 * Ghenoie New: 09-05-2012: If the field is not Numeric, set it normally
			 */
              {
	              
        	      
        	      
	              sb.setLength(0);
	              sb.append(colName);
	              sb.append(" = "); //$NON-NLS-1$
	              
	            
	              // Denisk for DATE_UPDATED Field
	              if("DATE_UPDATED".equalsIgnoreCase(colName))
	              {
	        	  dynamicElement.addElement(new TextElement(sb.toString()));
	        	  
	        	  XmlElement chooseElement = new XmlElement("choose"); //$NON-NLS-1$
	        	  dynamicElement.addElement(chooseElement);
	                  
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
	                  sb.append(" SYSDATE"); //$NON-NLS-1$
	                  sb.append(',');
	                  otherwiseElement.addElement(new TextElement(sb.toString())); 
	        	
	              }
	              else
	              {
	        	  sb.append(MyBatis3FormattingUtilities
		                      .getParameterClause(introspectedColumn));
	        	  sb.append(',');
	        	  isNotNullElement.addElement(new TextElement(sb.toString()));
	              }
	              
              }
        }
        

        boolean and = false;
        for (IntrospectedColumn introspectedColumn : introspectedTable
                .getPrimaryKeyColumns()) {
            sb.setLength(0);
            if (and) {
                sb.append("  and "); //$NON-NLS-1$
            } else {
                sb.append("where "); //$NON-NLS-1$
                and = true;
            }

            sb.append(MyBatis3FormattingUtilities
                    .getEscapedColumnName(introspectedColumn));
            sb.append(" = "); //$NON-NLS-1$
            sb.append(MyBatis3FormattingUtilities
                    .getParameterClause(introspectedColumn));
            answer.addElement(new TextElement(sb.toString()));
        }

        if (context.getPlugins()
                .sqlMapUpdateByPrimaryKeySelectiveElementGenerated(answer,
                        introspectedTable)) {
            parentElement.addElement(answer);
        }
   

		/**
		 * Ghenoie New: 08-05-2012: Create <update> statement with <include>
		 */
        answer = new XmlElement("update"); 
        answer.addAttribute(new Attribute("id", introspectedTable.getUpdateByPrimaryKeySelectiveStatementId()));
        
        String parameterType;

        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            parameterType = introspectedTable.getRecordWithBLOBsType();
        } else {
            parameterType = introspectedTable.getBaseRecordType();
        }

        answer.addAttribute(new Attribute("parameterType",parameterType));
        
        XmlElement include = new XmlElement("include"); 
        include.addAttribute(new Attribute("refid","sql_".concat(introspectedTable.getUpdateByPrimaryKeySelectiveStatementId())));
        answer.addElement(include);
        
        // Denisk to Add Control on DATE_UPDATED Field upon update
        if(isDateUpdatedExist)
        {
            sb.setLength(0);
            sb.append("DATE_UPDATED");
            sb.append(" != null"); //$NON-NLS-1$ 
            XmlElement isNotNullElement = new XmlElement("if");
            isNotNullElement.addAttribute(new Attribute("test", sb.toString())); //$NON-NLS-1$
            isNotNullElement.addElement(new TextElement("AND DATE_UPDATED = #{DATE_UPDATED}"));
            answer.addElement(isNotNullElement);
        }
        
        if (context.getPlugins().sqlMapUpdateByPrimaryKeySelectiveElementGenerated(answer,introspectedTable)) 
        {
            parentElement.addElement(answer);
        }
        
      // end adding an UPDATE statement with include
    }
}
