package org.example

object StringValidator {
    // Regex tìm các định dạng %s, %d, %1$s... [cite: 2, 13, 203]
    private val placeholderRegex = "%(\\d+\\$)?[-#+ 0,(\\[]*[a-zA-Z]".toRegex()

    fun checkErrors(translations: Map<String, String>): List<String> {
        val errorList = mutableListOf<String>()

        translations.forEach { (key, value) ->
            // Kiểm tra dấu nháy đơn chưa được escape [cite: 93, 118]
            if (value.contains("'") && !value.contains("\\'")) {
                errorList.add("Key [$key]: Thiếu dấu gạch chéo ngược cho dấu nháy đơn (\\')")
            }

            // Kiểm tra tính hợp lý của %s (ví dụ: key có chữ 'new' thường chứa %s) [cite: 1, 13, 201]
            if (key.contains("new") && !value.contains("%")) {
                // Đây chỉ là cảnh báo gợi ý
                // errorList.add("Key [$key]: Có thể thiếu placeholder %s")
            }
        }
        return errorList
    }
}