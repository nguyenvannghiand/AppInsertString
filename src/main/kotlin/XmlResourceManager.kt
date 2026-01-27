package org.example

import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class XmlResourceManager(private val projectRoot: String) {
    fun updateStrings(folderName: String, translations: Map<String, String>) {
        val resDir = File("$projectRoot/src/main/res/$folderName")
        if (!resDir.exists()) resDir.mkdirs()

        val xmlFile = File(resDir, "strings.xml")
        val doc = if (xmlFile.exists()) {
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile)
        } else {
            createNewStringsDocument()
        }

        val root = doc.documentElement
        translations.forEach { (key, value) ->
            val escapedValue = escapeXml(value)
            val existingElement = findElementByKey(root, key)

            if (existingElement != null) {
                existingElement.textContent = escapedValue
            } else {
                val newString = doc.createElement("string")
                newString.setAttribute("name", key)
                newString.textContent = escapedValue
                root.appendChild(newString)
            }
        }
        saveDocument(doc, xmlFile)
    }

    fun removeStrings(folderName: String, keys: List<String>) {
        val xmlFile = File("$projectRoot/src/main/res/$folderName/strings.xml")
        if (!xmlFile.exists() || keys.isEmpty()) return

        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile)
        val root = doc.documentElement
        val nodes = root.getElementsByTagName("string")

        val toRemove = mutableListOf<Element>()
        for (i in 0 until nodes.length) {
            val node = nodes.item(i) as Element
            if (keys.contains(node.getAttribute("name"))) toRemove.add(node)
        }

        toRemove.forEach { root.removeChild(it) }
        saveDocument(doc, xmlFile)
    }

    private fun findElementByKey(root: Element, key: String): Element? {
        val nodes = root.getElementsByTagName("string")
        for (i in 0 until nodes.length) {
            val node = nodes.item(i) as Element
            if (node.getAttribute("name") == key) return node
        }
        return null
    }

    private fun escapeXml(s: String): String = s.replace("'", "\\'").replace("\"", "\\\"")

    private fun createNewStringsDocument(): Document {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        doc.appendChild(doc.createElement("resources"))
        return doc
    }

    private fun saveDocument(doc: Document, file: File) {
        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
        transformer.transform(DOMSource(doc), StreamResult(file))
    }
}