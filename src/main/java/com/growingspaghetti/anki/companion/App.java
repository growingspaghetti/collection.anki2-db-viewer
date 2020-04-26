package com.growingspaghetti.anki.companion;

import com.growingspaghetti.anki.companion.service.AnkiDbService;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class App extends JFrame implements SqliteDbResolvable, Loggable {
  private File sqliteDb;
  private AnkiDbService ankiDbService = new AnkiDbService(this, this);

  private JMenuBar jMenuBar = new JMenuBar();
  private JMenu fileMenu = new JMenu("File");
  private JMenuItem fileOpenItem = new JMenuItem("Open");

  private JEditorPane jEditorPane = new JEditorPane();
  private JTextField searchField = new JTextField();
  private JTabbedPane jTabbedPane = new JTabbedPane();
  private RSyntaxTextArea rSyntaxTextArea = new RSyntaxTextArea(10, 60);

  private App() {
    sqliteDb = new File("/home/ryoji/Desktop/c/Anki2/User 1/collection.sqlite");
    this.setLayout(new BorderLayout());
    this.setTitle("Anki database viewer");

    fileMenu.add(fileOpenItem);
    jMenuBar.add(fileMenu);
    this.setJMenuBar(jMenuBar);

    JPanel centerPanel = new JPanel(new BorderLayout());
    //JPanel southPanel = new JPanel(new BorderLayout());
    this.add(centerPanel, BorderLayout.CENTER);
    rSyntaxTextArea.setCodeFoldingEnabled(true);
    rSyntaxTextArea.setLineWrap(true);
    this.add(new RTextScrollPane(rSyntaxTextArea), BorderLayout.SOUTH);

    JPopupMenu menu = new JPopupMenu();
    Action copy = new DefaultEditorKit.CopyAction();
    copy.putValue(Action.NAME, "Copy");
    copy.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
    menu.add(copy);

    jEditorPane.setContentType("text/html");
    jEditorPane.setEditable(false);
    jEditorPane.setFont(new Font("Arial", Font.PLAIN, 13));
    jEditorPane.setComponentPopupMenu(menu);


    JSplitPane jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    centerPanel.add(jSplitPane, BorderLayout.CENTER);
    JPanel westPanel = new JPanel(new BorderLayout());
    jSplitPane.setLeftComponent(westPanel);
    westPanel.add(searchField, BorderLayout.NORTH);
    westPanel.add(jTabbedPane, BorderLayout.CENTER);
    jSplitPane.setRightComponent(new JScrollPane(jEditorPane));

    this.setPreferredSize(new Dimension(1000, 800));
    this.pack();

    try {
      fetchCol();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void fetchCol() throws Exception {
    jEditorPane.setText("<html>" + ankiDbService.fetchCol());
  }

  public static void main(String[] args) throws IOException {

    SwingUtilities.invokeLater(
        () -> {
          App app = new App();
          app.setVisible(true);
          app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        });
  }

  @Override
  public File getSqliteDb() {
    return this.sqliteDb;
  }

  @Override
  public void log(String s) {
    SwingUtilities.invokeLater(
        () -> {
          rSyntaxTextArea.append(s + "\n");
        });
  }
}
