package api;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import pojo.User;

import static org.hamcrest.Matchers.*;

public class AuthApiTest {
    private String accessToken;

    @After
    public void tearDown() {
        if (accessToken != null) {
            AuthHelper.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Успешный логин пользователя")
    public void loginUserSuccess() {
        String email = "login_user_" + System.currentTimeMillis() + "@test.ru";
        User user = new User(email, "password", "Login User");
        AuthHelper.registerUser(user);

        Response response = AuthHelper.loginUser(user);
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue());

        accessToken = AuthHelper.getAccessToken(response);
    }

    @Test
    @DisplayName("Логин с неверным паролем")
    public void loginUserWithWrongPasswordFail() {
        String email = "wrong_pass_user_" + System.currentTimeMillis() + "@test.ru";
        User user = new User(email, "password", "Wrong Pass User");
        AuthHelper.registerUser(user);

        User wrongUser = new User(user.getEmail(), "wrong_password", user.getName());
        AuthHelper.loginUser(wrongUser)
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));

        accessToken = AuthHelper.getAccessToken(AuthHelper.loginUser(user));
    }
}