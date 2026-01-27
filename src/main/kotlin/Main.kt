package org.example

import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder

class MainLauncher : JFrame("Android String Automation Tool") {
    // --- 1. Khai báo các thành phần giao diện ---
    private val txtCsvPath = JTextField()
    private val txtProjectPath = JTextField()
    private val areaKeys = JTextArea(15, 25)
    private val btnSync = JButton("ADD / UPDATE")
    private val btnRemove = JButton("REMOVE KEYS")
    private val lblStatus = JLabel("Sẵn sàng...")

    init {
        setupLayout()
        setupEvents()

        // Cấu hình cửa sổ
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(950, 600)
        setLocationRelativeTo(null)
    }

    // --- 2. Hàm cập nhật trạng thái (updateStatus) ---
    /**
     * Hiển thị thông báo lên giao diện và đổi màu sắc tương ứng
     * @param msg Nội dung thông báo
     * @param isError true nếu là lỗi (đỏ), false nếu thành công (xanh)
     */
    private fun updateStatus(msg: String, isError: Boolean) {
        // Đảm bảo cập nhật UI luôn chạy trên Event Dispatch Thread
        SwingUtilities.invokeLater {
            lblStatus.text = "Nhật ký: $msg"
            lblStatus.foreground = if (isError) Color(0xD32F2F) else Color(0x388E3C)
        }
    }

    // --- 3. Hàm quản lý nút bấm (setButtonsEnabled) ---
    private fun setButtonsEnabled(enabled: Boolean) {
        SwingUtilities.invokeLater {
            btnSync.isEnabled = enabled
            btnRemove.isEnabled = enabled
            // Thay đổi con trỏ chuột khi đang xử lý
            cursor = if (enabled) Cursor.getDefaultCursor() else Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
        }
    }

    private fun setupLayout() {
        val mainPanel = JPanel(BorderLayout(20, 20)).apply {
            border = EmptyBorder(25, 25, 25, 25)
        }

        // Panel trái: Đường dẫn và Nút bấm
        val leftPanel = JPanel(GridBagLayout()).apply {
            val gbc = GridBagConstraints().apply {
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = Insets(10, 0, 10, 0)
                gridx = 0
            }
            add(JLabel("Đường dẫn file .csv (Strings chung):"), gbc)
            add(txtCsvPath, gbc)
            add(JLabel("Đường dẫn Project Android (/app):"), gbc)
            add(txtProjectPath, gbc)

            val pnlActions = JPanel(FlowLayout(FlowLayout.LEFT, 0, 15))
            pnlActions.add(btnSync)
            pnlActions.add(Box.createHorizontalStrut(15))

            btnRemove.apply {
                background = Color(0xD32F2F)
                foreground = Color.WHITE
                isFocusPainted = false
            }
            pnlActions.add(btnRemove)

            add(pnlActions, gbc)
            add(lblStatus, gbc)
        }

        // Panel phải: TextArea để dán Key
        val rightPanel = JPanel(BorderLayout(0, 10)).apply {
            add(JLabel("Dán danh sách Key (Cột A):"), BorderLayout.NORTH)
            add(JScrollPane(areaKeys), BorderLayout.CENTER)
        }

        mainPanel.add(leftPanel, BorderLayout.CENTER)
        mainPanel.add(rightPanel, BorderLayout.EAST)
        add(mainPanel)
    }

    private fun setupEvents() {
        btnSync.addActionListener { executeTask("SYNC") }
        btnRemove.addActionListener { executeTask("REMOVE") }
    }

    private fun executeTask(mode: String) {
        val csv = txtCsvPath.text.trim()
        val project = txtProjectPath.text.trim()
        val keys = areaKeys.text.lines().map { it.trim() }.filter { it.isNotEmpty() }

        if (csv.isEmpty() || project.isEmpty()) {
            updateStatus("Lỗi: Vui lòng không để trống đường dẫn!", true)
            return
        }

        // Xử lý Background Thread
        Thread {
            setButtonsEnabled(false)
            updateStatus("Đang xử lý...", false)

            try {
                // Gọi Logic động từ DynamicStringProcessor
                // Logic lấy Key từ cột A  và mã ngôn ngữ từ dòng 2
                val processor = DynamicStringProcessor(project)
                val result = processor.execute(csv, keys, mode)

                updateStatus(result.toString(), result is TransResult.Error)
            } catch (e: Exception) {
                updateStatus("Lỗi phát sinh: ${e.message}", true)
            } finally {
                setButtonsEnabled(true)
            }
        }.start()
    }
}

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    MainLauncher().isVisible = true
}