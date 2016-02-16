package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;

public class PinPositionStep implements WizardStep<Integer> {
    private CreateNewPromotionsPage page;
    private int position;

    public PinPositionStep(CreateNewPromotionsPage page, int position) {
        this.page = page;
        this.position = position;
    }

    @Override
    public String getTitle() {
        return "Promotion details";
    }

    @Override
    public Integer apply() {
        for (int i=1; i<position; i++) {
            page.selectPositionPlusButton().click();
        }
        return page.positionInputValue();
    }
}
