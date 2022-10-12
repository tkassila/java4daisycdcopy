package com.metait.java4daisycdcopy;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.AccessibleRole;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Scanner;
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

    private Stage mainStage;
    private DirectoryChooser readChooser = new DirectoryChooser();
    private DirectoryChooser writeChooser = new DirectoryChooser();
    private File readDir;
    private File writeDir;
    private ObservableList<String> listitems = FXCollections.observableArrayList();;

    @FXML
    public void initialize() {

        readChooser.setTitle("Set read directory when copying");
        writeChooser.setTitle("Set write directory when copying");

        Tooltip tooltip = new Tooltip(
                "After read and write directory are set, then start to copy ino write directory");
        tooltip.setStyle("-fx-font-weight: bold; -fx-text-fill: yellow; -fx-font-size: 14");
        buttonCopy.setTooltip(tooltip);
        buttonCopy.setAccessibleHelp(tooltip.getText());
        buttonCopy.setAccessibleText(tooltip.getText());
        buttonCopy.setAccessibleRoleDescription(buttonCopy.getText());

        tooltip = new Tooltip(
                "Set a read  directory before a copy");
        tooltip.setStyle("-fx-font-weight: bold; -fx-text-fill: yellow; -fx-font-size: 14");
        buttonReadDir.setTooltip(tooltip);
        buttonReadDir.setAccessibleHelp(tooltip.getText());
        buttonReadDir.setAccessibleText(tooltip.getText());
        buttonReadDir.setAccessibleRole(AccessibleRole.BUTTON);
        buttonReadDir.setAccessibleRoleDescription(buttonReadDir.getText());

        tooltip = new Tooltip(
                "Set a target  directory before a copy");
        tooltip.setStyle("-fx-font-weight: bold; -fx-text-fill: yellow; -fx-font-size: 14");
        buttonWriteDir.setTooltip(tooltip);
        buttonWriteDir.setAccessibleHelp(tooltip.getText());
        buttonWriteDir.setAccessibleText(tooltip.getText());
        buttonWriteDir.setAccessibleRole(AccessibleRole.BUTTON);
        buttonWriteDir.setAccessibleRoleDescription(buttonWriteDir.getText());

        buttonReadDir.defaultButtonProperty().bind(buttonReadDir.focusedProperty());
        buttonWriteDir.defaultButtonProperty().bind(buttonWriteDir.focusedProperty());
        buttonCopy.defaultButtonProperty().bind(buttonCopy.focusedProperty());
        buttonCopy.setAccessibleRole(AccessibleRole.BUTTON);
        buttonCopy.setAccessibleRoleDescription(buttonCopy.getText());

        listView.setItems(listitems);
        labelMsg.setAccessibleText("Message of the application");
        labelMsg.setAccessibleRole(AccessibleRole.TEXT);
        labelMsg.setFocusTraversable(true);
        labelMsg.setAccessibleRoleDescription(labelMsg.getText());

        tooltip = new Tooltip(
                "list for copied files");
        tooltip.setStyle("-fx-font-weight: bold; -fx-text-fill: yellow; -fx-font-size: 14");
        listView.setTooltip(tooltip);
        listView.setAccessibleHelp(tooltip.getText());
        listView.setAccessibleText(tooltip.getText());
        listView.setAccessibleRole(AccessibleRole.LIST_VIEW);
        listView.setAccessibleRoleDescription(tooltip.getText());
    }

    private void setLabelMsg(String msg)
    {
        Platform.runLater(new Runnable() {
            public void run() {
                labelMsg.setText(msg);
                labelMsg.setAccessibleText(msg);
                labelMsg.requestFocus();
            }
        });
    }

        @FXML
    protected void pressedButtonReadDir() {
        // labelMsg.setText("pressedButtonReadDir pressed!");
        File file = readChooser.showDialog(mainStage);
        if (file != null) {
            textFieldReadDir.setText(file.getAbsolutePath());
        }
    }
    @FXML
    protected void pressedButtonWriteDir() {
        // labelMsg.setText("pressedButtonWriteDir pressed!");
        File file = writeChooser.showDialog(mainStage);
        if (file != null) {
            textFieldWriteDir.setText(file.getAbsolutePath());
        }
    }

    @FXML
    protected void pressedButtonCopy() {

        setLabelMsg("Copy started...");
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
            setLabelMsg("Write and read drives or directories are the same! Change beofre to copy.");
            return;
        }
        file = new File(readDir.getAbsolutePath() +File.separatorChar +"ncc.html");
        if (file.exists() && file.isFile())
            copyDaisyCdContent(readDir, writeDir, file);
        else
        {
            String strCreator = "";
            String strTitle = "kirja1";
            copyDirContent(readDir, writeDir, strTitle, strCreator, false);
        }
    }

    private void copyDaisyCdContent(File readDir, File writeDir, File nccFile)
    {
        String strTitle = "";
        String strCreator = "";
        String strContent = "";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(nccFile, StandardCharsets.UTF_8));
            String line;
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
                                    copyDirContent(readDir, writeDir, strTitle, strCreator, isDaisyCopy);
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
        }
    }

    private void copyDirContent(File readDir, File writeDir, String strTitle, String strCreator, boolean isDaisyCoopy)
    {
        if (readDir == null || !readDir.exists())
        {
            return;
        }
        if (writeDir == null || !writeDir.exists())
        {
            return;
        }
        if (strTitle == null || strTitle.trim().length()==0)
            return;
        if (strCreator == null || strCreator.trim().length()==0)
            return;

        File writeNewDir = new File(writeDir.getAbsolutePath()
                +File.separatorChar +strCreator +"," +strTitle);
        if (writeNewDir.exists())
        {
            String strAfile = "Directory";
            if (writeNewDir.isFile())
                strAfile = "File";
            setLabelMsg("The target directory (" +writeNewDir.getName() +") exists all ready in dir: " +writeDir.getAbsolutePath());
            return;
        }
        if (!writeNewDir.mkdir())
        {
            setLabelMsg("Cannot create dir: " +writeNewDir.getName() +" in dir: " +writeDir.getAbsolutePath());
            return;
        }
        listitems.clear();
        File [] files = null;
        try {
            files = readDir.listFiles();
            for(File f : files)
            {
                if (f.getName().equals("desktop.ini") && isDaisyCoopy)
                    continue; // skip this file
                copyFileIntoTargetDir(f, writeNewDir);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        listitems.add(f.getAbsolutePath());
                    }});
            }
            setLabelMsg("All files (" +files.length +") have been copied into dir: " +writeNewDir.getAbsolutePath());
        }catch (Exception e){
            e.printStackTrace();
            setLabelMsg("Error in the directory writing: " +e.getMessage());
            return;
        }
    }

    private void copyFileIntoTargetDir(File fSource, File ftargetDir)
            throws IOException
    {
        File newTargetFile = new File(ftargetDir.getAbsolutePath() +File.separator +fSource.getName());
        Path pathSource = fSource.toPath();
        Path pathTarget = newTargetFile.toPath();
        Files.copy(pathSource, pathTarget, REPLACE_EXISTING,  COPY_ATTRIBUTES);
        FileTime creationTime  = (FileTime) Files.readAttributes(pathSource, "creationTime").get("creationTime");
        Files.setAttribute(pathTarget, "creationTime", creationTime);
        FileTime modTime  = Files.readAttributes(pathSource, BasicFileAttributes.class).lastModifiedTime();
        Files.setLastModifiedTime(pathTarget, modTime);
    }

    public void setMainStage(Stage p_mainStage)
    {
        this.mainStage = p_mainStage;
    }
}