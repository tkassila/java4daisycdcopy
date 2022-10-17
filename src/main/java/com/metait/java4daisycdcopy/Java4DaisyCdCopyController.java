package com.metait.java4daisycdcopy;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.AccessibleRole;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Java4DaisyCdCopyController {
    @FXML
    private Button buttonReadDir;
    @FXML
    private Button buttonWriteDir;
    @FXML
    private TextField textFieldReadDir;
    @FXML
    private TextField textFieldWriteDir;
    @FXML
    private TextArea labelMsg;
    @FXML
    private ListView<String> listView;
    @FXML
    private Button buttonCopy;
    @FXML
    private Button buttonCancel;

    private Stage mainStage;
    private final DirectoryChooser readChooser = new DirectoryChooser();
    private final DirectoryChooser writeChooser = new DirectoryChooser();
    private File readDir = null;
    private File writeDir = null;
    private final ObservableList<String> listItems = FXCollections.observableArrayList();
    // private MakeSound makeSound = null;
    private int iFileCopied = 0;
    private final Alert alert = new Alert(Alert.AlertType.INFORMATION);
    private Task<Void> task = null;
    private File writeNewDir;

    @FXML
    public void initialize() {

        buttonCancel.setDisable(true);
       // makeSound = new MakeSound();
        alert.setTitle("Please Wait...");
        alert.setHeaderText("The application is copying now. Wait...");

        readChooser.setTitle("Set read directory when copying");
        writeChooser.setTitle("Set write directory when copying");

        Tooltip tooltip = new Tooltip(
                "After read and write directory are set, then start to copy ino write directory");
        tooltip.setStyle("-fx-font-weight: bold; -fx-text-fill: yellow; -fx-font-size: 14");
        buttonCopy.setTooltip(tooltip);
        buttonCopy.setAccessibleHelp(tooltip.getText());
        // buttonCopy.setAccessibleText(tooltip.getText());
        //buttonCopy.setAccessibleRoleDescription(buttonCopy.getText());

        tooltip = new Tooltip(
                "Set a read  directory before a copy");
        tooltip.setStyle("-fx-font-weight: bold; -fx-text-fill: yellow; -fx-font-size: 14");
        buttonReadDir.setTooltip(tooltip);
        buttonReadDir.setAccessibleHelp(tooltip.getText());
        //buttonReadDir.setAccessibleText(tooltip.getText());
        buttonReadDir.setAccessibleRole(AccessibleRole.BUTTON);
        // buttonReadDir.setAccessibleRoleDescription(buttonReadDir.getText());

        tooltip = new Tooltip(
                "Set a target  directory before a copy");
        tooltip.setStyle("-fx-font-weight: bold; -fx-text-fill: yellow; -fx-font-size: 14");
        buttonWriteDir.setTooltip(tooltip);
        buttonWriteDir.setAccessibleHelp(tooltip.getText());
        //buttonWriteDir.setAccessibleText(tooltip.getText());
        buttonWriteDir.setAccessibleRole(AccessibleRole.BUTTON);
        // buttonWriteDir.setAccessibleRoleDescription(buttonWriteDir.getText());

        buttonReadDir.defaultButtonProperty().bind(buttonReadDir.focusedProperty());
        buttonWriteDir.defaultButtonProperty().bind(buttonWriteDir.focusedProperty());
        buttonCopy.defaultButtonProperty().bind(buttonCopy.focusedProperty());
        buttonCopy.setAccessibleRole(AccessibleRole.BUTTON);
        // buttonCopy.setAccessibleRoleDescription(buttonCopy.getText());

        listView.setItems(listItems);

        labelMsg.setAccessibleText("Message of the application");
        labelMsg.setAccessibleRole(AccessibleRole.TEXT);
        labelMsg.setFocusTraversable(true);
        // labelMsg.setAccessibleRoleDescription(labelMsg.getText());

        tooltip = new Tooltip(
                "list for copied files");
        tooltip.setStyle("-fx-font-weight: bold; -fx-text-fill: yellow; -fx-font-size: 14");
        listView.setTooltip(tooltip);
        listView.setAccessibleHelp(tooltip.getText());
        listView.setAccessibleText(tooltip.getText());
        listView.setAccessibleRole(AccessibleRole.LIST_VIEW);
        // listView.setAccessibleRoleDescription(tooltip.getText());
    }

    private void setLabelMsg(String msg)
    {
        Platform.runLater(() -> {
            labelMsg.setText(msg);
           // labelMsg.setAccessibleText(msg);
            labelMsg.requestFocus();
        });
    }

        @FXML
    protected void pressedButtonReadDir() {
        // labelMsg.setText("pressedButtonReadDir pressed!");
        File file = readChooser.showDialog(mainStage);
        if (file != null) {
            textFieldReadDir.setText(file.getAbsolutePath());
            if (file.isDirectory())
            {
                readChooser.setInitialDirectory(file);
            }
            else {
                File fPath = file.getParentFile();
                if (fPath != null && fPath.isDirectory()) {
                    readChooser.setInitialDirectory(fPath);
                }
            }
        }
    }
    @FXML
    protected void pressedButtonWriteDir() {
        // labelMsg.setText("pressedButtonWriteDir pressed!");
        File file = writeChooser.showDialog(mainStage);
        if (file != null) {
            textFieldWriteDir.setText(file.getAbsolutePath());
            if (file.isDirectory())
            {
                writeChooser.setInitialDirectory(file);
            }
            else {
                File fPath = file.getParentFile();
                if (fPath != null && fPath.isDirectory()) {
                    writeChooser.setInitialDirectory(fPath);
                }
            }
        }
    }

    @FXML
    protected void pressedButtonCancel()
    {
        if (task != null && task.getState() == Worker.State.READY)
        {
            task.cancel(true);
            buttonCancel.setDisable(true);
            buttonCopy.setDisable(false);
        }
    }
    @FXML
    protected void pressedButtonCopy() {

        setLabelMsg("Copy started...");
        listView.getItems().clear();
        listItems.clear();
        listView.setItems(listItems);

        if (textFieldReadDir.getText().trim().length()==0)
        {
            setLabelMsg("Read directory text field is empty!");
            return;
        }
        if (textFieldWriteDir.getText().trim().length()==0)
        {
            setLabelMsg("Write directory text field is empty!");
            return;
        }

        File file = new File(textFieldReadDir.getText());
        if (!file.exists())
        {
            setLabelMsg("Read drive or directory does not exists! : " +file.getAbsolutePath());
            return;
        }
        if (!file.isDirectory())
        {
            setLabelMsg("Read drive or directory is a file! : " +file.getAbsolutePath());
            return;
        }
        readDir = file;

        file = new File(textFieldWriteDir.getText());
        if (!file.exists())
        {
            setLabelMsg("Write drive or directory does not exists! : " +file.getAbsolutePath());
            return;
        }
        if (!file.isDirectory())
        {
            setLabelMsg("Write drive or directory is a file! : " +file.getAbsolutePath());
            return;
        }
        writeDir = file;

        if (writeDir.getAbsolutePath().equals(readDir.getAbsolutePath()))
        {
            setLabelMsg("Write and read drives or directories are the same! Change before to copy.");
            return;
        }
        file = new File(readDir.getAbsolutePath() +File.separatorChar +"ncc.html");
        if (file.exists() && file.isFile())
            copyDaisyCdContent(readDir, writeDir, file);
        else
        {
            String strCreator = "";
            String strTitle = "kirja1";
            try {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Directory name");
                dialog.setHeaderText("You should set a name of created new directory. \n" +
                        "Press Ok or Cancel button after written dir name.");
                dialog.setResizable(true);

                Label label1 = new Label("Dir Name: ");
                TextField text1 = new TextField();
                GridPane grid = new GridPane();
                File [] subDirs = writeDir.listFiles(pathname -> pathname.isDirectory());

                int text1_row = 1;
                if (subDirs != null && subDirs.length > 0)
                {
                    Label label2 = new Label("Existing sub dirs:");
                    grid.add(label2, 1, 1);
                    ListView<String> listView = new ListView<>();
                    listView.setFocusTraversable(true);
                    for (File f2 : subDirs)
                        listView.getItems().add(f2.getName());
                    grid.add(listView, 1, 2);
                    text1_row = 3;
                }
                grid.add(new Label(""), 1,  text1_row +1);
                grid.add(label1, 1, text1_row +2);
                grid.add(text1, 1, text1_row +3);

                dialog.getDialogPane().setContent(grid);
                ButtonType buttonTypeOk = ButtonType.OK;
                ButtonType buttonTypeCancel = ButtonType.CANCEL;
                dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
                dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
                Platform.runLater(()-> text1.requestFocus() );
                Optional<ButtonType> result = dialog.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    strTitle = text1.getText();
                    if (strTitle.trim().length()==0)
                    {
                        setLabelMsg("Directory name is empty. No copy.");
                        return;
                    }
                }
                else {
                    setLabelMsg("You pressed the cancel button. No copy.");
                    return;
                }

                File fTest = new File(writeDir.getAbsolutePath() +File.separatorChar +strTitle);
                if (fTest.exists())
                {
                    dialog.setHeaderText("Directory " +text1.getText() +" exists all ready.\n" +
                            "Give an another directory name.\n" +
                            "Press Ok or Cancel button after written dir name.");
                    result = dialog.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        strTitle = text1.getText();
                        if (strTitle.trim().length()==0)
                        {
                            setLabelMsg("Directory name is empty. No copy.");
                            return;
                        }
                    }
                    else {
                        setLabelMsg("You pressed the cancel button. No copy.");
                        return;
                    }
                    fTest = new File(writeDir.getAbsolutePath() +File.separatorChar +strTitle);
                    if (fTest.exists())
                    {
                        setLabelMsg("No copy. This directory exists all ready: " +fTest.getAbsolutePath());
                        return;
                    }
                }
                /*
                Platform.runLater(new Runnable() {
                    public void run() {
                 */
                        // alert.show();
                /*
                (())     makeSound.playSound(Java4DaisyCdCopyApplication.class.getResource("mistle_w.wav").toString());
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                        }
/*                    }
                 */
              //  });
                //         */
                /*
                setLabelMsg("Started copy...");
                Platform.runLater(new Runnable() {
                    public void run() {
                        // labelMsg.setAccessibleText(msg);
                        alert.setTitle("Please Wait...");
                        alert.setHeaderText("The application is copying now. Wait...");
                        alert.show();
                    }
                });
                 */
                copyDirContent(readDir, writeDir, strTitle, strCreator, false);
            } catch (Exception e) {
                e.printStackTrace();
                setLabelMsg("Error: " +e.getMessage());
            }
        }
    }

    private void copyDaisyCdContent(File readDir, File writeDir, File nccFile)
    {
        String strTitle = "";
        String strCreator = "";
        String strContent = "";
        BufferedReader br = null;
        try {
            startPlayWaw();
            br = new BufferedReader(new FileReader(nccFile, StandardCharsets.UTF_8));
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ((line = br.readLine()) != null) {
                sb.append(line +"\n");
            }
            strContent = sb.toString();
            if (strContent.trim().length()>0) {
                int indHead = strContent.indexOf("<head>");
                if (indHead > -1)
                {
                    final String searchTitle = "<title>";
                    int indTitle = strContent.indexOf(searchTitle, indHead);
                    if (indTitle > -1)
                    {
                        final String searchTitleEnd = "</title>";
                        int indTitleEnd = strContent.indexOf(searchTitleEnd, indTitle);
                        if (indTitleEnd > -1) {
                            strTitle = strContent.substring(indTitle +searchTitle.length(),
                                    indTitleEnd);
                            System.out.println("Title:" +strTitle);
                            if (strTitle != null && strTitle.trim().length()>0)
                            {
                                Pattern p = Pattern.compile("<meta\\s+name\\s*=\\s*\"dc:creator\"\\s+content\\s*=\\s*\"(.*)\"\\s*/>");//. represents single character
                                Matcher m = p.matcher(strContent);
                                boolean found = m.find();
                                if (found)
                                {
                                    strCreator = m.group(1);
                                    System.out.println("strCreator:" +strCreator);
                                    if (strCreator.trim().length()==0)
                                        strCreator = new String("");
                                    boolean isDaisyCopy = true;
                                    // Thread.sleep(1000);
                                    /*
                                    Platform.runLater(new Runnable() {
                                        public void run() {
                                     */
                                            // alert.show();
                                    /*
                                            makeSound.playSound(Java4DaisyCdCopyApplication.class.getResource("mistle_w.wav").toString());
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                        }
                        */
/*                                        }
                                    });
                         */

                                    copyDirContent(readDir, writeDir, strTitle, strCreator, isDaisyCopy);
                                    // alert.close();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (br != null)
                try {
                    br.close();
                }catch (IOException ioe){
                    ioe.printStackTrace();
                }
            stopPlayWaw();
        }
    }

    private void startPlayWaw()
    {

    }

    private void stopPlayWaw()
    {

    }

    private String getCorrectDirName(String strDir, boolean bYesBackSlash)
    {
        String strNewDir = new String(strDir);
        if (strNewDir.contains("?"))
            strNewDir = strNewDir.replaceAll("\\?", "").toString();
        if (strNewDir.contains("<"))
            strNewDir = strNewDir.replaceAll("\\<", "").toString();
        if (strNewDir.contains(">"))
            strNewDir = strNewDir.replaceAll("\\>", "").toString();
        if (!bYesBackSlash && strNewDir.contains(":"))
            strNewDir = strNewDir.replaceAll("\\:", " ").toString();
        if (!bYesBackSlash && strNewDir.contains("/"))
            strNewDir = strNewDir.replaceAll("\\/", " ").toString();
        if (!bYesBackSlash && strNewDir.contains("\\"))
            strNewDir = strNewDir.replaceAll("\\\\", " ").toString();
        if (strNewDir.contains("|"))
            strNewDir = strNewDir.replaceAll("\\|", " ").toString();
        if (strNewDir.contains("*"))
            strNewDir = strNewDir.replaceAll("\\*", " ").toString();
        if (File.separatorChar == '\\') // windows:
        {
            for(int i = 0; i < 32; i++)
            {
                char c = (char)i;
                if (strNewDir.contains(Character.toString(c)))
                {
                    strNewDir = strNewDir.replaceAll(Character.toString(c), "");
                }
            }
        }
        else // linux
        {
            char c = (char)0;
            if (strNewDir.contains(Character.toString(c)))
            {
                strNewDir = strNewDir.replaceAll(Character.toString(c), "");
            }
        }
        return strNewDir;
    }

    private void copyDirContent(File readDir, File writeDir, String strTitle, String strCreator, boolean isDaisyCopy)
            throws Exception
    {
        if (readDir == null || !readDir.exists())
        {
            return;
        }
        if (writeDir == null || !writeDir.exists())
        {
            return;
        }
        String strDirName = new String("");
        if (strTitle == null || strTitle.trim().length()==0)
            return;
        strDirName = "" +strTitle;
        if (strCreator == null)
            return;
        else {
            if (strCreator.trim().length() > 0)
                strDirName = strCreator + ", " + strTitle;
        }

        String strNewDir = writeDir.getAbsolutePath()
                +File.separatorChar +getCorrectDirName(strDirName, false);
        boolean bYesBackSlash = true;
        strNewDir = getCorrectDirName(strNewDir, bYesBackSlash);
        /*
        if (strNewDir.contains(" "))
            strNewDir = "" + '"' + strNewDir +'"';
         */
        writeNewDir = new File(strNewDir);
        if (writeNewDir.canWrite())
        {
            throw new Exception("Write lock is maybe on. Cannot write a new dir!");
        }
        if (writeNewDir.exists())
        {
            String strAFile = "Directory";
            if (writeNewDir.isFile())
                strAFile = "File";
            setLabelMsg("The target directory (" +writeNewDir.getName() +") exists all ready in dir: " +writeDir.getAbsolutePath());
            return;
        }
        if (!writeNewDir.mkdir())
        {
            setLabelMsg("Cannot create dir: " +writeNewDir.getName() +" in dir: " +writeDir.getAbsolutePath());
            return;
        }

        Platform.runLater(new Runnable() {
            public void run() {
                buttonCancel.setDisable(false);
                buttonCopy.setDisable(true);
            }
        });

        //Task for computing the Panels:
        task = new Task<Void>() {
            private MakeSound makeSound = new MakeSound();

            @Override
            protected Void call() throws Exception {
                makeSound.playSound(Java4DaisyCdCopyApplication.class.getResource("mistle_w.wav").toString());
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                }

                File[] files = null;
                try {
                    Platform.runLater(() -> {
                        alert.show();
                    });
                    files = readDir.listFiles();
                    for (File f : files) {
                        if (f.getName().equals("desktop.ini") && isDaisyCopy)
                            continue; // skip this file
                        copyFileIntoTargetDir(f, writeNewDir);
                /*
                try {
                    Thread.sleep(2000);
                }catch (Exception e){
                }
                 */
                        Platform.runLater(new Runnable() {
                            public void run() {
                                if (f.isDirectory())
                                    listItems.add("DIR: " + f.getName());
                                else
                                    listItems.add(f.getName());
                            }
                        });
                    }
                    setLabelMsg("All files (" + iFileCopied + ") have been copied into dir: " + writeNewDir.getAbsolutePath());
                    Platform.runLater(new Runnable() {
                        public void run() {
                            makeSound.stop();
                            try {
                                Thread.sleep(1000);
                            }catch (Exception e){
                            }
                            buttonCancel.setDisable(true);
                            buttonCopy.setDisable(false);
                            alert.close();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    makeSound.stop();
                    try {
                        Thread.sleep(1000);
                    }catch (Exception e2){
                    }
                    setLabelMsg("Error in the directory writing: " + e.getMessage());
                    task.cancel(true);
                    Platform.runLater(new Runnable() {
                        public void run() {
                            buttonCancel.setDisable(true);
                            buttonCopy.setDisable(false);
                            alert.close();
                        }
                    });
                    throw e;
                }
                return null;
             }};
        new Thread(task).start();
    }

    private void copyFileIntoTargetDir(File fSource, File fTargetDir)
            throws IOException
    {
        File newTargetFile = new File(fTargetDir.getAbsolutePath() +File.separator +fSource.getName());
        Path pathSource = fSource.toPath();
        Path pathTarget = newTargetFile.toPath();
        Files.copy(pathSource, pathTarget, REPLACE_EXISTING,  COPY_ATTRIBUTES);
        FileTime creationTime  = (FileTime) Files.readAttributes(pathSource, "creationTime").get("creationTime");
        Files.setAttribute(pathTarget, "creationTime", creationTime);
        FileTime modTime  = Files.readAttributes(pathSource, BasicFileAttributes.class).lastModifiedTime();
        Files.setLastModifiedTime(pathTarget, modTime);
        iFileCopied++;
        if (fSource.isDirectory())
        {
            File [] subFiles = fSource.listFiles();
            for (File f : subFiles)
            {
                copyFileIntoTargetDir(f, newTargetFile);
                Platform.runLater(new Runnable() {
                    public void run() {
                        if (f.isDirectory())
                            listItems.add("DIR: " + f.getName());
                        else
                            listItems.add(f.getName());
                    }
                });
            }
        }
    }

    public void setMainStage(Stage p_mainStage)
    {
        this.mainStage = p_mainStage;
    }
}