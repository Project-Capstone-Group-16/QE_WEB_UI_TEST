package starter.stepdefinitions;

import com.github.javafaker.Faker;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;
import net.serenitybdd.screenplay.rest.interactions.*;
import org.json.simple.JSONObject;
import starter.data.Admin;
import starter.data.User;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class APIStepDefinitions {

    String baseURL = "http://143.198.92.250:8080";

    User user = new User();
    Admin admin = new Admin();

    @Given("{actor} call an api {string} with method {string} with payload below")
    public void callApi(Actor actor, String path, String method, DataTable table) {
        actor.whoCan(CallAnApi.at(baseURL));

        // Create request body json instance
        JSONObject bodyRequest = new JSONObject();


        // Get headers
        List<List<String>> rowsList = table.asLists(String.class);
        List<String> headerList = rowsList.get(0);

        // Get values
        List<Map<String, String>> rowsMap = table.asMaps(String.class, String.class);
        Map<String, String> valueList = rowsMap.get(0);

        // Loop on every values and set value with key from header to request body
        for (int i = 0; i < valueList.size(); i++) {
            Faker faker = new Faker(new Locale("in-ID"));

            String key = headerList.get(i);

            //check if value correspond to random syntax
            switch (valueList.get(key)) {

                case "randomUserEmail" -> {
                    String randomEmail = faker.internet().emailAddress();
                    bodyRequest.put(key, randomEmail);
                    user.setUserEmail(randomEmail);
                }
                case "randomEmail" -> {
                    String randomEmail = faker.internet().emailAddress();
                    bodyRequest.put(key, randomEmail);
                    admin.setAdminEmail(randomEmail);
                }
                case "randomUserPassword" -> {
                    String randomPassword = faker.internet().password();
                    bodyRequest.put(key, randomPassword);
                    user.setUserPassword(randomPassword);
                }
                case "randomPassword" -> {
                    String randomPassword = faker.internet().password();
                    bodyRequest.put(key, randomPassword);
                    admin.setAdminPassword(randomPassword);
                }
                case "randomFullname" -> {
                    String randomFullname = faker.name().fullName();
                    bodyRequest.put(key, randomFullname);
                }

                case "userEmail" -> bodyRequest.put(key, user.getUserEmail());
                case "adminEmail" -> bodyRequest.put(key, admin.getAdminEmail());
                case "userPassword" -> bodyRequest.put(key, user.getUserPassword());
                case "adminPassword" -> bodyRequest.put(key, admin.getAdminPassword());
                default -> bodyRequest.put(key, valueList.get(key));
            }
        }

        switch (method) {
            case "GET":
                actor.attemptsTo(Get.resource(path));
                break;
            case "POST":
                actor.attemptsTo(Post.to(path).with(request -> request.header("Authorization", "Bearer " + user.getAuth()).body(bodyRequest).log().all()));
                break;
            case "PATCH":
                actor.attemptsTo(Patch.to(path));
                break;
            case "PUT":
                actor.attemptsTo(Put.to(path).with(request -> request.header("Authorization", "Bearer " + user.getAuth()).body(bodyRequest).log().all()));
                break;
            case "DELETE":
                actor.attemptsTo(Delete.from(path));
                break;
            default:
                throw new IllegalStateException("Unknown method");
        }
    }

    @Given("{actor} call an api {string} with method {string}")
    public void callApi(Actor actor, String path, String method) {
        actor.whoCan(CallAnApi.at(baseURL));

        switch (method) {
            case "GET":
                actor.attemptsTo(Get.resource(path));
                break;
            case "POST":
                actor.attemptsTo(Post.to(path));
                break;
            case "PUT":
                actor.attemptsTo(Put.to(path));
                break;
            case "PATCH":
                actor.attemptsTo(Patch.to(path));
                break;
            case "DELETE":
                actor.attemptsTo(Delete.from(path));
                break;
            default:
                throw new IllegalStateException("Unknown method");
        }
    }


    @Then("{actor} verify response is match with json schema {string}")
    public void userVerifyResponseIsMatchWithJsonSchema(Actor actor, String schema) {
        Response response = SerenityRest.lastResponse();
        response.then().body(matchesJsonSchemaInClasspath(schema));
    }

    @Then("{actor} verify status code is {int}")
    public void userVerifyStatusCodeIs(Actor actor, int statusCode) {
        Response response = SerenityRest.lastResponse();
        response.then().statusCode(statusCode).log().all();
    }

    @Then("{actor} admin get auth token")
    public void adminGetAuth(Actor actor) {
        Response response = SerenityRest.lastResponse();
        admin.setAuth(response.path("data.token"));

        System.out.println(admin.getAuth());
    }

    @Then("{actor} user get auth token")
    public void userGetAuth(Actor actor) {
        Response response = SerenityRest.lastResponse();
        user.setAuth(response.path("data.token"));
    }

    @Given("{actor} call api {string} with method {string}")
    public void userCallApiWithMethod(Actor actor, String path, String method) {
        actor.whoCan(CallAnApi.at(baseURL));

        Faker faker = new Faker(new Locale("in-ID"));
        String location = faker.address().city();
        String nameWarehouse = "Inventron " + location;

        File file = new File(System.getProperty("user.dir") + "/src/test/resources/img/warehouse.jpg");

        switch (method) {
            case "GET" ->
                    actor.attemptsTo(Get.resource(path).with(request -> request.header("Authorization", "Bearer " + admin.getAuth())));

            case "POST" -> actor.attemptsTo(Post.to(path).with(request -> request.contentType("multipart/form-data").header("Authorization", "Bearer " + admin.getAuth())
                    .multiPart("name", nameWarehouse).multiPart("location", location).multiPart("warehouse_image", file,"image/jpeg")));

            case "PUT" -> actor.attemptsTo(Put.to(path).with(request -> request.contentType("multipart/form-data").header("Authorization", "Bearer " + admin.getAuth())
                    .multiPart("name", nameWarehouse).multiPart("location", location).multiPart("status", "Not Available").multiPart("warehouse_image", file,"image/jpeg")));

            case "DELETE" -> actor.attemptsTo(Delete.from(path).with(request -> request.header("Authorization", "Bearer " + admin.getAuth())));

            default -> throw new IllegalStateException("Unknown method");
        }
    }
}
