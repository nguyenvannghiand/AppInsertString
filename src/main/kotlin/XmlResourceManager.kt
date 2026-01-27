package org.example

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
    fun updateStrings(modulePath: String, folderName: String, translations: Map<String, String>) {
        val resDir = File("$modulePath/src/main/res/$folderName")
        if (!resDir.exists()) resDir.mkdirs()

        val xmlFile = File(resDir, "strings.xml")
        val dbf = DocumentBuilderFactory.newInstance()

        // QUAN TRỌNG: Không bỏ qua khoảng trắng để giữ nguyên các phân cụm cũ
        dbf.isIgnoringElementContentWhitespace = false

        val doc = if (xmlFile.exists()) {
            dbf.newDocumentBuilder().parse(xmlFile)
        } else {
            createNewStringsDocument()
        }

        val root = doc.documentElement

        translations.forEach { (key, value) ->
            val escapedValue = escapeAndroidString(value)
            val existingElement = findElementByKey(root, key)

            // Bước 1: Tạo CDATA section trước
            val cdata = doc.createCDATASection(escapedValue)

            if (existingElement != null) {
                // GIẢI PHÁP MỚI: Tạo một thẻ string mới hoàn toàn để thay thế thẻ cũ bị lỗi
                val newCleanElement = doc.createElement("string")
                newCleanElement.setAttribute("name", key)

                // Sao chép các thuộc tính khác nếu có (ví dụ: translatable="false")
                val attrs = existingElement.attributes
                for (i in 0 until attrs.length) {
                    val attr = attrs.item(i)
                    newCleanElement.setAttribute(attr.nodeName, attr.nodeValue)
                }

                // Chèn CDATA vào thẻ sạch
                newCleanElement.appendChild(cdata)

                // Thay thế thẻ cũ bằng thẻ sạch trên cây DOM
                root.replaceChild(newCleanElement, existingElement)
            } else {
                // Logic cho Key mới
                root.appendChild(doc.createTextNode("\n    "))
                val newString = doc.createElement("string")
                newString.setAttribute("name", key)
                newString.appendChild(cdata)
                root.appendChild(newString)
            }
        }
        saveDocument(doc, xmlFile)
    }

    private fun escapeAndroidString(input: String): String {
        return input.replace("'", "\\'").replace("’", "\\’")
            .replace("\"", "\\\"")
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
        val root = doc.createElement("resources")
        doc.appendChild(root)
        return doc
    }

    private fun saveDocument(doc: Document, file: File) {
        val transformer = TransformerFactory.newInstance().newTransformer()

        // GIẢI PHÁP TRIỆT ĐỂ:
        // Đổi INDENT thành "no" để Transformer không tự ý chèn thêm dòng trống xen kẽ
        transformer.setOutputProperty(OutputKeys.INDENT, "no")
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")

        // Đảm bảo encoding chuẩn
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")

        val source = DOMSource(doc)
        val result = StreamResult(file)
        transformer.transform(source, result)
    }
}