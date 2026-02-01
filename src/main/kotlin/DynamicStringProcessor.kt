package org.example

import java.io.File
import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream

class DynamicStringProcessor(private val modulePathFromUI: String) {
    private val xmlManager = XmlResourceManager()

    fun process(excelPath: String, targetKeys: List<String>, mode: String = "SYNC"): String {
        return try {
            val workbook = XSSFWorkbook(FileInputStream(File(excelPath)))
            val sheet = workbook.getSheetAt(0)

            val langRow = sheet.getRow(1) ?: return "Lỗi: Excel thiếu dòng 2"
            val langMapping = mutableMapOf<Int, String>()

            for (cn in 1 until langRow.lastCellNum.toInt()) {
                val code = langRow.getCell(cn)?.toString()?.trim() ?: continue
                langMapping[cn] = mapToAndroidFolder(code)
            }

            val finalLogs = mutableListOf<String>()

            langMapping.forEach { (colIndex, folderName) ->
                val translations = mutableMapOf<String, String>()
                for (rn in 2..sheet.lastRowNum) {
                    val row = sheet.getRow(rn) ?: continue
                    val key = row.getCell(0)?.toString()?.trim() ?: continue
                    if (key.isNotEmpty() && (targetKeys.isEmpty() || targetKeys.contains(key))) {
                        translations[key] = row.getCell(colIndex)?.toString() ?: ""
                    }
                }

                // Gọi XmlResourceManager với mode cụ thể
                val log = xmlManager.updateStrings(this.modulePathFromUI, folderName, translations, mode)
                if (log.isNotEmpty()) finalLogs.add(log)
            }

            workbook.close()
            if (finalLogs.isEmpty()) "Thành công!" else finalLogs.distinct().joinToString("<br>")
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
}