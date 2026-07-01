package org.chipfc;

import javax.swing.SwingUtilities;

import org.chipfc.controller.MainController;
import org.chipfc.view.MainFrame;
import org.chipfc.view.modal.XModal;
import org.chipfc.view.toast.XToast;

import com.formdev.flatlaf.FlatDarkLaf;

import lombok.extern.slf4j.Slf4j;

/**
 * Entry point for the xTerm+ application.
 * 
 * @author bs (thanhhai135@gmail.com)
 */
@Slf4j
public class Main {
    public static void main(String[] args) {
        log.info("Starting xTerm+ Engine...");

        // Set up the FlatDark Look and Feel
        FlatDarkLaf.setup();

        SwingUtilities.invokeLater(() -> {
            // Initialize the main UI frame
            MainFrame frame = new MainFrame();
            XToast.install(frame);
            XModal.install(frame);

            // Initialize the controller to manage the view and application logic
            @SuppressWarnings("unused")
            MainController controller = new MainController(frame);

            frame.setVisible(true);
        });
    }
}
