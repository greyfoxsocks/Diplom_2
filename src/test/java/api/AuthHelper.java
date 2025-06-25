package api;

import io.restassured.response.Response;
import pojo.User;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

public class AuthHelper {
    public static final String BASE_URI = "https://stellarburgers.nomoreparties.site";
    private static final String REGISTER_ENDPOINT = "/api/auth/register";
    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String USER_ENDPOINT = "/api/auth/user";

    public static Response registerUser(User user) {
        return given()
                .baseUri(BASE_URI)
                .header("Content-Type", "application/json")
                .body(user)
                .post(REGISTER_ENDPOINT);
    }

    public static Response loginUser(User user) {
        return given()
                .baseUri(BASE_URI)
                .header("Content-Type", "application/json")
                .body(user)
                .post(LOGIN_ENDPOINT);
    }

    public static String getAccessToken(Response response) {
        String token = response.path("accessToken");
        return token != null ? token.replace("Bearer ", "") : null;
    }

    public static void deleteUser(String accessToken) {
        if (accessToken != null && !accessToken.isEmpty()) {
            given()
                    .baseUri(BASE_URI)
                    .header("Authorization", "Bearer " + accessToken)
                    .delete(USER_ENDPOINT)
                    .then()
                    .statusCode(anyOf(is(200), is(202)));
        }
    }
}