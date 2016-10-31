package com.autonomy.abc.find;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.filters.GraphFilterContainer;
import com.autonomy.abc.selenium.find.filters.IdolFilterPanel;
import com.autonomy.abc.selenium.find.filters.ListFilterContainer;
import com.autonomy.abc.selenium.find.results.ResultsView;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang3.text.WordUtils;
import org.junit.Before;
import org.junit.Test;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

@Role(UserRole.BIFHI)
public class IdolFilterITCase extends IdolFindTestBase {
    private FindService findService;
    private IdolFindPage findPage;

    public IdolFilterITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findService = getApplication().findService();
        findPage = getElementFactory().getFindPage();
        if(!findPage.footerLogo().isDisplayed()) {
            findPage.goToListView();
        }
    }

    private ResultsView searchAndWait(final String searchTerm) {
        final ResultsView results = findService.search(searchTerm);
        findPage.waitForParametricValuesToLoad();
        return results;
    }

    //META-FILTERING
    @Test
    @ResolvedBug("FIND-122")
    public void testSearchForParametricFieldName() {
        findService.search("face");
        findPage.waitForParametricValuesToLoad();
        final IdolFilterPanel filters = filters();

        final int goodFieldIndex = 2;
        final ListFilterContainer goodField = filters.parametricField(goodFieldIndex);

        final String goodFieldName = goodField.filterCategoryName();
        final int badFieldIndex = getContainerWithoutThatFilter(goodFieldName, goodFieldIndex);

        final String badFieldName = filters.parametricField(badFieldIndex).filterCategoryName();
        final String goodFieldValue = goodField.getFilterNames().get(0);

        filters.collapseAll();
        Waits.loadOrFadeWait();

        filters.searchFilters(goodFieldName);

        Waits.loadOrFadeWait();

        //goodFieldName all capitals -> needs to be each word capitalized.
        final String goodNameGoodFormat = WordUtils.capitalize(goodFieldName.toLowerCase());

        verifyThat(badFieldName + " is (correctly) not visible", !filters.parametricContainerIsPresent(badFieldName));
        assertThat(goodFieldName + " container is correctly shown", filters.parametricContainerIsPresent(goodNameGoodFormat));
        verifyThat(goodFieldValue + " is also shown", filters.parametricContainer(goodNameGoodFormat).getFilterNames().get(0), is(goodFieldValue));
    }

    private int getContainerWithoutThatFilter(final String target, final int alreadyUsedIndex) {
        IdolFilterPanel filters = filters();
        final int max = filters.parametricFieldContainers().size() - 1;
        int index = 0;

        while(filters.containerContainsFilter(target, index) && index <= max) {
            if(index == (alreadyUsedIndex - 1)) {
                index+=2;
            }
            index++;
        }
        return index;
    }

    @Test
    public void testSearchForParametricFieldValue() {
        findService.search("face");

        int index = filters().nonZeroParamFieldContainer(0);
        final ListFilterContainer goodField = filters().parametricField(index);
        final String goodFieldName = goodField.filterCategoryName();
        final String badFieldValue = goodField.getFilterNames().get(0);
        final String goodFieldValue = goodField.getFilterNames().get(1);

        filters().searchFilters(goodFieldValue);

        Waits.loadOrFadeWait();

        assertThat(filters().parametricField(0).filterCategoryName(), is(goodFieldName));
        assertThat(filters().parametricField(0).getFilterNames().get(0), not(badFieldValue));
        assertThat(filters().parametricField(0).getFilterNames().get(0), is(goodFieldValue));
    }

    @Test
    public void testSearchForNonExistentFilter() {
        findService.search("face");

        filters().searchFilters("garbageasfsefeff");
        assertThat(filters().getErrorMessage(), is("No filters matched"));

        filters().clearMetaFilter();
        findPage.waitUntilDatabasesLoaded();
        assertThat(filters().getErrorMessage(), isEmptyOrNullString());
    }

    @Test
    @Role(UserRole.BOTH)
    public void testNumericWidgetsDefaultCollapsed() {
        findService.search("swim");

        for(GraphFilterContainer container : filters().graphContainers()) {
            verifyThat("Widget is collapsed", container.isCollapsed());
        }
    }

    @Test
    public void testParametricFiltersOpenWhenMatchingFilter() {
        searchAndWait("haven");
        final IdolFilterPanel filterPanel = filters();
        int index = filterPanel.nonZeroParamFieldContainer(0);

        final String firstValue = filterPanel.parametricField(index).getFilterNames().get(0);

        verifyThat(filterPanel.parametricField(index).isCollapsed(), is(true));

        filterPanel.searchFilters(firstValue);
        verifyThat(filterPanel.parametricField(index).isCollapsed(), is(false));

        filterPanel.clearMetaFilter();
        verifyThat(filterPanel.parametricField(index).isCollapsed(), is(true));
    }

    @Test
    public void testParametricFilterRemembersStateWhenMetaFiltering() {
        searchAndWait("haven");
        final IdolFilterPanel filterPanel = filters();

        int index = filterPanel.nonZeroParamFieldContainer(0);
        final String firstValue = filterPanel.parametricField(index).getFilterNames().get(0);

        filterPanel.parametricField(index).expand();
        verifyThat(filterPanel.parametricField(index).isCollapsed(), is(false));

        filterPanel.searchFilters(firstValue);
        verifyThat(filterPanel.parametricField(index).isCollapsed(), is(false));

        filterPanel.clearMetaFilter();
        verifyThat(filterPanel.parametricField(index).isCollapsed(), is(false));
    }

    @Test
    public void testIndexesOpenWhenMatchingMetaFilter() {
        searchAndWait("haven");
        final IdolFilterPanel filterPanel = filters();

        final ListFilterContainer indexesTreeContainer = filterPanel.indexesTreeContainer();
        final IndexesTree indexes = filterPanel.indexesTree();
        indexesTreeContainer.expand();

        final String firstValue = indexes.allIndexes().getIndex(0).getName();

        verifyThat(indexesTreeContainer.isCollapsed(), is(false));

        filterPanel.searchFilters(firstValue);
        verifyThat(indexesTreeContainer.isCollapsed(), is(false));

        filterPanel.clearMetaFilter();
        verifyThat(indexesTreeContainer.isCollapsed(), is(false));
    }

    @Test
    public void testIndexesRememberStateWhenMetaFiltering() {
        searchAndWait("haven");
        final IdolFilterPanel filterPanel = filters();

        findPage.waitUntilDatabasesLoaded();
        final ListFilterContainer indexesTreeContainer = filterPanel.indexesTreeContainer();
        final IndexesTree indexes = filterPanel.indexesTree();
        indexesTreeContainer.expand();

        final String firstValue = indexes.allIndexes().getIndex(0).getName();

        indexesTreeContainer.collapse();
        verifyThat(indexesTreeContainer.isCollapsed(), is(true));


        filterPanel.searchFilters(firstValue);
        verifyThat(indexesTreeContainer.isCollapsed(), is(false));

        filterPanel.clearMetaFilter();
        verifyThat(indexesTreeContainer.isCollapsed(), is(true));
    }

    private IdolFilterPanel filters() {
        return getElementFactory().getFilterPanel();
    }

}
