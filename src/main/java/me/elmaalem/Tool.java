package me.elmaalem;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tool {

    private JPanel allPanel;
    private JPanel menuPanel;
    private JPanel editorCodePanel;
    private JPanel outputPanel;
    private JButton fileButton;
    private JButton runButton;
    private JScrollPane editorScrollPane;
    private JEditorPane textEditorPane;
    private JTextArea textOutput;
    private JLabel consoleLabel;
    private JLabel fileNameLabel;
    private JScrollPane consoleScrollPane;
    private JTabbedPane tabPane;
    private JButton saveButton;
    private JProgressBar progressBar;
    private JLabel progressLabel;

    private File file;

    PrintStream printStream = new PrintStream(new CustomOutputStream(textOutput));
    //TODO : Redirect System.in to console
    CustomInputStream customInputStream = new CustomInputStream(textOutput);

    public Tool() {

        // Click of File Button
        fileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                String fileName = "";
                do {
                    fileName = JOptionPane.showInputDialog(allPanel,
                            "What is your file name?", "");
                } while (fileName.isEmpty());

                fileNameLabel.setText(fileName + ".kts");
                tabPane.setTitleAt(0, fileName + ".kts");
                file = new File("src/main/java/resourceScripts/" + fileName + ".kts");

                try {
                    if (file.createNewFile()) {
                        //System.out.println("File created: " + file.getName());
                    } else {
                        //System.out.println("File already exists.");
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                try {
                    textEditorPane.setEditable(true);
                    textOutput.setText("");

                    URL url = file.toURI().toURL();
                    System.out.println(url);
                    textEditorPane.setPage(file.toURI().toURL());//URI =URL+ /#posts
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        // Click of Run Button
        runButton.addActionListener(new ActionListener() {

            public void printLines(String std, InputStream ins) throws Exception {
                String line = null;
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(ins));
                while ((line = in.readLine()) != null) {
                    System.out.println(std + " " + line);
                }
            }

            public void runProcess(String command) throws Exception {
                System.out.println("Process is currently running");
                progressBar.setValue(0);

                Process pro = Runtime.getRuntime().exec(command);
                printLines("stdout: ", pro.getInputStream());
                printLines("stderr: ", pro.getErrorStream());

                pro.waitFor();
                progressBar.setValue(100);
                System.out.println("Process finished with exit code " + pro.exitValue());
            }

            public void actionPerformed(ActionEvent e) {

                textOutput.setText("");
                System.setOut(printStream);
                System.setErr(printStream);
                //TODO : Redirect System.in to console
                //System.setIn(customInputStream);

                System.out.println("**********");
                try {
                    runProcess("kotlinc -script src/main/java/resourceScripts/" + fileNameLabel.getText());
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                String str = textOutput.getText();

                //Extract the File name Error and Line number of Error
                String regex = "\\(.*?\\.kts:\\d\\)";
                //Creating a pattern object
                Pattern pattern = Pattern.compile(regex);
                ArrayList list = new ArrayList();
                //Matching the compiled pattern in the String
                Matcher matcher = pattern.matcher(str);
                while (matcher.find()) {
                    list.add(matcher.group());
                }
                Iterator it = list.iterator();
                System.out.println("**********");
                System.out.println("Name of File and Line Number of Error : ");
                while (it.hasNext()) {
                    System.out.println(it.next());
                }


            }
        });

        // Click of Save Button
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FileWriter writer = null;
                try {
                    writer = new FileWriter(file);
                    textEditorPane.write(writer);
                    writer.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        // Progress Bar
        progressBar.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress" == evt.getPropertyName()) {
                    int progress = (Integer) evt.getNewValue();
                    progressBar.setValue(progress);
                }
            }
        });

        // Enter Input from user and click of "Enter" Key
        //TODO : add possibilite for Input
        /*textOutput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == e.VK_ENTER) {
                    //TODO : Not yet updated
                    customInputStream.actionPerformed(e);
                }
            }
        });*/
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tool");
        frame.setContentPane(new Tool().allPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setSize(500, 500);
        frame.setVisible(true);
    }
}


