package com.demo.integration.core.xml;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/5/9 23:06
 */

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class XmlUtil {

    private static final XmlMapper XML_MAPPER =
            new XmlMapper();

    public static <T> T xmlToObj(
            String xml,
            Class<T> clazz) {

        try {

            return XML_MAPPER.readValue(xml, clazz);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    public static String objToXml(Object obj) {

        try {

            return XML_MAPPER.writeValueAsString(obj);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }
}