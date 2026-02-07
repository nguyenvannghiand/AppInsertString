package org.example

import java.awt.*
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

class StringMasterUI : JFrame("Android String Automation - Excel Version") {
    private val txtExcelPath = JTextField()
    private val txtModulePath = JTextField()

    private val areaKeys = JTextArea(7, 40)

    private val txtLogArea = JTextArea()

    private val btnBrowseExcel = JButton("...")
    private val btnBrowseModule = JButton("...")

    private val btnSync = JButton("ADD / UPDATE (ALL)")
    private val btnAddKeyOnly = JButton("ADD KEY")
    private val btnUpdateKeyOnly = JButton("UPDATE KEY")
    private val btnRefresh = JButton("REFRESH")

    init {
        setupLayout()
        setupEvents()
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(950, 700)
        setLocationRelativeTo(null)
    }

    private fun setupLayout() {
        val mainPanel = JPanel(BorderLayout(15, 15)).apply { border = BorderFactory.createEmptyBorder(20, 20, 20, 20) }

        // 1. Panel bên trái (Input đường dẫn)
        val inputPanel = JPanel(GridBagLayout()).apply {
            val gbc = GridBagConstraints().apply { fill = GridBagConstraints.HORIZONTAL; insets = Insets(5, 5, 5, 5) }

            gbc.gridx = 0; gbc.gridy = 0; add(JLabel("1. File Excel (.xlsx):"), gbc)
            gbc.gridy = 1; gbc.weightx = 1.0; add(txtExcelPath, gbc)
            gbc.gridx = 1; add(btnBrowseExcel, gbc)

            gbc.gridx = 0; gbc.gridy = 2; add(JLabel("2. Lựa chọn đường dẫn Module (VD: .../app):"), gbc)
            gbc.gridy = 3; gbc.weightx = 1.0; add(txtModulePath, gbc)
            gbc.gridx = 1; add(btnBrowseModule, gbc)
        }

        // 2. Panel bên phải (Nhập Key và các nút thao tác)
        val rightPanel = JPanel(BorderLayout(0, 10)).apply {
            add(JLabel("Danh sách Key (mỗi dòng 1 key):"), BorderLayout.NORTH)
            add(JScrollPane(areaKeys), BorderLayout.CENTER)

            val combinedButtonPanel = JPanel(GridLayout(2, 1, 0, 5))
            val row1Panel = JPanel(GridLayout(1, 2, 5, 0))
            row1Panel.add(btnAddKeyOnly)
            row1Panel.add(btnUpdateKeyOnly)

            combinedButtonPanel.add(row1Panel)
            combinedButtonPanel.add(btnRefresh)
            add(combinedButtonPanel, BorderLayout.SOUTH)
        }

        mainPanel.add(inputPanel, BorderLayout.CENTER)
        mainPanel.add(rightPanel, BorderLayout.EAST)

        // 3. KHU VỰC LOG MỚI (Thay cho bottomPanel cũ)
        val logPanel = JPanel(BorderLayout(5, 5))

        // Nút Sync nằm trên cùng của vùng log
        val syncButtonPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))

        // --- CẬP NHẬT: Tăng kích thước nút ADD / UPDATE ---
        btnSync.preferredSize = Dimension(250, 30)

        syncButtonPanel.add(btnSync)
        logPanel.add(syncButtonPanel, BorderLayout.NORTH)

        // Cấu hình vùng hiển thị Log
        txtLogArea.isEditable = false
        txtLogArea.lineWrap = true
        txtLogArea.wrapStyleWord = true
        txtLogArea.font = Font("Monospaced", Font.PLAIN, 12)
        txtLogArea.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

        val scrollLog = JScrollPane(txtLogArea)
        scrollLog.preferredSize = Dimension(900, 180)
        scrollLog.border = BorderFactory.createTitledBorder("Trạng thái / Log:")

        logPanel.add(scrollLog, BorderLayout.CENTER)

        mainPanel.add(logPanel, BorderLayout.SOUTH)

        add(mainPanel)
    }

    private fun setupEvents() {
        btnBrowseExcel.addActionListener { chooseFile(txtExcelPath) }
        btnBrowseModule.addActionListener { chooseDirectory(txtModulePath) }

        btnSync.addActionListener { executeTask("SYNC") }
        btnAddKeyOnly.addActionListener { executeTask("ADD_ONLY") }
        btnUpdateKeyOnly.addActionListener { executeTask("UPDATE_ONLY") }

        btnRefresh.addActionListener {
            areaKeys.text = ""
            txtLogArea.text = "Sẵn sàng"
            txtLogArea.foreground = Color.BLACK
        }
    }

    private fun executeTask(mode: String) {
        val excel = txtExcelPath.text
        val module = txtModulePath.text
        val keys = areaKeys.text.lines().map { it.trim() }.filter { it.isNotBlank() }

        if (excel.isEmpty() || module.isEmpty()) {
            txtLogArea.text = "Lỗi: Vui lòng chọn file Excel và Module!"
            txtLogArea.foreground = Color.RED
            return
        }

        Thread {
            SwingUtilities.invokeLater {
                txtLogArea.text = "Đang thực hiện $mode...\nVui lòng chờ..."
                txtLogArea.foreground = Color.BLUE
            }

            val result = DynamicStringProcessor(module).process(excel, keys, mode)

            SwingUtilities.invokeLater {
                txtLogArea.text = result
                txtLogArea.foreground = if (result.startsWith("Lỗi")) Color.RED else Color.BLACK
                txtLogArea.caretPosition = txtLogArea.document.length
            }
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

fun main() {
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch (e: Exception) {
        e.printStackTrace()
    }
    SwingUtilities.invokeLater {
        StringMasterUI().isVisible = true
    }
}