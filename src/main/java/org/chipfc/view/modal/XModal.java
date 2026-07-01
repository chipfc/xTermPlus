package org.chipfc.view.modal;

import java.awt.Component;

import raven.modal.ModalDialog;
import raven.modal.Toast.Type;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.BorderOption;
import raven.modal.option.Location;
import raven.modal.option.Option;

public class XModal {
    private static Component owner;

    private XModal() {
        // Private constructor to prevent instantiation
    }

    public static void install(Component owner) {
        XModal.owner = owner;
    }

    private static Option createOption() {
        Option option = ModalDialog.createOption();
        option.setAnimationEnabled(true)
                .setCloseOnPressedEscape(false)
                .setBackgroundClickType(Option.BackgroundClickType.NONE)
                .setOpacity(0.5f)
                .setHeavyWeight(false);
        option.getBorderOption()
                .setBorderWidth(0)
                .setShadow(BorderOption.Shadow.EXTRA_LARGE);
        option.getLayoutOption().setLocation(Location.CENTER, Location.CENTER)
                .setRelativeToOwner(false)
                .setMovable(true);
        option.getLayoutOption().setAnimateDistance(0, 0)
                .setAnimateScale(0.1f);
        return option;
    }

    private static void show(Type type, String title, String message) {
        ModalDialog.showModal(owner, new SimpleMessageModal(
                type, message, title,
                SimpleModalBorder.CLOSE_OPTION, null), createOption());
    }

    public static void show(String title, String message) {
        show(Type.DEFAULT, title, message);
    }

    public static void showSuccess(String title, String message) {
        show(Type.SUCCESS, title, message);
    }

    public static void showInfo(String title, String message) {
        show(Type.INFO, title, message);
    }

    public static void showWarning(String title, String message) {
        show(Type.WARNING, title, message);
    }

    public static void showError(String title, String message) {
        show(Type.ERROR, title, message);
    }

}
