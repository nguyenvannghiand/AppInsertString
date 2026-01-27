package org.example

import java.awt.*
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

class StringMasterUI : JFrame("Android String Automation - Excel Version") {
    private val txtExcelPath = JTextField()
    private val txtProjectPath = JTextField()
    private val txtModulePath = JTextField() // Ô nhập đường dẫn Module
    private val areaKeys = JTextArea(10, 40)
    private val lblStatus = JLabel("Sẵn sàng")

    private val btnBrowseExcel = JButton("...")
    private val btnBrowseProject = JButton("...")
    private val btnBrowseModule = JButton("...") // Nút chọn Module
    private val btnSync = JButton("ADD / UPDATE")

    init {
        setupLayout()
        setupEvents()
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(900, 600)
        setLocationRelativeTo(null)
    }

    private fun setupLayout() {
        val mainPanel = JPanel(BorderLayout(15, 15)).apply { border = BorderFactory.createEmptyBorder(20, 20, 20, 20) }

        val inputPanel = JPanel(GridBagLayout()).apply {
            val gbc = GridBagConstraints().apply { fill = GridBagConstraints.HORIZONTAL; insets = Insets(5, 5, 5, 5) }

            // 1. Chọn file Excel
            gbc.gridy = 0; add(JLabel("1. File Excel (.xlsx):"), gbc)
            gbc.gridy = 1; gbc.weightx = 1.0; add(txtExcelPath, gbc)
            gbc.gridx = 1; add(btnBrowseExcel, gbc)

            // 2. Chọn Gốc Dự án (Project Root)
            gbc.gridx = 0; gbc.gridy = 2; add(JLabel("2. Gốc dự án Android (Project Root):"), gbc)
            gbc.gridy = 3; gbc.weightx = 1.0; add(txtProjectPath, gbc)
            gbc.gridx = 1; add(btnBrowseProject, gbc)

            // 3. Chọn Module (Nơi chứa res/values) - ĐÂY LÀ PHẦN MỚI
            gbc.gridx = 0; gbc.gridy = 4; add(JLabel("3. Lựa chọn đường dẫn Module (Ví dụ: /app hoặc /core):"), gbc)
            gbc.gridy = 5; gbc.weightx = 1.0; add(txtModulePath, gbc)
            gbc.gridx = 1; add(btnBrowseModule, gbc)
        }

        val rightPanel = JPanel(BorderLayout(0, 10)).apply {
            add(JLabel("Danh sách Key lọc (Để trống nếu muốn add hết):"), BorderLayout.NORTH)
            add(JScrollPane(areaKeys), BorderLayout.CENTER)
        }

        mainPanel.add(inputPanel, BorderLayout.CENTER)
        mainPanel.add(rightPanel, BorderLayout.EAST)
        mainPanel.add(JPanel(FlowLayout(FlowLayout.LEFT)).apply { add(btnSync); add(lblStatus) }, BorderLayout.SOUTH)
        add(mainPanel)
    }

    private fun setupEvents() {
        btnBrowseExcel.addActionListener { chooseFile(txtExcelPath) }
        btnBrowseProject.addActionListener { chooseDirectory(txtProjectPath) }
        btnBrowseModule.addActionListener { chooseDirectory(txtModulePath) }

        btnSync.addActionListener {
            val excel = txtExcelPath.text
            val module = txtModulePath.text // Lấy đường dẫn module
            val keys = areaKeys.text.lines().filter { it.isNotBlank() }

            if (excel.isEmpty() || module.isEmpty()) {
                lblStatus.text = "Lỗi: Vui lòng chọn đủ File Excel và Module!"
                return@addActionListener
            }

            Thread {
                lblStatus.text = "Đang xử lý..."
                // Truyền modulePath vào Processor
                val result = DynamicStringProcessor(module).process(excel, keys)
                lblStatus.text = result
            }.start()
        }
    }

    private fun chooseFile(target: JTextField) {
        val chooser = JFileChooser().apply { fileFilter = FileNameExtensionFilter("Excel Files", "xlsx") }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) target.text = chooser.selectedFile.absolutePath
    }

    private fun chooseDirectory(target: JTextField) {
        val chooser = JFileChooser().apply { fileSelectionMode = JFileChooser.DIRECTORIES_ONLY }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) target.text = chooser.selectedFile.absolutePath
    }
}

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    SwingUtilities.invokeLater {
        StringMasterUI().isVisible = true
    }
}