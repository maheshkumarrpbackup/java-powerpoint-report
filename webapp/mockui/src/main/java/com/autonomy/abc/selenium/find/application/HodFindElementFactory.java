package com.autonomy.abc.selenium.find.application;

import com.autonomy.abc.selenium.find.login.FindHasLoggedIn;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import org.openqa.selenium.WebDriver;

public abstract class HodFindElementFactory extends FindElementFactory {
    protected HodFindElementFactory(WebDriver driver) {
        super(driver);
    }

    @Override
    public LoginPage getLoginPage() {
        return new HSOLoginPage(getDriver(), new FindHasLoggedIn(getDriver()));
    }


}
