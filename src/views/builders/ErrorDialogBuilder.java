package views.builders;

import enums.IssueType;
import supportModel.ErrorEntity;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * Created by pkhvoros on 4/28/15.
 */
public class ErrorDialogBuilder {
    public static Dialog buildErrorDialog(java.util.List<ErrorEntity> errorEntities, Frame owner) {
        if (errorEntities.size() == 0) {
            return null;
        }
//        String message = "";
//        for (ErrorEntity errorEntity:errorEntities){
//            message = message + errorEntity.getErrorType().getMessage() + errorEntity.getMessage() + "\n";
//        }
        JDialog dialog = new JDialog(owner);
        dialog.setTitle("Issue log");
        dialog.setLocationByPlatform(true);
        StyleContext sc = new StyleContext();
        final DefaultStyledDocument doc = new DefaultStyledDocument(sc);

        final Style errorStyle = sc.addStyle("ErrorStyle", null);
        errorStyle.addAttribute(StyleConstants.Foreground, Color.red);

        JTextPane pane = new JTextPane(doc);
        try {
            for (ErrorEntity errorEntity : errorEntities) {
                if (errorEntity.getErrorType().getIssueType() == IssueType.Error ||
                        errorEntity.getErrorType().getIssueType() == IssueType.FatalError) {
                    String message = "ERROR: " + errorEntity.getErrorType().getMessage() + errorEntity.getMessage() + "\n";
                    doc.insertString(doc.getLength(), message, null);
                    doc.setParagraphAttributes(doc.getLength() - message.length(), 1, errorStyle, false);
                }
            }
            for (ErrorEntity errorEntity : errorEntities) {
                if (errorEntity.getErrorType().getIssueType() == IssueType.Warning) {
                    String message = "WARNING: " + errorEntity.getErrorType().getMessage() + errorEntity.getMessage() + "\n";
                    doc.insertString(doc.getLength(), message, null);
                    doc.setParagraphAttributes(doc.getLength() - message.length(), 1, errorStyle, false);
                }
            }
            for (ErrorEntity errorEntity : errorEntities) {
                if (errorEntity.getErrorType().getIssueType() == IssueType.Info) {
                    String message = "INFO: " + errorEntity.getErrorType().getMessage() + errorEntity.getMessage() + "\n";
                    doc.insertString(doc.getLength(), message, null);
                }
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        pane.select(0, 0);
        pane.setEditable(false);
        dialog.setPreferredSize(new Dimension(400, 300));
        dialog.add(new JScrollPane(pane));
        dialog.pack();
        dialog.setVisible(true);
        return dialog;
    }
}
