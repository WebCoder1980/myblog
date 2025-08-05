package org.myblog.users.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.myblog.users.dto.AppResponse;
import org.myblog.users.dto.request.LoginRequest;
import org.myblog.users.dto.request.SignupRequest;
import org.myblog.users.dto.response.JwtResponse;
import org.myblog.users.unit.service.PostgresTestContainerInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@Testcontainers
@ContextConfiguration(initializers = PostgresTestContainerInitializer.class)
public class UsersControllerIntegrationTests {
    final String USER_LOGIN = "maxsmg";
    final String USER_PASSWORD = "qweqwe";
    final String MODERATOR_LOGIN = "moderator";
    final String MODERATOR_PASSWORD = "moderatorPassword";
    final String ADMIN_LOGIN = "admin";
    final String ADMIN_PASSWORD = "adminPassword";

    final String URI_USERS_AUTH_LOGIN = "/users/auth/login";
    final String URI_USERS_AUTH_REGISTER = "/users/auth/register";
    final String URI_USERS_USER = "/users/user";
    final String URI_USERS_USER_ID = "/users/user/{id}";

    @Value("${TEST_PORT}")
    private Integer APP_PORT;

    @Autowired
    private IntegrationTestsUtil integrationTestsUtil;

    @Autowired
    private RestTemplate restTemplate;

    @Container
    private final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer("postgres:16.1")
            .withDatabaseName("myblog-test")
            .withUsername("postgres")
            .withPassword("postgres");

    @BeforeEach
    public void restAssuredInit() {
        RestAssured.port = APP_PORT;
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .build();
    }

    public String getUserToken() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(USER_LOGIN);
        loginRequest.setPassword(USER_PASSWORD);

        final String uri = String.format("http://127.0.0.1:%d%s", APP_PORT, URI_USERS_AUTH_LOGIN);

