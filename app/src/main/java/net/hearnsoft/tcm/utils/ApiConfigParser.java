package net.hearnsoft.tcm.utils;

import net.hearnsoft.tcm.infrastructure.api.test.ApiTestConfig;
import net.hearnsoft.tcm.infrastructure.api.test.ApiTestEntry;
import net.hearnsoft.tcm.infrastructure.logger.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class ApiConfigParser {

    public ApiTestEntry parseApiConfigs(InputStream inputStream) {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        ApiConfigHandler handler;
        try {
            SAXParser parser = spf.newSAXParser();
            handler = new ApiConfigHandler();
            parser.parse(inputStream, handler);
        } catch (Exception e) {
            Logger.err("ApiConfigParser", e.getMessage());
            return null;
        }
        return new ApiTestEntry(handler.getApiTestEntry());
    }

    private static class ApiConfigHandler extends DefaultHandler {
        private final List<ApiTestConfig> apis = new ArrayList<>();
        private final StringBuilder builder = new StringBuilder();
        private ApiTestConfig apiTestConfig;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equalsIgnoreCase("api")) {
                apiTestConfig = new ApiTestConfig();
            }
            builder.setLength(0);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            builder.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (apiTestConfig == null) return;

            switch (qName.toLowerCase()) {
                case "name":
                    apiTestConfig.setApiName(builder.toString());
                    break;
                case "fragment":
                    apiTestConfig.setTestFragment(builder.toString());
                    break;
                case "api":
                    apis.add(apiTestConfig);
                    apiTestConfig = null;
                    break;
            }
        }

        public List<ApiTestConfig> getApiTestEntry() {
            return new ArrayList<>(apis);
        }
    }
}
