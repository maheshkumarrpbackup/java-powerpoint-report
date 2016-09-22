package com.autonomy.abc.selenium.find.save;

import com.autonomy.abc.selenium.find.application.BIIdolFind;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.comparison.ComparisonModal;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SavedSearchService {
    private final BIIdolFindElementFactory elementFactory;
    private static final Logger LOGGER = LoggerFactory.getLogger(SavedSearchService.class);

    public SavedSearchService(final BIIdolFind find) {
        elementFactory = find.elementFactory();
    }

    public void saveCurrentAs(final String searchName, final SearchType type){
        Waits.loadOrFadeWait();
        nameSavedSearch(searchName,type).confirmSave();
    }

    public void renameCurrentAs(final String newSearchName) {
        final SearchOptionsBar optionsBar = elementFactory.getSearchOptionsBar();
        Waits.loadOrFadeWait();

        optionsBar.renameButton().click();
        optionsBar.searchTitleInput().setValue(newSearchName);
        optionsBar.confirmSave();
    }

    public SearchOptionsBar nameSavedSearch(final String searchName,final SearchType type){
        final SearchOptionsBar options = elementFactory.getSearchOptionsBar();
        options.saveAsButton(type).click();
        options.searchTitleInput().setValue(searchName);
        return options;
    }

    public void openNewTab() {
        elementFactory.getSearchTabBar().newTabButton().click();
        elementFactory.getTopicMap().waitForMapLoaded();
        elementFactory.getSearchTabBar().hoverOnTab(0);
    }

    public void deleteAll() {
        LOGGER.info("Deleting all tabs");

        for (final SearchTab tab : elementFactory.getSearchTabBar()) {
            tab.activate();
            deleteCurrentSearch();
        }
    }

    public void deleteCurrentSearch() {
        final SearchOptionsBar options = elementFactory.getSearchOptionsBar();
        options.openDeleteModal();
        options.confirmModalOperation();
        Waits.loadOrFadeWait();
    }

    public void compareCurrentWith(final String savedSearchName) {
        final ComparisonModal modal = elementFactory.getFindPage().openCompareModal();
        modal.select(savedSearchName);
        modal.compareButton().click();
        modal.waitForComparisonToLoad();
    }

    /**
     * Click the reset button for the current query, then click to confirm in the modal.
     */
    public void resetCurrentQuery() {
        final SearchOptionsBar options = elementFactory.getSearchOptionsBar();
        options.openResetModal();
        options.confirmModalOperation();
        Waits.loadOrFadeWait();
    }
}
