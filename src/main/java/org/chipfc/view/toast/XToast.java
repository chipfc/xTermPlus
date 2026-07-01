package org.chipfc.view.toast;

import java.awt.Component;

import raven.modal.Toast;
import raven.modal.option.Location;
import raven.modal.toast.option.ToastBorderStyle;
import raven.modal.toast.option.ToastLocation;
import raven.modal.toast.option.ToastOption;
import raven.modal.toast.option.ToastStyle;

/**
 * A wrapper utility for displaying toast notifications across the application.
 */
public class XToast {

    private static Component owner;

    private XToast() {
        // Private constructor to prevent instantiation
    }

    /**
     * Installs the toast utility with the main application frame/component.
     * 
     * @param owner The parent component for the toasts.
     */
    public static void install(Component owner) {
        XToast.owner = owner;
    }

    /**
     * Configures the default style and behavior for all toast notifications.
     */
    private static ToastOption getSelectedOption() {
        ToastOption option = Toast.createOption();

        option.setAnimationEnabled(true)
                .setPauseDelayOnHover(true)
                .setAutoClose(true)
                .setCloseOnClick(false)
                .setHeavyWeight(false);

        // Layout settings: Display at the bottom-right corner
        option.getLayoutOption()
                .setLocation(ToastLocation.from(Location.TRAILING, Location.BOTTOM))
                .setRelativeToOwner(false);

        // Visual style settings
        option.getStyle().setBackgroundType(ToastStyle.BackgroundType.GRADIENT)
                .setShowIcon(true)
                .setShowLabel(true)
                .setIconSeparateLine(true)
                .setShowCloseButton(true)
                .setPaintTextColor(false)
                .getBorderStyle()
                .setBorderType(ToastBorderStyle.BorderType.LEADING_LINE);

        return option;
    }

    private static void show(Toast.Type type, String message) {
        if (owner == null) {
            // Fallback or log if install() wasn't called
            return;
        }
        Toast.show(owner, type, message, getSelectedOption());
    }

    // --- Public API ---

    public static void show(String message) {
        show(Toast.Type.DEFAULT, message);
    }

    public static void showSuccess(String message) {
        show(Toast.Type.SUCCESS, message);
    }

    public static void showInfo(String message) {
        show(Toast.Type.INFO, message);
    }

    public static void showWarning(String message) {
        show(Toast.Type.WARNING, message);
    }

    public static void showError(String message) {
        show(Toast.Type.ERROR, message);
    }
}
