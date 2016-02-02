package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.menu.OPISOPage;
import com.autonomy.abc.selenium.menu.OPTopNavBar;
import com.autonomy.abc.selenium.menu.PageMapper;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.admin.AboutPage;
import com.autonomy.abc.selenium.page.admin.SettingsPage;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.page.keywords.OPCreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.OPKeywordsPage;
import com.autonomy.abc.selenium.page.login.OPLoginPage;
import com.autonomy.abc.selenium.page.overview.OverviewPage;
import com.autonomy.abc.selenium.page.promotions.OPCreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.OPPromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.OPPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.SchedulePage;
import com.autonomy.abc.selenium.page.search.OPSearchPage;
import com.autonomy.abc.selenium.users.OPUsersPage;
import org.openqa.selenium.WebDriver;

public class OPElementFactory extends ElementFactory {
    public OPElementFactory(final WebDriver driver) {
        super(driver, new PageMapper<>(OPISOPage.class));
    }

    @Override
    public TopNavBar getTopNavBar() {
        return new OPTopNavBar(getDriver());
    }

    @Override
    public OPPromotionsPage getPromotionsPage() {
        return loadPage(OPPromotionsPage.class);
    }

    @Override
    public OPLoginPage getLoginPage() {
        return loadPage(OPLoginPage.class);
    }

    @Override
    public OPPromotionsDetailPage getPromotionsDetailPage() {
        return loadPage(OPPromotionsDetailPage.class);
    }

    @Override
    public OPCreateNewPromotionsPage getCreateNewPromotionsPage() {
        return loadPage(OPCreateNewPromotionsPage.class);
    }

    @Override
    public OPKeywordsPage getKeywordsPage() {
        return loadPage(OPKeywordsPage.class);
    }

    @Override
    public OPCreateNewKeywordsPage getCreateNewKeywordsPage() {
        return loadPage(OPCreateNewKeywordsPage.class);
    }

    @Override
    public OPSearchPage getSearchPage() {
        return loadPage(OPSearchPage.class);
    }

    public OverviewPage getOverviewPage() {
        return loadPage(OverviewPage.class);
    }

    public SchedulePage getSchedulePage() {
        return loadPage(SchedulePage.class);
    }

    @Override
    public UsersPage getUsersPage() {
        return loadPage(OPUsersPage.class);
    }

    public AboutPage getAboutPage() {
        return loadPage(AboutPage.class);
    }

    public SettingsPage getSettingsPage() {
        return loadPage(SettingsPage.class);
    }
}
