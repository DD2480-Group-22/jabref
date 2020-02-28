package org.jabref.gui.preferences;

//TODO: Move IO imports with css writer function
import java.io.*;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.jabref.gui.DialogService;
import org.jabref.gui.util.ThemeLoader;
import org.jabref.logic.l10n.Localization;
import org.jabref.preferences.JabRefPreferences;

import de.saxsys.mvvmfx.utils.validation.FunctionBasedValidator;
import de.saxsys.mvvmfx.utils.validation.ValidationMessage;
import de.saxsys.mvvmfx.utils.validation.ValidationStatus;
import de.saxsys.mvvmfx.utils.validation.Validator;

public class AppearanceTabViewModel implements PreferenceTabViewModel {

    private final BooleanProperty fontOverrideProperty = new SimpleBooleanProperty();
    private final StringProperty fontSizeProperty = new SimpleStringProperty();
    private final BooleanProperty themeLightProperty = new SimpleBooleanProperty();
    private final BooleanProperty themeDarkProperty = new SimpleBooleanProperty();
    private final BooleanProperty themeCustomProperty = new SimpleBooleanProperty();
    private final BooleanProperty themeImportedProperty = new SimpleBooleanProperty();

    private final StringProperty colorBackgroundProperty = new SimpleStringProperty();
    private final StringProperty colorTextProperty = new SimpleStringProperty();
    private final StringProperty colorHighlightProperty = new SimpleStringProperty();

    private final DialogService dialogService;
    private final JabRefPreferences preferences;

    private Validator fontSizeValidator;

    private List<String> restartWarnings = new ArrayList<>();

    public AppearanceTabViewModel(DialogService dialogService, JabRefPreferences preferences) {
        this.dialogService = dialogService;
        this.preferences = preferences;

        fontSizeValidator = new FunctionBasedValidator<>(
                fontSizeProperty,
                input -> {
                    try {
                        return Integer.parseInt(fontSizeProperty().getValue()) > 8;
                    } catch (NumberFormatException ex) {
                        return false;
                    }
                },
                ValidationMessage.error(String.format("%s > %s %n %n %s",
                        Localization.lang("Appearance"),
                        Localization.lang("Font settings"),
                        Localization.lang("You must enter an integer value higher than 8."))));
    }

    @Override
    public void setValues() {
        fontOverrideProperty.setValue(preferences.getBoolean(JabRefPreferences.OVERRIDE_DEFAULT_FONT_SIZE));
        fontSizeProperty.setValue(String.valueOf(preferences.getInt(JabRefPreferences.MAIN_FONT_SIZE)));

        switch (preferences.get(JabRefPreferences.FX_THEME)) {
            case ThemeLoader.DARK_CSS:
                themeLightProperty.setValue(false);
                themeDarkProperty.setValue(true);
                themeImportedProperty.setValue(false);
                themeCustomProperty.setValue(false);
                break;
            case ThemeLoader.MAIN_CSS:
                themeLightProperty.setValue(true);
                themeDarkProperty.setValue(false);
                themeImportedProperty.setValue(false);
                themeCustomProperty.setValue(false);
                break;
            case ThemeLoader.CUSTOM_CSS:
                themeLightProperty.setValue(false);
                themeDarkProperty.setValue(false);
                themeImportedProperty.setValue(false);
                themeCustomProperty.setValue(true);
                break;
            default:
                themeLightProperty.setValue(false);
                themeDarkProperty.setValue(false);
                themeImportedProperty.setValue(true);
                themeCustomProperty.setValue(false);
        }
    }

    @Override
    public void storeSettings() {
        if (preferences.getBoolean(JabRefPreferences.OVERRIDE_DEFAULT_FONT_SIZE) != fontOverrideProperty.getValue()) {
            restartWarnings.add(Localization.lang("Override font settings"));
            preferences.putBoolean(JabRefPreferences.OVERRIDE_DEFAULT_FONT_SIZE, fontOverrideProperty.getValue());
        }

        int newFontSize = Integer.parseInt(fontSizeProperty.getValue());
        if (preferences.getInt(JabRefPreferences.MAIN_FONT_SIZE) != newFontSize) {
            restartWarnings.add(Localization.lang("Override font size"));
            preferences.putInt(JabRefPreferences.MAIN_FONT_SIZE, newFontSize);
        }

        if (themeLightProperty.getValue() && !preferences.get(JabRefPreferences.FX_THEME).equals(ThemeLoader.MAIN_CSS)) {
            restartWarnings.add(Localization.lang("Theme changed to light theme."));
            preferences.put(JabRefPreferences.FX_THEME, ThemeLoader.MAIN_CSS);
        } else if (themeDarkProperty.getValue() && !preferences.get(JabRefPreferences.FX_THEME).equals(ThemeLoader.DARK_CSS)) {
            restartWarnings.add(Localization.lang("Theme changed to dark theme."));
            preferences.put(JabRefPreferences.FX_THEME, ThemeLoader.DARK_CSS);
        } else if (themeImportedProperty.getValue()) {
            restartWarnings.add(Localization.lang("Theme change to a imported theme."));
            preferences.put(JabRefPreferences.FX_THEME, preferences.getPathToImportedTheme());
        } else if (themeCustomProperty.getValue()) {
            restartWarnings.add(Localization.lang("Theme change to a custom theme."));
            writeCustomTheme(colorBackgroundProperty.getValue(), colorTextProperty.getValueSafe(), colorHighlightProperty.getValueSafe());
            preferences.put(JabRefPreferences.FX_THEME, ThemeLoader.CUSTOM_CSS);
        }
    }

