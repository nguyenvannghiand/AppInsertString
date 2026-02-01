package org.example

import org.w3c.dom.CDATASection
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class XmlResourceManager {
    fun updateStrings(modulePath: String, folderName: String, translations: Map<String, String>, mode: String): String {
        val resDir = File("$modulePath/src/main/res/$folderName")
        if (!resDir.exists()) resDir.mkdirs()

        val xmlFile = File(resDir, "strings.xml")
        val dbf = DocumentBuilderFactory.newInstance()
        dbf.isIgnoringElementContentWhitespace = false

        val doc = if (xmlFile.exists()) {
            dbf.newDocumentBuilder().parse(xmlFile)
        } else {
            createNewStringsDocument()
        }

        val root = doc.documentElement
        val logs = mutableListOf<String>()

        translations.forEach { (key, value) ->
            val escapedValue = escapeAndroidString(value)
            val existingElement = findElementByKey(root, key)
            val cdata = doc.createCDATASection(escapedValue)

            when (mode) {
                "ADD_ONLY" -> {
                    if (existingElement != null) {
                        logs.add("Key '$key' đã tồn tại trong $folderName")
                    } else {
                        addNewElement(doc, root, key, cdata)
                    }
                }
                "UPDATE_ONLY" -> {
                    if (existingElement == null) {
                        logs.add("Key '$key' không tồn tại trong $folderName (Cần add mới)")
                    } else {
                        replaceWithCleanElement(doc, root, existingElement, key, cdata)
                    }
                }
                else -> { // SYNC mode: Cả add và update
                    if (existingElement != null) {
                        replaceWithCleanElement(doc, root, existingElement, key, cdata)
                    } else {
                        addNewElement(doc, root, key, cdata)
                    }
                }
            }
        }
        saveDocument(doc, xmlFile)
        return logs.joinToString(", ")
    }

    private fun addNewElement(doc: Document, root: Element, key: String, cdata: CDATASection) {
        root.appendChild(doc.createTextNode("\n    "))
        val newString = doc.createElement("string")
        newString.setAttribute("name", key)
        newString.appendChild(cdata)
        root.appendChild(newString)
    }

    private fun replaceWithCleanElement(doc: Document, root: Element, oldEl: Element, key: String, cdata: CDATASection) {
        val newEl = doc.createElement("string")
        newEl.setAttribute("name", key)
        val attrs = oldEl.attributes
        for (i in 0 until attrs.length) {
            val attr = attrs.item(i)
            newEl.setAttribute(attr.nodeName, attr.nodeValue)
        }
        newEl.appendChild(cdata)
        root.replaceChild(newEl, oldEl)
    }

    private fun escapeAndroidString(input: String): String {
        return input.replace("'", "\\'").replace("’", "\\’").replace("\"", "\\\"")
    }

    private fun findElementByKey(root: Element, key: String): Element? {
        val nodes = root.getElementsByTagName("string")
        for (i in 0 until nodes.length) {
            val node = nodes.item(i) as Element
            if (node.getAttribute("name") == key) return node
        }
        return null
    }

    private fun createNewStringsDocument(): Document {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        doc.appendChild(doc.createElement("resources"))
        return doc
    }

    private fun saveDocument(doc: Document, file: File) {
        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "no")
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        transformer.transform(DOMSource(doc), StreamResult(file))
    }
}