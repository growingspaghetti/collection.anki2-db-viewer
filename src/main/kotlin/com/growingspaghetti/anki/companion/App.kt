package com.growingspaghetti.anki.companion

import com.formdev.flatlaf.FlatLightLaf
import com.growingspaghetti.anki.companion.service.AnkiDbService
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.io.File
import javax.swing.*
import javax.swing.text.DefaultEditorKit


class App : JFrame(), Loggable, SqliteDbResolvable {
    private val sqliteDb: File = File("/home/ryoji/Desktop/c/Anki2/User 1/collection.sqlite")
    private val ankiDbService = AnkiDbService(this, this)

    private val menuBar = JMenuBar()
    private val fileMenu = JMenu("File")
    private val fileOpenItem = JMenuItem("Open")

    private val jEditorPane = JEditorPane()
    private val searchField = JTextField()
    private val jTabbedPane = JTabbedPane()
    private val rSyntaxTextArea = RSyntaxTextArea(10, 60)

    init {
        this.layout = BorderLayout()
        title = "Anki database viewer"

        fileMenu.add(fileOpenItem)
        menuBar.add(fileMenu)
        jMenuBar = menuBar

        val centerPanel = JPanel(BorderLayout())
        //JPanel southPanel = new JPanel(new BorderLayout());
        //JPanel southPanel = new JPanel(new BorderLayout());
        this.add(centerPanel, BorderLayout.CENTER)
        rSyntaxTextArea.isCodeFoldingEnabled = true
        rSyntaxTextArea.lineWrap = true
        this.add(RTextScrollPane(rSyntaxTextArea), BorderLayout.SOUTH)

        val menu = JPopupMenu()
        val copy: Action = DefaultEditorKit.CopyAction()
        copy.putValue(Action.NAME, "Copy")
        copy.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"))
        menu.add(copy)

        jEditorPane.contentType = "text/html"
        jEditorPane.isEditable = false
        jEditorPane.font = Font("Arial", Font.PLAIN, 13)
        jEditorPane.componentPopupMenu = menu


        val jSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
        centerPanel.add(jSplitPane, BorderLayout.CENTER)
        val westPanel = JPanel(BorderLayout())
        jSplitPane.leftComponent = westPanel
        westPanel.add(searchField, BorderLayout.NORTH)
        westPanel.add(jTabbedPane, BorderLayout.CENTER)
        jSplitPane.rightComponent = JScrollPane(jEditorPane)

        this.preferredSize = Dimension(1000, 800)
        pack()

        try {
            fetchCol()
            ankiDbService.fetchCards()
            ankiDbService.fetchNotes()
            ankiDbService.fetchRevLogs()
            ankiDbService.fetchGraves()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchCol() {
        jEditorPane.text = "<html>" + ankiDbService.fetchCol()
    }

    companion object {
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            SwingUtilities.invokeLater {
                try {
                    UIManager.setLookAndFeel(FlatLightLaf())
                } catch (ex: java.lang.Exception) {
                    System.err.println("Failed to initialize LaF")
                }
                val app = App()
                app.setLocationRelativeTo(null)
                app.isVisible = true
                app.defaultCloseOperation = EXIT_ON_CLOSE

            }
        }
    }

    override fun log(s: String) {
        SwingUtilities.invokeLater { rSyntaxTextArea.append(s + "\n") }
    }

    override fun getSqliteDb(): File = sqliteDb
}