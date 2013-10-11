package org.activiti.bpmn.converter.util;

import java.text.StringCharacterIterator;
import java.util.*;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.GraphicInfo;
import org.apache.commons.lang.StringUtils;

public class BpmnXMLUtil implements BpmnXMLConstants {

  public static void addXMLLocation(BaseElement element, XMLStreamReader xtr) {
    Location location = xtr.getLocation();
    element.setXmlRowNumber(location.getLineNumber());
    element.setXmlColumnNumber(location.getColumnNumber());
  }
  
  public static void addXMLLocation(GraphicInfo graphicInfo, XMLStreamReader xtr) {
    Location location = xtr.getLocation();
    graphicInfo.setXmlRowNumber(location.getLineNumber());
    graphicInfo.setXmlColumnNumber(location.getColumnNumber());
  }
  
  public static void writeDefaultAttribute(String attributeName, String value, XMLStreamWriter xtw) throws Exception {
    if (StringUtils.isNotEmpty(value) && "null".equalsIgnoreCase(value) == false) {
      xtw.writeAttribute(attributeName, value);
    }
  }
  
  public static void writeQualifiedAttribute(String attributeName, String value, XMLStreamWriter xtw) throws Exception {
    if (StringUtils.isNotEmpty(value)) {
      xtw.writeAttribute(ACTIVITI_EXTENSIONS_PREFIX, ACTIVITI_EXTENSIONS_NAMESPACE, attributeName, value);
    }
  }
  
  public static List<String> parseDelimitedList(String s) {
    List<String> result = new ArrayList<String>();
    if (StringUtils.isNotEmpty(s)) {

      StringCharacterIterator iterator = new StringCharacterIterator(s);
      char c = iterator.first();

      StringBuilder strb = new StringBuilder();
      boolean insideExpression = false;

      while (c != StringCharacterIterator.DONE) {
        if (c == '{' || c == '$') {
          insideExpression = true;
        } else if (c == '}') {
          insideExpression = false;
        } else if (c == ',' && !insideExpression) {
          result.add(strb.toString().trim());
          strb.delete(0, strb.length());
        }

        if (c != ',' || (insideExpression)) {
          strb.append(c);
        }

        c = iterator.next();
      }

      if (strb.length() > 0) {
        result.add(strb.toString().trim());
      }

    }
    return result;
  }
  
  public static String convertToDelimitedString(List<String> stringList) {
    StringBuilder resultString = new StringBuilder();
    for (String result : stringList) {
      if (resultString.length() > 0) {
        resultString.append(",");
      }
      resultString.append(result);
    }
    return resultString.toString();
  }

  /**
   * add all attributes from XML to element extensionAttributes.
   *
   * @param xtr
   * @param element
   */
  public static void addCustomAttributes(XMLStreamReader xtr, BaseElement element) {
    for (int i = 0; i < xtr.getAttributeCount(); i++) {
      ExtensionAttribute extensionAttribute = new ExtensionAttribute();
      extensionAttribute.setName(xtr.getAttributeLocalName(i));
      extensionAttribute.setValue(xtr.getAttributeValue(i));
      extensionAttribute.setNamespace(xtr.getAttributeNamespace(i));
      if (StringUtils.isNotEmpty(xtr.getAttributePrefix(i))) {
        extensionAttribute.setNamespacePrefix(xtr.getAttributePrefix(i));
      }
      element.addAttribute(extensionAttribute);
    }
  }

  /**
   * write attributes to xtw (except blacklisted)
   * @param attributes
   * @param xtw
   * @param blackList
   */
  public static void writeAttribute(Collection<List<ExtensionAttribute>> attributes, XMLStreamWriter xtw, List<ExtensionAttribute> blackList) throws XMLStreamException {
    Map<String, String> localNamespaces = new LinkedHashMap<String, String>();
    for (List<ExtensionAttribute> attributeList : attributes) {
      if (attributeList != null && !attributeList.isEmpty()) {
        for (ExtensionAttribute attribute : attributeList) {
          if ( !isBlacklisted(attribute, blackList)) {
            if (attribute.getNamespacePrefix() == null) {
              if (attribute.getNamespace() == null)
                xtw.writeAttribute(attribute.getName(), attribute.getValue());
              else {
                xtw.writeAttribute(attribute.getNamespace(), attribute.getName(), attribute.getValue());
              }
            } else {
              if ( !localNamespaces.containsKey(attribute.getNamespacePrefix())) {
                localNamespaces.put(attribute.getNamespacePrefix(), attribute.getNamespace());
                xtw.writeNamespace(attribute.getNamespacePrefix(), attribute.getNamespace());
              }
              xtw.writeAttribute(attribute.getNamespacePrefix(), attribute.getNamespace(),
                  attribute.getName(), attribute.getValue());
            }
          }
        }
      }
    }
  }

  private static boolean isBlacklisted(ExtensionAttribute attribute, List<ExtensionAttribute> blackList) {
    for (ExtensionAttribute blackAttribute : blackList){
      if (blackAttribute.getName().equals(attribute.getName())) {
        if ( blackAttribute.getNamespace() != null && attribute.getNamespace() != null
            && blackAttribute.getNamespace().equals(attribute.getNamespace()))
          return true;
        if (blackAttribute.getNamespace() == null && attribute.getNamespace() == null)
          return true;
      }
    }
    return false;
  }
}
