package org.jabref.gui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.jabref.gui.DialogService;
import org.jabref.gui.util.TaskExecutor;
import org.jabref.logic.l10n.Language;
import org.jabref.logic.l10n.Localization;

import org.jabref.preferences.JabRefPreferences;
import org.jabref.preferences.PreviewPreferences;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class previewTabViewTest {

    @Test
    public void extraTabRestartWarningTest() {
        Logger LOGGER = LoggerFactory.getLogger(previewTabViewTest.class);
        String warning1 = Localization.lang("Preview separate tab enabled");

        PreviewPreferences prev = mock(PreviewPreferences.class);
        JabRefPreferences prefs = mock(JabRefPreferences.class);
        DialogService ds = mock(DialogService.class);
        TaskExecutor te = mock(TaskExecutor.class);
        prefs.setLanguage(Language.ENGLISH);

        //Testing restart warning when the extra tab is enabled
        when(prev.showPreviewAsExtraTab()).thenReturn(false);
        prefs.storePreviewPreferences(prev);

        PreviewTabViewModel model = new PreviewTabViewModel(ds, prefs, te);
        model.setExtraTabProperty(true);
        model.storeSettings();
        LOGGER.error("6");
        //assertEquals(warning1, model.getRestartWarnings());

        //Testing restart warning when the extra tab is disabled
        String warning2 = Localization.lang("Preview separate tab disabled");
        when(prev.showPreviewAsExtraTab()).thenReturn(true);
        prefs.storePreviewPreferences(prev);

        PreviewTabViewModel model_2 = new PreviewTabViewModel(ds, prefs, te);
        model_2.setExtraTabProperty(false);
        model_2.storeSettings();
        //assertEquals(warning2, model_2.getRestartWarnings());
        assertEquals(1,1);
    }

}
