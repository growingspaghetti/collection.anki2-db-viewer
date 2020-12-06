package com.growingspaghetti.anki.companion

import com.formdev.flatlaf.FlatLightLaf
import com.growingspaghetti.anki.companion.service.AnkiDbService
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import java.awt.*
import java.awt.event.ActionListener
import java.io.File
import java.time.LocalDateTime
import java.util.*
import javax.swing.*
import javax.swing.text.DefaultEditorKit


class App : JFrame(), SqliteDbResolvable {
    private val ankiDbService = AnkiDbService(this)

    private val jEditorPane = JEditorPane()
    private val jTabbedPane = JTabbedPane()
    private val playAudioCheckBox = JCheckBox("Play audio & scroll automatically")

    init {
        layout = BorderLayout()
        title = "Anki auxiliary tool"

        RepeatingReleasedEventsFixer().install()
        val centerPanel = JPanel(BorderLayout())
        add(jTabbedPane, BorderLayout.CENTER)
        val colCardNoteRevsSwingList = ColCardNoteRevsSwingList(jEditorPane, collectionMediaDir(), playAudioCheckBox)
        jTabbedPane.add(centerPanel, "Review")

        centerPanel.add(JScrollPane(colCardNoteRevsSwingList), BorderLayout.CENTER)
        val controlPanel = JPanel(FlowLayout())
        centerPanel.add(controlPanel, BorderLayout.NORTH)
        with(controlPanel) {
            val decksModel = DefaultComboBoxModel<String>()
            ankiDbService.decks().forEach { decksModel.addElement(it.name) }
            val deckComboBox = JComboBox(decksModel)
            deckComboBox.selectedIndex = 0
            add(deckComboBox)

            val datesModel = DefaultComboBoxModel<String>()
            for (i in -10..4) {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, i)
                datesModel.addElement(DATE_FORMATTER.format(calendar.toInstant()))
            }
            val dateComboBox = JComboBox(datesModel)
            val tomorrow = DATE_FORMATTER.format(LocalDateTime.now().plusDays(1))
            dateComboBox.selectedIndex = datesModel.getIndexOf(tomorrow)
            add(dateComboBox)

            val queryButton = JButton("Search")
            add(queryButton)
            queryButton.addActionListener(ActionListener {
                SwingUtilities.invokeLater(Runnable {
                    val deckName = deckComboBox.selectedItem as String
                    val date = SIMPLE_DATE_FORMAT.parse(dateComboBox.selectedItem as String)
                    val colCardNoteRevsList = ankiDbService.queues(deckName, date)
                    colCardNoteRevsSwingList.setList(colCardNoteRevsList)
                })
            })
        }

        preferredSize = Dimension(600, 800)
        setLocation(0, 0)
        pack()

        val editorFrame = JFrame()
        with(editorFrame) {
            val menu = JPopupMenu()
            val copy: Action = DefaultEditorKit.CopyAction()
            copy.putValue(Action.NAME, "Copy")
            copy.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"))
            menu.add(copy)
            with(jEditorPane) {
                background = Color.WHITE
                contentType = "text/html"
                isEditable = false
                font = Font("Arial", Font.PLAIN, 13)
                componentPopupMenu = menu
            }
            val editorPanel = JPanel(BorderLayout())
            editorPanel.add(JScrollPane(jEditorPane), BorderLayout.CENTER)
            val mediaControlPanel = JPanel(FlowLayout())
            editorPanel.add(mediaControlPanel, BorderLayout.NORTH)
            val stopButton = JButton("Stop audio")
            val autoSlideShow = JCheckBox("Slideshow")
            with(playAudioCheckBox) {
                isSelected = true
                horizontalAlignment = JCheckBox.CENTER
                addChangeListener {
                    autoSlideShow.isSelected = isSelected
                }
            }
            with(autoSlideShow) {
                isSelected = true
                horizontalAlignment = JCheckBox.CENTER
                addChangeListener {
                }
            }
            with(mediaControlPanel) {
                add(stopButton)
                add(playAudioCheckBox)
                //add(autoSlideShow)
            }
            stopButton.addActionListener(ActionListener {
                Mp3Player.stop()
            })

            add(editorPanel)
            preferredSize = Dimension(600, 800)
            pack()
            setLocation(600, 0)
            isVisible = true
            defaultCloseOperation = DO_NOTHING_ON_CLOSE
        }
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
                app.isVisible = true
                app.defaultCloseOperation = EXIT_ON_CLOSE
            }
        }
    }

    override fun sqliteDb(): File {
        val collectionUnderCurrentDir = File("collection.anki2")
        return if (collectionUnderCurrentDir.exists()) {
            collectionUnderCurrentDir
        } else {
            File(System.getProperty("user.home") + "/.local/share/Anki2/User 1/collection.anki2")
        }
    }
}