        ResponseEntity<AppResponse<JwtResponse>> response = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<>() {
                }
        );

        return response.getBody().getData().getToken();
    }

    public String getModeratorToken() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(MODERATOR_LOGIN);
        loginRequest.setPassword(MODERATOR_PASSWORD);

        final String uri = String.format("http://127.0.0.1:%d%s", APP_PORT, URI_USERS_AUTH_LOGIN);

        ResponseEntity<AppResponse<JwtResponse>> response = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<>() {
                }
        );

        return response.getBody().getData().getToken();
    }

    public String getAdminToken() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(ADMIN_LOGIN);
        loginRequest.setPassword(ADMIN_PASSWORD);

        final String uri = String.format("http://127.0.0.1:%d%s", APP_PORT, URI_USERS_AUTH_LOGIN);

        ResponseEntity<AppResponse<JwtResponse>> response = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<>() {
                }
        );

        return response.getBody().getData().getToken();
    }

    @Test
    public void checkPostgresStatus() {
        assertTrue(POSTGRES_CONTAINER.isRunning());
    }

    @Test
    public void post_authenticateUser_ok() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(USER_LOGIN);
        loginRequest.setPassword(USER_PASSWORD);

        given()
                .body(loginRequest)
            .when()
                .post(URI_USERS_AUTH_LOGIN)
            .then()
                .statusCode(200)
                .body("status", equalTo("OK"))
                .body("data.token", notNullValue())
                .body("data.type", equalTo("Bearer"))
                .body("data.id", equalTo(3))
                .body("data.username", equalTo("maxsmg"))
                .body("data.email", equalTo("maxsmg@myblog.org"))
                .body("data.roles", contains("ROLE_USER"))
                .body("errors", nullValue());
    }

    @Test
    public void post_authenticateUser_wrongLoginOrPassword() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("wrong");
        loginRequest.setPassword("wrong");

        given()
                .body(loginRequest)
            .when()
                .post(URI_USERS_AUTH_LOGIN)
            .then()
                .statusCode(400)
                .body("status", equalTo("ERROR"))
                .body("data", nullValue())
                .body("errors.general", contains("Bad credentials"));
    }

    @Test
    public void post_authenticateUser_emptyLoginAndPassword() {
        final String expectedBody = """
                {
                  "status": "ERROR",
                  "data": null,
                  "errors": {
                    "password": [
                      "must not be blank"
                    ],
                    "username": [
                      "must not be blank"
                    ]
                  }
                }
                """;

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("");
        loginRequest.setPassword("");

        given()
                .body(loginRequest)
            .when()
                .post(URI_USERS_AUTH_LOGIN)
            .then()
                .statusCode(400)
                .body(integrationTestsUtil.equalToJSON(expectedBody));
    }

    @Test
    public void post_registerUser_ok() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testUser");
        signupRequest.setEmail("testUserEmail@gmail.org");
        signupRequest.setPassword("testUserPassword");

        given()
                .body(signupRequest)
                .when()
                .post(URI_USERS_AUTH_REGISTER)
                .then()
                .statusCode(200)

                .body("status", equalTo("OK"))
                .body("data.id", equalTo(4))
                .body("data.username", equalTo("testUser"))
                .body("data.email", equalTo("testUserEmail@gmail.org"))

                .body("data.roles[0].id", equalTo(1))
                .body("data.roles[0].name", equalTo("ROLE_USER"))

                .body("errors", nullValue());
    }

    @Test
    public void post_registerUser_emptyData() {
        final String expectedBody = """
                {
                  "status": "ERROR",
                  "data": null,
                  "errors": {
                    "email": [
                      "must not be blank"
                    ],
                    "password": [
                      "must not be blank",
                      "size must be between 6 and 40"
                    ],
                    "username": [
                      "must not be blank",
                      "size must be between 3 and 20"
                    ]
                  }
                }
                """;

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("");
        signupRequest.setEmail("");
        signupRequest.setPassword("");

        given()
                .body(signupRequest)
                .when()
                .post(URI_USERS_AUTH_REGISTER)
                .then()
                .statusCode(400)
                .body(integrationTestsUtil.equalToJSON(expectedBody));
    }

    @Test
    public void get_getAll_guest_ok() {
        final String expectedBody = """
                {
                  "status": "ERROR",
                  "data": null,
                  "errors": {
                    "general": [
                      "Full authentication is required to access this resource"
                    ]
                  }
                }
                """;

        given()
                .when()
                .get(URI_USERS_USER)
                .then()
                .statusCode(401)
                .body(integrationTestsUtil.equalToJSON(expectedBody));
    }

    @Test
    public void get_getAll_user_ok() {
        final String expectedBody = """
                {
                   "status": "ERROR",
                   "data": null,
                   "errors": {
                     "general": [
                       "Access Denied"
                     ]
                   }
                 }
                """;

        given()
                .when()
                .header("Authorization", String.format("Bearer %s", getUserToken()))
                .get(URI_USERS_USER)
                .then()
                .statusCode(400)
                .body(integrationTestsUtil.equalToJSON(expectedBody));
    }

    @Test
    public void get_getAll_moderator_ok() {
        final String expectedBody = """
                  {
                    "status": "ERROR",
                    "data": null,
                    "errors": {
                      "general": [
                        "Access Denied"
                      ]
                    }
                  }
                """;

        given()
            .when()
                .header("Authorization", String.format("Bearer %s", getModeratorToken()))
                .get(URI_USERS_USER)
            .then()
                .statusCode(400)
                .body(integrationTestsUtil.equalToJSON(expectedBody));
    }

    @Test
    public void get_getAll_admin_ok() {
        given()
                .when()
                .header("Authorization", String.format("Bearer %s", getAdminToken()))
                .get(URI_USERS_USER)
                .then()
                .statusCode(200)
                .body("status", equalTo("OK"))

                .body("data[0].id", equalTo(1))
                .body("data[0].username", equalTo("admin"))
                .body("data[0].email", equalTo("admin@myblog.org"))
                .body("data[0].roles[0].id", equalTo(3))
                .body("data[0].roles[0].name", equalTo("ROLE_ADMIN"))

                .body("data[1].id", equalTo(2))
                .body("data[1].username", equalTo("moderator"))
                .body("data[1].email", equalTo("moderator@myblog.org"))
                .body("data[1].roles[0].id", equalTo(2))
                .body("data[1].roles[0].name", equalTo("ROLE_MODERATOR"))

                .body("data[2].id", equalTo(3))
                .body("data[2].username", equalTo("maxsmg"))
                .body("data[2].email", equalTo("maxsmg@myblog.org"))
                .body("data[2].roles[0].id", equalTo(1))
                .body("data[2].roles[0].name", equalTo("ROLE_USER"))

                .body("errors", nullValue());
    }

    @Test
    public void get_get_guest_ok() {
        final String expectedBody = """
                {
                  "status": "ERROR",
                  "data": null,
                  "errors": {
                    "general": [
                      "Full authentication is required to access this resource"
                    ]
                  }
                }
                """;

        given()
                .pathParam("id", 1)
                .when()
                .get(URI_USERS_USER_ID)
                .then()
                .statusCode(401)
                .body(integrationTestsUtil.equalToJSON(expectedBody));
    }

    @Test
    public void get_get_user_ok() {
        final String expectedBody = """
                {
                  "status": "ERROR",
                  "data": null,
                  "errors": {
                    "general": [
                      "Access Denied"
                    ]
                  }
                }
                """;

        given()
                .header("Authorization", String.format("Bearer %s", getUserToken()))
                .pathParam("id", 1)
                .when()
                .get(URI_USERS_USER_ID)
                .then()
                .statusCode(400)
                .body(integrationTestsUtil.equalToJSON(expectedBody));
    }

    @Test
    public void get_get_moderator_ok() {
        final String expectedBody = """
                {
                  "status": "ERROR",
                  "data": null,
                  "errors": {
                    "general": [
                      "Access Denied"
                    ]
                  }
                }
                """;

        given()
                .header("Authorization", String.format("Bearer %s", getModeratorToken()))
                .pathParam("id", 1)
                .when()
                .get(URI_USERS_USER_ID)
                .then()
                .statusCode(400)
                .body(integrationTestsUtil.equalToJSON(expectedBody));
    }

    @Test
    public void get_get_admin_ok() {
        given()
                .header("Authorization", String.format("Bearer %s", getAdminToken()))
                .pathParam("id", 1)
                .when()
                .get(URI_USERS_USER_ID)
                .then()
                .statusCode(200)

                .body("status", equalTo("OK"))

                .body("data.id", equalTo(1))
                .body("data.username", equalTo("admin"))
                .body("data.email", equalTo("admin@myblog.org"))
                .body("data.roles[0].id", equalTo(3))
                .body("data.roles[0].name", equalTo("ROLE_ADMIN"))

                .body("errors", nullValue());
    }

    @Test
    public void get_get_admin_userWasNotFound() {
        final String expectedBody = """
                {
                   "status": "ERROR",
                   "data": null,
                   "errors": {
                     "general": [
                       "User was not found"
                     ]
                   }
                }
                """;

        given()
                .header("Authorization", String.format("Bearer %s", getAdminToken()))
                .pathParam("id", 999)
                .when()
                .get(URI_USERS_USER_ID)
                .then()
                .statusCode(400)
                .body(integrationTestsUtil.equalToJSON(expectedBody));
    }
}