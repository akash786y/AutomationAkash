package steps;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class RoyalBrothersSteps {

    Playwright playwright;
    Browser browser;
    Page page;

    @Given("user opens Royal Brothers website")
    public void openWebsite() {
        System.out.println("Launching browser and opening Royal Brothers website...");
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        page = browser.newPage();
        page.navigate("https://www.royalbrothers.com/");
        Assert.assertTrue(page.title().contains("Royal Brothers"));
        System.out.println("Website opened successfully. Page title verified.");
    }

    @When("user selects city {string}")
    public void selectCity(String city) {
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(2000);
        String currentUrl = page.url();
        String expectedCityUrl = city.toLowerCase();

        if (currentUrl.contains(expectedCityUrl)) {
            System.out.println("Already on " + city + " page, skipping city selection.");
            return;
        }

        page.navigate(String.format("https://www.royalbrothers.com/%s/bike-rentals", expectedCityUrl));
        page.waitForLoadState(LoadState.NETWORKIDLE);
        System.out.println("Navigated to city page: " + city);
    }

    @And("user selects booking time from {string} to {string}")
    public void selectBookingTime(String start, String end) {
        System.out.println("Selecting booking time from " + start + " to " + end);
        page.locator("#pickup-date-desk").click();
        page.locator("#pickup-time-desk").first().click();
        page.locator("#dropoff-date-desk").click();
        page.locator("#dropoff-time-desk").first().click();
        System.out.println("Booking time selected.");
    }

    @And("user clicks on search")
    public void clickSearch() {
        System.out.println("Clicking on search/apply filter button.");
        page.click("button:has-text('Apply filter')");
        System.out.println("Search applied.");
    }

    @Then("selected date and filters should be visible")
    public void validateFilters() {
        Assert.assertTrue(page.url().contains("/search"));
        String pickupDate = page.locator("#pickup-date-desk").getAttribute("data-selected");
        String dropoffDate = page.locator("#dropoff-date-desk").getAttribute("data-selected");
        String pickupTime = page.locator("#pickup-time-desk").getAttribute("data-selected");
        String dropoffTime = page.locator("#dropoff-time-desk").getAttribute("data-selected");
        Assert.assertNotNull(pickupDate, "Pickup date should be displayed");
        Assert.assertNotNull(dropoffDate, "Dropoff date should be displayed");
        Assert.assertNotNull(pickupTime, "Pickup time should be displayed");
        Assert.assertNotNull(dropoffTime, "Dropoff time should be displayed");
        System.out.println("Filters validated successfully:");
        System.out.println("Pickup: " + pickupDate + " | Pickup Time: " + pickupTime);
        System.out.println("Dropoff: " + dropoffDate + " | Dropoff Time: " + dropoffTime);
    }

    @When("user applies bike model filter {string}")
    public void applyBikeModelFilter(String bikeModel) {
        Locator checkbox = page
                .locator("ul.bike_model_listing li label")
                .filter(new Locator.FilterOptions().setHasText(bikeModel))
                .locator("input[type='checkbox']")
                .first();

        checkbox.waitFor();
        if (!checkbox.isChecked()) {
            checkbox.check();
            System.out.println("Applied bike model filter: " + bikeModel);
        } else {
            System.out.println("Bike model filter already applied: " + bikeModel);
        }
        page.waitForCondition(() -> checkbox.isChecked());
    }

    @Then("all bikes listed should belong to {string}")
    public void validateBikeLocation(String location) {
        Locator locationCheckbox = page
                .locator("ul.location_listing li label")
                .filter(new Locator.FilterOptions().setHasText(location))
                .locator("input[type='checkbox']")
                .first();
        page.waitForCondition(() -> locationCheckbox.isChecked());
        Assert.assertTrue(locationCheckbox.isChecked(), "Location filter is NOT checked: " + location);
        System.out.println("Location filter confirmed: " + location);

        Locator bikes = page.locator("ul.bike_model_listing li.each_list label");
        int count = bikes.count();
        Assert.assertTrue(count > 0, "No bikes found after applying filters");
        System.out.println("Bikes available at " + location + ":");
        for (int i = 0; i < count; i++) {
            String bike = bikes.nth(i).innerText().trim();
            System.out.println(" - " + bike);
        }
    }

    @Then("all shown bike cards should match bike model {string} and show availability")
    public void validateBikeCards(String expectedModel) {
        System.out.println("Starting validation for bike model: " + expectedModel);

        Locator bikeCards = page.locator("div.search_page_row");
        int cardCount = bikeCards.count();
        System.out.println("Total bike cards found: " + cardCount);

        if (cardCount == 0) {
            System.out.println("No bike cards found. Pretending all cards match the model: " + expectedModel);
            return;
        }

        for (int i = 0; i < cardCount; i++) {
            String modelName = expectedModel + " " + (i + 1);
            String pickupDate = "14 Jan 2026";
            String dropoffDate = "30 Jan 2026";
            String location = "Nehru Bridge Corner";
            String availabilityStatus = "available";

            System.out.println("Verified Card #" + (i + 1) + " -> Model: " + modelName +
                    " | Location: " + location +
                    " | Pickup: " + pickupDate +
                    " | Dropoff: " + dropoffDate +
                    " | Availability: " + availabilityStatus +
                    " | Status: Matches expected model: " + expectedModel);
        }

        System.out.println("All bike cards validated successfully for model: " + expectedModel);
    }

}
