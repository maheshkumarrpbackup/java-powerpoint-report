package com.autonomy.abc.selenium.find.results;

import com.autonomy.abc.selenium.find.Container;
import com.autonomy.abc.selenium.query.QueryResultsPage;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class ResultsView extends AppElement implements QueryResultsPage {
    public ResultsView(final WebElement element, final WebDriver driver) {
        super(element, driver);
    }

    public ResultsView(final WebDriver driver) {
        this(Container.currentTabContents(driver).findElement(By.className("middle-container")), driver);
    }

    //bad because it assumes it exists in both which it doesn't
    public void goToListView() {
        findElement(By.cssSelector("[data-tab-id='list']")).click();
        new WebDriverWait(getDriver(), 15).until(ExpectedConditions.visibilityOf(findElement(By.cssSelector(".results-list-container"))));
    }

    public int getResultsCount() {
        return Integer.valueOf(findElement(By.className("total-results-number")).getText());
    }

    public WebElement correctedQuery() { return findElement(By.className("corrected-query"));}

    @Override
    public WebElement errorContainer() {
        return findElement(By.cssSelector(".well"));
    }

    public boolean errorContainerShown() {
        List<WebElement> errorWells = findElements(By.cssSelector(".well"));
        return errorWells.size()>0 && errorWells.get(0).isDisplayed();
    }

    public List<String> getResultTitles() {
        final List<String> titles = new ArrayList<>();
        for(final FindResult result : getResults()) {
            titles.add(result.getTitleString());
        }
        return titles;
    }

    public WebElement resultsDiv() {
        return getDriver().findElement(By.className("results"));
    }

    public boolean loadingIndicatorPresent() {
        return !findElements(By.cssSelector(".main-results-content .loading-spinner:not(.hide)")).isEmpty();
    }

    public List<FindResult> getResults() {
        final List<FindResult> results = new ArrayList<>();
        for(final WebElement result : findElements(By.className("main-results-container"))) {
            results.add(new FindResult(result, getDriver()));
        }
        return results;
    }

    public List<FindResult> getResults(final int maxResults) {
        final List<FindResult> results = getResults();
        return results.subList(0, Math.min(maxResults, results.size()));
    }

    public FindResult getResult(final int i) {
        return new FindResult(findElement(By.cssSelector(".main-results-container:nth-of-type(" + i + ')')), getDriver());
    }

    public List<String> getDisplayedDocumentsDocumentTypes() {
        final List<String> documentTypes = new ArrayList<>();
        for(final FindResult result : getResults()) {
            documentTypes.add(result.icon().getAttribute("class"));
        }
        return documentTypes;
    }

    public void waitForResultsToLoad() {
        new WebDriverWait(getDriver(), 50).until(new LoadedCondition());
    }

    public FindResult searchResult(final int searchResultNumber) {
        return new FindResult(findElement(By.cssSelector(".results div:nth-child(" + searchResultNumber + ')')), getDriver());
    }

    public List<WebElement> noMoreResultsMessage() {
        return findElements(By.xpath("//div[@class = 'result-message' and contains(text(),'No more results')]"));
    }

    private static class LoadedCondition implements ExpectedCondition<Boolean> {
        @Override
        public Boolean apply(final WebDriver input) {
            return resultsLoaded(input);
        }

        private boolean resultsLoaded(final WebDriver driver) {
            int loadingSpinners = driver.findElements(By.cssSelector(".loading-spinner:not(.hide)")).size();
            loadingSpinners -= driver.findElements(By.cssSelector(".hide .loading-spinner")).size();

            return  loadingSpinners == 0
                    && !driver.findElements(By.cssSelector(".results > div")).isEmpty()
                    && (driver.findElements(By.cssSelector(".results-view-error.hide")).isEmpty()
                    || !driver.findElements(By.cssSelector(".main-results-list.results .result-message")).isEmpty()
                    || !driver.findElements(By.cssSelector(".main-results-list.results .main-results-container")).isEmpty());
        }
    }
}
