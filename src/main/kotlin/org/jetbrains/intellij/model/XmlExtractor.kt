package org.jetbrains.intellij.model

import com.jetbrains.plugin.structure.base.utils.inputStream
import com.jetbrains.plugin.structure.base.utils.outputStream
import com.jetbrains.plugin.structure.intellij.utils.JDOMUtil
import org.jetbrains.intellij.transformXml
import org.jetbrains.intellij.warn
import java.io.InputStream
import java.nio.file.Path
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException

class XmlExtractor<T>(private val context: String? = null) {

    private val jaxbContext by lazy {
        JAXBContext.newInstance("org.jetbrains.intellij.model", ObjectFactory::class.java.classLoader)
    }

    @Throws(JAXBException::class)
    fun unmarshal(path: Path) = unmarshal(path.inputStream())

    @Suppress("UNCHECKED_CAST")
    @Throws(JAXBException::class)
    fun unmarshal(inputStream: InputStream) = jaxbContext.createUnmarshaller().unmarshal(inputStream) as T

    @Throws(JAXBException::class)
    fun marshal(bean: T, path: Path) {
        jaxbContext.createMarshaller().marshal(bean, path.outputStream())
        val document = JDOMUtil.loadDocument(path.inputStream())
        transformXml(document, path)
    }

    fun fetch(path: Path) = runCatching {
        unmarshal(path)
    }.onFailure {
        warn(context, "Failed to get products releases list: ${it.message}", it)
    }.getOrNull()
}
