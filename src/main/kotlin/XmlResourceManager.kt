package org.example

import org.w3c.dom.CDATASection
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import java.io.StringWriter
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

        // Biến cờ kiểm tra thay đổi
        var isModified = false

        translations.forEach { (key, value) ->
            val escapedValue = escapeAndroidString(value)
            val existingElement = findElementByKey(root, key)
            // BỎ DÒNG: val cdata = doc.createCDATASection(escapedValue)

            when (mode) {
                "ADD_ONLY" -> {
                    if (existingElement != null) {
                        logs.add("Key '$key' đã tồn tại trong $folderName")
                    } else {
                        addNewElement(doc, root, key, escapedValue) // Truyền escapedValue
                        isModified = true
                    }
                }
                "UPDATE_ONLY" -> {
                    if (existingElement == null) {
                        logs.add("Key '$key' không tồn tại (Cần add mới)")
                    } else {
                        replaceWithCleanElement(doc, root, existingElement, key, escapedValue) // Truyền escapedValue
                        isModified = true
                    }
                }
                else -> { // SYNC mode
                    if (existingElement != null) {
                        replaceWithCleanElement(doc, root, existingElement, key, escapedValue)
                    } else {
                        addNewElement(doc, root, key, escapedValue)
                    }
                    isModified = true
                }
            }
        }

        // CHỈ LƯU FILE NẾU CÓ SỰ THAY ĐỔI THỰC SỰ
        if (isModified) {
            saveDocument(doc, xmlFile)
        }

        return logs.joinToString(", ")
    }

    private fun addNewElement(doc: Document, root: Element, key: String, escapedValue: String) {
        root.appendChild(doc.createTextNode("\n    "))
        val newString = doc.createElement("string")
        newString.setAttribute("name", key)

        // Sử dụng createTextNode thay vì createCDATASection
        newString.appendChild(doc.createTextNode(escapedValue))

        root.appendChild(newString)
    }

    private fun replaceWithCleanElement(doc: Document, root: Element, oldEl: Element, key: String, escapedValue: String) {
        val newEl = doc.createElement("string")
        newEl.setAttribute("name", key)

        // Copy lại các attributes cũ (nếu có như translatable="false")
        val attrs = oldEl.attributes
        for (i in 0 until attrs.length) {
            val attr = attrs.item(i)
            newEl.setAttribute(attr.nodeName, attr.nodeValue)
        }

        // Sử dụng createTextNode
        newEl.appendChild(doc.createTextNode(escapedValue))

        root.replaceChild(newEl, oldEl)
    }

    private fun escapeAndroidString(input: String): String {
        return input
            .replace("&", "&amp;") // XML bắt buộc
            .replace("<", "&lt;")   // XML bắt buộc
            .replace(">", "&gt;")   // XML bắt buộc
            .replace("'", "\\'")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
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

    // --- HÀM SAVE ĐƯỢC VIẾT LẠI HOÀN TOÀN ĐỂ FIX LỖI GIT ---
    private fun saveDocument(doc: Document, file: File) {
        val transformer = TransformerFactory.newInstance().newTransformer()

        // 1. TẮT việc tự động sinh header (để mình tự viết thủ công)
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")

        // 2. Giữ nguyên format nội dung (không indent tự động)
        transformer.setOutputProperty(OutputKeys.INDENT, "no")
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")

        // 3. Ghi nội dung XML (bắt đầu từ <resources>) vào bộ nhớ đệm StringWriter
        val writer = StringWriter()
        transformer.transform(DOMSource(doc), StreamResult(writer))
        val xmlBody = writer.toString()

        // 4. Tự tay viết chuỗi Header chuẩn y hệt file cũ của bạn (bao gồm cả ký tự xuống dòng \n)
        // Lưu ý: Chuỗi này khớp 100% với ảnh Screenshot_6 bạn gửi
        val customHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"

        // 5. Nối Header + Body và ghi đè vào file với encoding UTF-8
        file.writeText(customHeader + xmlBody, java.nio.charset.StandardCharsets.UTF_8)
    }
}