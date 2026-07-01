package org.chipfc.view;

import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;

/**
 * Custom settings provider to apply a dark theme to the terminal widget.
 */
public class DarkThemeSettings extends DefaultSettingsProvider {

    /**
     * Defines the default text style (foreground and background colors).
     */
    @Override
    public TextStyle getDefaultStyle() {
        return new TextStyle(
                TerminalColor.rgb(204, 204, 204), // Foreground color (light grey)
                TerminalColor.rgb(30, 30, 30)); // Background color (dark grey)
    }

    /**
     * Defines the default terminal font size.
     */
    @Override
    public float getTerminalFontSize() {
        return 14f;
    }
}
