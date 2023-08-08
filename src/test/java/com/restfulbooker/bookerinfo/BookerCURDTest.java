package com.restfulbooker.bookerinfo;

import com.restfulbooker.constants.EndPoints;
import com.restfulbooker.model.AuthPojo;
import com.restfulbooker.model.BookingDates;
import com.restfulbooker.model.RestPojo;
import com.restfulbooker.testbase.TestBase;
import com.restfulbooker.utils.TestUtils;
import io.restassured.http.ContentType;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.core.annotations.Title;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static org.hamcrest.Matchers.hasValue;

@RunWith(SerenityRunner.class)
public class BookerCURDTest extends TestBase {
    static String firstName = "PrimUser" + TestUtils.getRandomValue();
    static String updateFirstName = "Update" + TestUtils.getRandomValue();
    static String lastName = "Testing" + TestUtils.getRandomValue();
    static String additionalNeeds = "Breakfast";
    static int bookingId;

    static String token;


    @Title("This will return token")
    @Test
    public void test001() {
         AuthPojo authPojo = new AuthPojo();
        authPojo.setUserName("admin");
        authPojo.setPassword("password123");

        token = SerenityRest.given()
                .contentType(ContentType.JSON)
                .when()
                .body(authPojo)
                .post("/auth")
                .then().log().all().statusCode(200)
                .extract().path("token");

        System.out.println("Token :" + token);
        Assert.assertNotNull(token);

    }

    @Title("This will create booking")
    @Test()
    public void test002() {
        System.out.println("====================" + token);

         RestPojo restPojo= new RestPojo();
        restPojo.setFirstname(firstName);
        restPojo.setLastname(lastName);
        restPojo.setTotalprice(111);
        restPojo.setDepositpaid(true);
        BookingDates bookingdates = new BookingDates();
        bookingdates.setCheckin("");
        bookingdates.setCheckout("");
        restPojo.setBookingdates(bookingdates);
        restPojo.setAdditionalneeds(additionalNeeds);


        bookingId = SerenityRest.given()
                .contentType(ContentType.JSON)
                .when()
                .body(restPojo)
                .post("/booking")
                .then().log().all().statusCode(200)
                .extract()
                .path("bookingid");

        System.out.println("bookingId :" + bookingId);
        Assert.assertNotNull(bookingId);

    }

    @Title("This will update booking with firstname")
    @Test
    public void test003() {

        RestPojo bookingPojo = new RestPojo();
        bookingPojo.setFirstname(updateFirstName);
        bookingPojo.setLastname(lastName);
        bookingPojo.setTotalprice(123);
        bookingPojo.setDepositpaid(true);
        BookingDates bookingdates = new BookingDates();
        bookingdates.setCheckin("12/12/2023");
        bookingdates.setCheckout("10/12/2024");
        bookingPojo.setBookingdates(bookingdates);
        bookingPojo.setAdditionalneeds(additionalNeeds);


        String updatedFirstNameResult = SerenityRest.given()
                .headers("Content-Type", "application/json", "Cookie", "token=" + token)
                .pathParam("bookingID", bookingId)
                .body(bookingPojo)
                .when()
                .put("/booking" + EndPoints.UPDATE_BOOKING_BY_ID)
                .then().log().all().statusCode(200)
                .extract()
                .path("firstname");


        System.out.println("updatedFirstNameResult -- " + updatedFirstNameResult);
        Assert.assertEquals(updateFirstName, updatedFirstNameResult);
    }

    @Title("This will fetch booking details by booking id")
    @Test
    public void test004() {

        HashMap<String, Object> bookingMap = SerenityRest.given()
                .headers("Content-Type", "application/json", "Cookie", "token=" + token)
                .pathParam("bookingID", bookingId)
                .when()
                .get("/booking" + EndPoints.GET_SINGLE_BOOKING_BY_ID)
                .then().statusCode(200)
                .extract()
                .path("");

        System.out.println("updateFirstName - " + updateFirstName);
        System.out.println("lastName - " + lastName);
        System.out.println("additionalNeeds - " + additionalNeeds);
        Assert.assertThat(bookingMap, hasValue(updateFirstName));
        Assert.assertThat(bookingMap, hasValue(lastName));
        Assert.assertThat(bookingMap, hasValue(additionalNeeds));

    }

    @Title("This will delete booking")
    @Test
    public void test005() {

        SerenityRest.given()
                .headers("Content-Type", "application/json", "Cookie", "token=" + token)
                .pathParam("bookingID", bookingId)
                .when()
                .delete("/booking" + EndPoints.DELETE_BOOKING_BY_ID)
                .then()
                .statusCode(201);

        SerenityRest.given()
                .headers("Content-Type", "application/json", "Cookie", "token=" + token)
                .pathParam("bookingID", bookingId)
                .when()
                .get("/booking" + EndPoints.GET_SINGLE_BOOKING_BY_ID)
                .then()
                .statusCode(404);

    }
}
