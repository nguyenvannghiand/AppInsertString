import model.TransResult
import java.io.File

class DynamicStringProcessor(private val projectRoot: String) {
    fun execute(csvPath: String, targetKeys: List<String>, mode: String): TransResult {
        return try {
            val file = File(csvPath)
            if (!file.exists()) return TransResult.Error("File CSV không tồn tại")

            val rows = csvReader().readAll(file)
            if (rows.size < 3) return TransResult.Error("File CSV không đúng định dạng (cần tối thiểu 3 dòng)")

            // 1. Nhận diện ngôn ngữ từ dòng 2 (Index 1), cột B trở đi
            val langHeaderRow = rows[1]
            val langMapping = mutableMapOf<Int, String>()
            for (i in 1 until langHeaderRow.size) {
                val rawCode = langHeaderRow[i].trim()
                if (rawCode.isEmpty()) continue

                // Logic: Lấy phần bên trái dấu '-'
                val cleanCode = when {
                    rawCode.equals("En", true) -> ""
                    rawCode.contains("-") -> rawCode.substringBefore("-").lowercase()
                    else -> rawCode.lowercase()
                }
                langMapping[i] = cleanCode
            }

            // 2. Lấy dữ liệu từ dòng 3 (Index 2)
            val dataRows = rows.drop(2)
            val keysFromCsv = dataRows.map { it[0].trim() }

            // 3. Thực thi theo từng ngôn ngữ
            langMapping.forEach { (colIndex, langSuffix) ->
                val folderName = if (langSuffix.isEmpty()) "values" else "values-$langSuffix"
                val translations = mutableMapOf<String, String>()

                dataRows.forEach { row ->
                    val key = row[0].trim()
                    if (key.isEmpty()) return@forEach

                    // Chế độ lọc key: Nếu targetKeys trống thì lấy hết, ngược lại chỉ lấy key yêu cầu
                    if (targetKeys.isEmpty() || targetKeys.contains(key)) {
                        translations[key] = row.getOrElse(colIndex) { "" }
                    }
                }

                // Gọi hàm XML Manager (sẽ viết ở bước sau) để ghi file
                val xmlManager = XmlResourceManager(projectRoot)
                if (mode == "SYNC") {
                    xmlManager.updateStrings(folderName, translations)
                } else {
                    xmlManager.removeStrings(folderName, targetKeys)
                }
            }

            TransResult.Success
        } catch (e: Exception) {
            TransResult.Error(e.localizedMessage ?: "Lỗi không xác định")
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