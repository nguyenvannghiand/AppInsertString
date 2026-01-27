package org.example

sealed class TransResult {
    object Success : TransResult() {
        override fun toString(): String = "Thành công!"
    }

    data class Error(val message: String) : TransResult() {
        override fun toString(): String = "Lỗi: $message"
    }
}