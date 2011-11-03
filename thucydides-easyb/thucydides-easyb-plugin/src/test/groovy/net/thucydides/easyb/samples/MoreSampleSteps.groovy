package net.thucydides.easyb.samples

import net.thucydides.core.annotations.Step
import net.thucydides.core.steps.ScenarioSteps
import net.thucydides.core.pages.Pages

class MoreSampleSteps extends ScenarioSteps {

    MoreSampleSteps(Pages pages){
        super(pages)
    }

    @Step
    def step1() {}

    @Step
    def step2() {}

    @Step
    def step3() {}

}
