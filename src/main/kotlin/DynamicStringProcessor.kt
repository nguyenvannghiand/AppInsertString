package org.example

import java.io.File
import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream

class DynamicStringProcessor(private val modulePathFromUI: String) {
    private val xmlManager = XmlResourceManager()

    fun process(excelPath: String, targetKeys: List<String>): String {
        return try {
            val workbook = XSSFWorkbook(FileInputStream(File(excelPath)))
            val sheet = workbook.getSheetAt(0)

            // Dòng 2: Mã ngôn ngữ
            val langRow = sheet.getRow(1) ?: return "Lỗi: Excel thiếu dòng 2"
            val langMapping = mutableMapOf<Int, String>()

            for (cn in 1 until langRow.lastCellNum.toInt()) {
                val code = langRow.getCell(cn)?.toString()?.trim() ?: continue
                langMapping[cn] = mapToAndroidFolder(code)
            }

            // Duyệt từng cột ngôn ngữ
            langMapping.forEach { (colIndex, folderName) ->
                val translations = mutableMapOf<String, String>()

                // Dòng 3 trở đi: Dữ liệu Key/Value
                for (rn in 2..sheet.lastRowNum) {
                    val row = sheet.getRow(rn) ?: continue
                    val key = row.getCell(0)?.toString()?.trim() ?: continue

                    if (key.isNotEmpty() && (targetKeys.isEmpty() || targetKeys.contains(key))) {
                        val value = row.getCell(colIndex)?.toString() ?: ""
                        translations[key] = value
                    }
                }

                // TRUYỀN modulePathFromUI vào hàm updateStrings
                xmlManager.updateStrings(this.modulePathFromUI, folderName, translations)
            }

            workbook.close()
            "Cập nhật thành công cho module: ${File(modulePathFromUI).name}"
        } catch (e: Exception) {
            "Lỗi: ${e.localizedMessage}"
        }
    }

    private fun mapToAndroidFolder(code: String): String {
        return when {
            code.equals("En", true) -> "values"
            code.contains("-") -> "values-${code.substringBefore("-").lowercase()}"
            else -> "values-${code.lowercase()}"
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