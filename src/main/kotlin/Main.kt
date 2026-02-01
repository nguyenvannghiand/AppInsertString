package org.example

import java.awt.*
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

class StringMasterUI : JFrame("Android String Automation - Excel Version") {
    private val txtExcelPath = JTextField()
    private val txtProjectPath = JTextField()
    private val txtModulePath = JTextField()

    private val areaKeys = JTextArea(7, 40)
    private val lblStatus = JLabel("Sẵn sàng")

    private val btnBrowseExcel = JButton("...")
    private val btnBrowseProject = JButton("...")
    private val btnBrowseModule = JButton("...")

    private val btnSync = JButton("ADD / UPDATE") // Giữ nguyên chức năng Sync cũ
    private val btnAddKeyOnly = JButton("ADD KEY")     // Chức năng Add key input
    private val btnUpdateKeyOnly = JButton("UPDATE KEY") // Chức năng  Update key input
    private val btnRefresh = JButton("REFRESH") // Refresh UI

    init {
        setupLayout()
        setupEvents()
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(950, 650)
        setLocationRelativeTo(null)
    }

    private fun setupLayout() {
        val mainPanel = JPanel(BorderLayout(15, 15)).apply { border = BorderFactory.createEmptyBorder(20, 20, 20, 20) }

        // 1. Panel bên trái (Input đường dẫn)
        val inputPanel = JPanel(GridBagLayout()).apply {
            val gbc = GridBagConstraints().apply { fill = GridBagConstraints.HORIZONTAL; insets = Insets(5, 5, 5, 5) }
            gbc.gridy = 0; add(JLabel("1. File Excel (.xlsx):"), gbc)
            gbc.gridy = 1; gbc.weightx = 1.0; add(txtExcelPath, gbc)
            gbc.gridx = 1; add(btnBrowseExcel, gbc)
            gbc.gridx = 0; gbc.gridy = 2; add(JLabel("2. Gốc dự án Android (Project Root):"), gbc)
            gbc.gridy = 3; gbc.weightx = 1.0; add(txtProjectPath, gbc)
            gbc.gridx = 1; add(btnBrowseProject, gbc)
            gbc.gridx = 0; gbc.gridy = 4; add(JLabel("3. Lựa chọn đường dẫn Module:"), gbc)
            gbc.gridy = 5; gbc.weightx = 1.0; add(txtModulePath, gbc)
            gbc.gridx = 1; add(btnBrowseModule, gbc)
        }

        // 2. Panel bên phải (Nhập Key và các nút thao tác)
        val rightPanel = JPanel(BorderLayout(0, 10)).apply {
            add(JLabel("Danh sách Key (mỗi dòng 1 key):"), BorderLayout.NORTH)
            add(JScrollPane(areaKeys), BorderLayout.CENTER)

            // Panel chứa các nút bấm dưới areaKeys
            // Sử dụng GridLayout 2 dòng: Dòng 1 chứa Add/Update, Dòng 2 chứa Refresh
            val combinedButtonPanel = JPanel(GridLayout(2, 1, 0, 5)) // 2 rows, 1 col, gap 5px

            // Dòng 1: Add và Update nằm ngang
            val row1Panel = JPanel(GridLayout(1, 2, 5, 0))
            row1Panel.add(btnAddKeyOnly)
            row1Panel.add(btnUpdateKeyOnly)

            // Thêm vào container chung
            combinedButtonPanel.add(row1Panel)
            combinedButtonPanel.add(btnRefresh) // Dòng 2: Refresh nằm dưới

            add(combinedButtonPanel, BorderLayout.SOUTH)
        }

        mainPanel.add(inputPanel, BorderLayout.CENTER)
        mainPanel.add(rightPanel, BorderLayout.EAST)

        // Khu vực dưới cùng chứa nút Sync cũ và nhãn trạng thái
        val bottomPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        bottomPanel.add(btnSync)
        bottomPanel.add(lblStatus)
        mainPanel.add(bottomPanel, BorderLayout.SOUTH)

        add(mainPanel)
    }

    private fun setupEvents() {
        btnBrowseExcel.addActionListener { chooseFile(txtExcelPath) }
        btnBrowseProject.addActionListener { chooseDirectory(txtProjectPath) }
        btnBrowseModule.addActionListener { chooseDirectory(txtModulePath) }

        // Chức năng cũ: Sync
        btnSync.addActionListener { executeTask("SYNC") }

        // Chức năng mới: Chỉ ADD
        btnAddKeyOnly.addActionListener { executeTask("ADD_ONLY") }

        // Chức năng mới: Chỉ UPDATE
        btnUpdateKeyOnly.addActionListener { executeTask("UPDATE_ONLY") }

        // --- Chức năng REFRESH ---
        btnRefresh.addActionListener {
            // 1. Xóa nội dung key nhập vào
            areaKeys.text = ""
            // 2. Đưa trạng thái về ban đầu
            lblStatus.text = "Sẵn sàng"
        }
    }

    private fun executeTask(mode: String) {
        val excel = txtExcelPath.text
        val module = txtModulePath.text
        val keys = areaKeys.text.lines().map { it.trim() }.filter { it.isNotBlank() }

        if (excel.isEmpty() || module.isEmpty()) {
            lblStatus.text = "Lỗi: Vui lòng chọn file Excel và Module!"
            return
        }

        Thread {
            lblStatus.text = "Đang thực hiện $mode..."
            val result = DynamicStringProcessor(module).process(excel, keys, mode)
            lblStatus.text = "<html>$result</html>"
        }.start()
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