    //TODO: Maybe move function to more appropriate file
    private void writeCustomTheme(String background, String text, String highlight) {
        String path = "src/main/java/org/jabref/gui/";
        try {
            // Read from template and then write to Custom.css with the placeholder fields replaced with real values
            BufferedReader templateReader = new BufferedReader(new FileReader(path+"CustomTemplate.css"));
            BufferedWriter themeWriter = new BufferedWriter(new FileWriter(path +"Custom.css"));

            String line = null;
            while ((line = templateReader.readLine()) != null) {
                line = line.replace("[background]", background);
                line = line.replace("[background-d1]", colorCodeModifier(background, -2, -2, -2));
                line = line.replace("[background-l1]", colorCodeModifier(background, 11, 11, 11));
                line = line.replace("[background-l2]", colorCodeModifier(background, 22, 22, 22));
                line = line.replace("[background-l3]", colorCodeModifier(background, 33, 33, 33));

                line = line.replace("[text]", text);
                line = line.replace("[text-d1]", colorCodeModifier(text, -50, -50, -50));
                line = line.replace("[text-l1]", colorCodeModifier(text, 30, 30, 30));
                line = line.replace("[highlight]", highlight);
                line = line.replace("[highlight-d1]", colorCodeModifier(highlight, -50, -50, -50));
                line = line.replace("[highlight-l1]", colorCodeModifier(highlight, 30, 30, 30));

                themeWriter.write(line+"\n");
            }
            templateReader.close();
            themeWriter.close();
        } catch(IOException e) {
            //LOGGER.warn("Cannot load css CustomTemplate.css", e);
        }
    }

    private String colorCodeModifier(String colorCode, int redModification, int greenModification, int blueModification) {
        // Turn the RGB values of the hexadecimal color-coded string to decimal values
        int red = Integer.parseInt(colorCode.substring(1,3),16);
        int green = Integer.parseInt(colorCode.substring(3,5),16);
        int blue = Integer.parseInt(colorCode.substring(5),16);

        // Modify the values
        red += redModification;
        green += greenModification;
        blue += blueModification;

        // Check and bound the values within the acceptable range
        red = (red < 0 ) ? 0 : red;
        green = (green < 0 ) ? 0 : green;
        blue = (blue < 0 ) ? 0 : blue;
        red = (red > 255 ) ? 255 : red;
        green = (green > 255 ) ? 255 : green;
        blue = (blue > 255 ) ? 255 : blue;

        // Turn the modified RGB values back to color-coded string
        String modifiedColorCode = "#" + Integer.toHexString(red) + Integer.toHexString(green) + Integer.toHexString(blue);

        // Return modified color-code
        return modifiedColorCode;
    }

    public ValidationStatus fontSizeValidationStatus() { return fontSizeValidator.getValidationStatus(); }

    @Override
    public boolean validateSettings() {
        if (fontOverrideProperty.getValue() && !fontSizeValidator.getValidationStatus().isValid()) {
            fontSizeValidator.getValidationStatus().getHighestMessage().ifPresent(message ->
                    dialogService.showErrorDialogAndWait(message.getMessage()));
            return false;
        }
        return true;
    }

    @Override
    public List<String> getRestartWarnings() { return restartWarnings; }

    public BooleanProperty fontOverrideProperty() { return fontOverrideProperty; }

    public StringProperty fontSizeProperty() { return fontSizeProperty; }

    public BooleanProperty themeLightProperty() { return themeLightProperty; }

    public BooleanProperty themeDarkProperty() { return themeDarkProperty; }

    public BooleanProperty themeCustomProperty() { return themeCustomProperty; }

    public BooleanProperty themeImportedProperty() { return themeImportedProperty; }

    public StringProperty colorBackgroundProperty() { return colorBackgroundProperty; }

    public StringProperty colorTextProperty() { return colorTextProperty; }

    public StringProperty colorHighlightProperty() { return colorHighlightProperty; }
}
