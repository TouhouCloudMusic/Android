package net.hearnsoft.tcm.infrastructure.api

import net.hearnsoft.tcm.infrastructure.api.test.ApiTestConfig
import net.hearnsoft.tcm.infrastructure.api.test.ApiTestEntry
import net.hearnsoft.tcm.infrastructure.logger.Logger.err
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream
import java.util.Locale
import javax.xml.parsers.SAXParserFactory

class ApiConfigParser {
    fun parseStream(inputStream: InputStream?): ApiTestEntry? {
        val spf = SAXParserFactory.newInstance()
        val handler: ApiConfigHandler
        try {
            val parser = spf.newSAXParser()
            handler = ApiConfigHandler()
            parser.parse(inputStream, handler)
        } catch (e: Exception) {
            err("ApiConfigParser", e.message!!)
            return null
        }
        return ApiTestEntry(handler.apiTestEntry)
    }

    private class ApiConfigHandler : DefaultHandler() {
        private val apis: MutableList<ApiTestConfig> = ArrayList()
        private val builder = StringBuilder()
        private var apiTestConfig: ApiTestConfig? = null

        @Throws(SAXException::class)
        override fun startElement(
            uri: String,
            localName: String,
            qName: String,
            attributes: Attributes
        ) {
            if (qName.equals("api", ignoreCase = true)) {
                apiTestConfig = ApiTestConfig()
            }
            builder.setLength(0)
        }

        @Throws(SAXException::class)
        override fun characters(ch: CharArray, start: Int, length: Int) {
            builder.append(ch, start, length)
        }

        @Throws(SAXException::class)
        override fun endElement(uri: String, localName: String, qName: String) {
            if (apiTestConfig == null) return

            when (qName.lowercase(Locale.getDefault())) {
                "name" -> apiTestConfig!!.apiName = builder.toString()
                "fragment" -> apiTestConfig!!.testFragment = builder.toString()
                "api" -> {
                    apis.add(apiTestConfig!!)
                    apiTestConfig = null
                }
            }
        }

        val apiTestEntry: List<ApiTestConfig>
            get() = ArrayList(apis)
    }
}
