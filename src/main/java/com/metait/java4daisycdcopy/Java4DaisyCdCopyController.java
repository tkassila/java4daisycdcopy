package com.metait.java4daisycdcopy;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.Optional;
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
    private MakeSound makeSound = null;
    private int iFilecopied = 0;
    @FXML
    public void initialize() {

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

        listView.setItems(listitems);
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
        Platform.runLater(new Runnable() {
            public void run() {
                labelMsg.setText(msg);
               // labelMsg.setAccessibleText(msg);
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

        makeSound = null;
        setLabelMsg("Copy started...");
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
            try {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Directory name");
                dialog.setHeaderText("You should set a name of created new directory. \n" +
                        "Press Ok or Cancel button after written dir name.");
                dialog.setResizable(true);

                Label label1 = new Label("Dir Name: ");
                TextField text1 = new TextField();
                GridPane grid = new GridPane();
                File [] subDirs = writeDir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isDirectory();
                    }
                });

                int text1_row = 1;
                if (subDirs.length > 0)
                {
                    Label label2 = new Label("Existing subdirs:");
                    grid.add(label2, 1, 1);
                    ListView<String> lview = new ListView<>();
                    lview.setFocusTraversable(true);
                    for (File f2 : subDirs)
                        lview.getItems().add(f2.getName());
                    grid.add(lview, 1, 2);
                    text1_row = 3;
                }
                grid.add(label1, 1, text1_row +1);
                grid.add(text1, 1, text1_row +2);

                dialog.getDialogPane().setContent(grid);
                ButtonType buttonTypeOk = ButtonType.OK;
                ButtonType buttonTypeCancel = ButtonType.CANCEL;
                dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
                dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    strTitle = text1.getText();
                    if (strTitle.trim().length()==0)
                    {
                        setLabelMsg("Directory name is emtpy. No copy.");
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
                            setLabelMsg("Directory name is emtpy. No copy.");
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
                makeSound = new MakeSound();
                makeSound.playSound(Java4DaisyCdCopyApplication.class.getResource("mistle_w.wav").toString());
                Thread.sleep(1000);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Please Wait...");
                alert.setHeaderText("The applicatioin is copying now. Wait...");
                alert.show();
                copyDirContent(readDir, writeDir, strTitle, strCreator, false);
                makeSound.stop();
                alert.close();
            } catch (Exception e) {
                e.printStackTrace();
                if (makeSound != null)
                    makeSound.stop();
                setLabelMsg("Error: " +e.getMessage().toString());
            }
        }
    }

    private void copyDaisyCdContent(File readDir, File writeDir, File nccFile)
    {
        String strTitle = "";
        String strCreator = "";
        String strContent = "";
        BufferedReader br = null;
        makeSound = null;
        try {
            startPlayWaw();
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
                                    makeSound = new MakeSound();
                                    makeSound.playSound(Java4DaisyCdCopyApplication.class.getResource("mistle_w.wav").toString());
                                    Thread.sleep(1000);
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Please Wait...");
                                    alert.setHeaderText("The applicatioin is copying now. Wait...");
                                    alert.show();
                                    copyDirContent(readDir, writeDir, strTitle, strCreator, isDaisyCopy);
                                    makeSound.stop();
                                    alert.close();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            if (makeSound != null)
                makeSound.stop();
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
    private void copyDirContent(File readDir, File writeDir, String strTitle, String strCreator, boolean isDaisyCoopy)
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

        File writeNewDir = new File(writeDir.getAbsolutePath()
                +File.separatorChar +strDirName);
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
        iFilecopied = 0;
        File [] files = null;
        try {
            files = readDir.listFiles();
            for(File f : files)
            {
                if (f.getName().equals("desktop.ini") && isDaisyCoopy)
                    continue; // skip this file
                copyFileIntoTargetDir(f, writeNewDir);
                /*
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                 */
                    if (f.isDirectory())
                        listitems.add("DIR: " +f.getName());
                    else
                        listitems.add(f.getName());
                //    }});
            }
            setLabelMsg("All files (" +iFilecopied +") have been copied into dir: " +writeNewDir.getAbsolutePath());
        }catch (Exception e){
            e.printStackTrace();
            setLabelMsg("Error in the directory writing: " +e.getMessage());
            throw e;
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
        iFilecopied++;
        if (fSource.isDirectory())
        {
            File [] subFiles = fSource.listFiles();
            for (File f : subFiles)
            {
                copyFileIntoTargetDir(f, newTargetFile);
                if (f.isDirectory())
                    listitems.add("DIR: " +f.getAbsolutePath());
                else
                    listitems.add(f.getAbsolutePath());
            }
        }
    }

    public void setMainStage(Stage p_mainStage)
    {
        this.mainStage = p_mainStage;
    }
}