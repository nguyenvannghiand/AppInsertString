package org.example

import java.io.File
import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader

class DynamicStringProcessor(private val projectRoot: String) {
    fun execute(csvPath: String, targetKeys: List<String>, mode: String): TransResult {
        return try {
            val file = File(csvPath)
            if (!file.exists()) return TransResult.Error("Không tìm thấy file CSV tại: $csvPath")

            // Đọc toàn bộ file CSV vào bộ nhớ
            val reader = getSafeCsvReader()
            val rows: List<List<String>> = reader.readAll(file)
            if (rows.size < 3) return TransResult.Error("File CSV thiếu dữ liệu (cần ít nhất 3 dòng)")

            // 1. Lấy dòng mã ngôn ngữ (Dòng 2 - Index 1)
            val langCodes = rows[1]

            // Tạo map lưu trữ: Column Index -> Android Suffix (ví dụ: 3 -> "ar")
            val langMapping = mutableMapOf<Int, String>()
            for (i in 1 until langCodes.size) {
                val rawCode = langCodes[i].trim()
                if (rawCode.isEmpty()) continue

                // Cắt chuỗi lấy phần bên trái dấu '-' theo yêu cầu của bạn
                val cleanCode = when {
                    rawCode.equals("En", true) -> "" // values
                    rawCode.equals("Vi", true) -> "vi" // values-vi
                    rawCode.contains("-") -> rawCode.substringBefore("-").lowercase() // ar-SA -> ar
                    else -> rawCode.lowercase()
                }
                langMapping[i] = cleanCode
            }

            // 3. Lấy dữ liệu dịch thuật (Dòng 3 trở đi - Index 2)
            val dataRows = rows.drop(2)
            val xmlManager = XmlResourceManager(projectRoot)
            val errors = mutableListOf<String>()

            langMapping.forEach { (colIndex, langSuffix) ->
                val folderName = if (langSuffix.isEmpty()) "values" else "values-$langSuffix"
                val translations = mutableMapOf<String, String>()

                dataRows.forEach { row ->
                    val key = row[0].trim() // Cột A là KEY
                    if (key.isEmpty()) return@forEach

                    // Nếu targetKeys trống -> lấy hết. Nếu có -> chỉ lấy key trong list.
                    if (targetKeys.isEmpty() || targetKeys.contains(key)) {
                        translations[key] = row.getOrElse(colIndex) { "" }
                    }
                }

                // Thực thi ghi hoặc xóa
                if (mode == "SYNC") {
                    val currentErrors = StringValidator.checkErrors(translations)
                    errors.addAll(currentErrors)
                    xmlManager.updateStrings(folderName, translations)
                } else {
                    xmlManager.removeStrings(folderName, targetKeys)
                }
            }
            if (errors.isNotEmpty()) {
                println("⚠️ Cảnh báo lỗi Strings:\n${errors.joinToString("\n")}")
            }

            TransResult.Success
        } catch (e: Exception) {
            TransResult.Error("Quá trình xử lý thất bại: ${e.localizedMessage}")
        }
    }

    private fun getSafeCsvReader(): CsvReader {
        return csvReader {
            charset = "UTF-8"
            quoteChar = '\"'
            delimiter = ','
            escapeChar = '\\'
            skipEmptyLine = true
        }
    }
}