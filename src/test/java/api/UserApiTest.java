package api;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import pojo.User;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;

@DisplayName("API тесты для создания пользователя")
public class UserApiTest {
    private String accessToken;

    @After
    public void tearDown() {
        if (accessToken != null) {
            AuthHelper.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    @Description("Успешная регистрация нового пользователя")
    public void createUniqueUserSuccessTest() {
        User user = new User("unique_user_" + System.currentTimeMillis() + "@test.ru", "password", "Username");
        Response response = AuthHelper.registerUser(user);

        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true));

        accessToken = AuthHelper.getAccessToken(response);
    }

    @Test
    @DisplayName("Создание уже зарегистрированного пользователя")
    @Description("Попытка регистрации пользователя с уже существующим email")
    public void createDuplicateUserFailTest() {
        String email = "duplicate_" + System.currentTimeMillis() + "@test.ru";
        User user = new User(email, "password", "Username");

        // Первая регистрация
        Response firstRegister = AuthHelper.registerUser(user);
        firstRegister.then().statusCode(SC_OK);
        accessToken = AuthHelper.getAccessToken(firstRegister);

        // Вторая регистрация с тем же email
        Response response = AuthHelper.registerUser(user);
        response.then()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без пароля")
    @Description("Попытка регистрации без указания пароля")
    public void createUserWithoutPasswordFailTest() {
        User user = new User("invalid@test.ru", null, "Username");
        checkRequiredFieldError(user);
    }

    @Test
    @DisplayName("Создание пользователя без email")
    @Description("Попытка регистрации без указания email")
    public void createUserWithoutEmailFailTest() {
        User user = new User(null, "password", "Username");
        checkRequiredFieldError(user);
    }

    @Test
    @DisplayName("Создание пользователя без имени")
    @Description("Попытка регистрации без указания имени")
    public void createUserWithoutNameFailTest() {
        User user = new User("invalid@test.ru", "password", null);
        checkRequiredFieldError(user);
    }

    private void checkRequiredFieldError(User user) {
        AuthHelper.registerUser(user)
                .then()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }
}