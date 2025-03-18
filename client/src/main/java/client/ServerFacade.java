package client;

import com.google.gson.Gson;
import exception.ResponseException;
import model.*;
import requestsresults.*;

import java.io.*;
import java.net.*;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }


    public RegisterResult register(String username, String password, String email) throws ResponseException {
        var path = "/user";
        RegisterRequest request = new RegisterRequest(username, password, email);
        return this.makeRequest("POST", path, request, null, RegisterResult.class);
    }

    public LoginResult login(String username, String password) throws ResponseException {
        var path = "/session";
        LoginRequest request = new LoginRequest(username, password);
        return this.makeRequest("POST",path, request, null, LoginResult.class);
    }

    public void logout(String authToken) throws ResponseException {
        var path = "/session";
        this.makeRequest("DELETE", path, null, authToken, null);
    }

    public void clear() throws ResponseException {
        var path = "/db";
        this.makeRequest("DELETE", path, null, null, null);
    }

    public ListResult list(String authToken) throws ResponseException {
        var path = "/game";
        return this.makeRequest("GET", path, null, authToken, ListResult.class);
    }

    public CreateResult create(String gameName, String authToken) throws ResponseException{
        var path = "/game";
        CreateRequest request = new CreateRequest(authToken, gameName);
        return this.makeRequest("POST", path, request, authToken, CreateResult.class);
    }

    public void join(String playerColor, int gameID, String authToken) throws ResponseException {
        var path = "/game";
        JoinRequest request = new JoinRequest(authToken, playerColor, gameID);
        this.makeRequest("PUT", path, request, authToken, null);
    }

    private <T> T makeRequest(String method, String path, Object request, String authToken, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeBody(request, http, authToken);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }


    private static void writeBody(Object request, HttpURLConnection http, String authToken) throws IOException {
        if (request != null) {
            if (authToken != null) {
                http.addRequestProperty("Authorization", authToken);
            }
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    throw ResponseException.fromJson(respErr);
                }
            }

            throw new ResponseException(status, "other failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }


    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
