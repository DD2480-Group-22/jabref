package org.jabref.gui.preferences;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Paint;

import org.jabref.gui.util.ControlHelper;
import org.jabref.gui.util.IconValidationDecorator;
import org.jabref.logic.l10n.Localization;
import org.jabref.preferences.JabRefPreferences;

import com.airhacks.afterburner.views.ViewLoader;
import de.saxsys.mvvmfx.utils.validation.visualization.ControlsFxVisualizer;

public class AppearanceTabView extends AbstractPreferenceTabView<AppearanceTabViewModel> implements PreferencesTab {

    @FXML public CheckBox fontOverride;
    @FXML public TextField fontSize;
    @FXML public RadioButton themeLight;
    @FXML public RadioButton themeDark;
    @FXML public RadioButton themeImported;
    @FXML public RadioButton themeCustom;
    @FXML public TextField colorBackground;
    @FXML public TextField colorText;
    @FXML public TextField colorHighlight;

    @FXML public Rectangle colorBackgroundDisplay;
    @FXML public Rectangle colorTextDisplay;
    @FXML public Rectangle colorHighlightDisplay;

    private final String colorStandardBackground = "#121212";
    private final StringProperty colorCodeBackground = new SimpleStringProperty();
    private final String colorStandardText = "#dedede";
    private final StringProperty colorCodeText = new SimpleStringProperty();
    private final String colorStandardHighlight = "#aa6767";
    private final StringProperty colorCodeHighlight = new SimpleStringProperty();

    private final String colorCodeRegex = "\\#[\\dabcdef]{6}";

    private final ControlsFxVisualizer validationVisualizer = new ControlsFxVisualizer();

    public AppearanceTabView(JabRefPreferences preferences) {
        this.preferences = preferences;

        ViewLoader.view(this)
                .root(this)
                .load();
    }

    @Override
    public String getTabName() { return Localization.lang("Appearance"); }

    public void initialize () {
        this.viewModel = new AppearanceTabViewModel(dialogService, preferences);

        fontOverride.selectedProperty().bindBidirectional(viewModel.fontOverrideProperty());
        fontSize.setTextFormatter(ControlHelper.getIntegerTextFormatter());
        fontSize.textProperty().bindBidirectional(viewModel.fontSizeProperty());

        themeLight.selectedProperty().bindBidirectional(viewModel.themeLightProperty());
        themeDark.selectedProperty().bindBidirectional(viewModel.themeDarkProperty());
        themeImported.selectedProperty().bindBidirectional(viewModel.themeImportedProperty());
        themeCustom.selectedProperty().bindBidirectional(viewModel.themeCustomProperty());

        colorCodeBackground.bindBidirectional(viewModel.colorBackgroundProperty());
        colorCodeText.bindBidirectional(viewModel.colorTextProperty());
        colorCodeHighlight.bindBidirectional(viewModel.colorHighlightProperty());

        // Add input listeners to all color text field inputs
        addColorCodeListener(colorBackground, colorCodeBackground, colorBackgroundDisplay, colorStandardBackground);
        addColorCodeListener(colorText, colorCodeText, colorTextDisplay, colorStandardText);
        addColorCodeListener(colorHighlight, colorCodeHighlight, colorHighlightDisplay, colorStandardHighlight);

        validationVisualizer.setDecoration(new IconValidationDecorator());
        Platform.runLater(() -> validationVisualizer.initVisualization(viewModel.fontSizeValidationStatus(), fontSize));
    }

    private void addColorCodeListener(TextField textInput, StringProperty colorCode, Rectangle displayBox, String standardColor) {
        // Set the color display box to standard color
        displayBox.fillProperty().setValue(Paint.valueOf(standardColor));

        // Add a listener that changes the color of the display and update the value used when saving the preferences
        // Uses input value is valid, standard color if not
        textInput.textProperty().addListener((observable, oldValue, newValue) -> {
            String inputString = textInput.textProperty().getValueSafe();
            if (inputString.matches(colorCodeRegex)) {
                colorCode.setValue(textInput.textProperty().getValue());
                displayBox.fillProperty().setValue(Paint.valueOf(inputString));
            } else {
                colorCode.setValue(standardColor);
                displayBox.fillProperty().setValue(Paint.valueOf(standardColor));
            }
        });
    }
}
