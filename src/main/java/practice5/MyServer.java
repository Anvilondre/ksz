package practice5;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import practice4.Unit;
import practice4.Database;
import practice4.User;

import javax.crypto.spec.SecretKeySpec;

public class MyServer {

    private static final byte[] API_KEY_SECRET_BYTES = "IReallyNeedZaraxBoodLasochka".getBytes(StandardCharsets.UTF_8);
    private static final String PATH = "/api/good/";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8888), 0);
        server.start();
        ObjectMapper objectMapper = new ObjectMapper();

        Database database = new Database();
        database.initialize();
        
        database.insertUser(new User("myUser1", "myPassword1"));
        database.insertUser(new User("myUser2", "myPassword2"));
        
        database.insertProduct(new Unit(100421, "whisky",29.99,69));
        database.insertProduct(new Unit(123512, "beer",2.99,1000));
        database.insertProduct(new Unit(312984, "coffee",10.49,24));
        database.insertProduct(new Unit(519284, "cranberry",1.49,12.9));

        Authenticator authenticator = new Authenticator() {
            @Override
            public Result authenticate(HttpExchange exchange) {
                String jwt = exchange.getRequestHeaders().getFirst("Authorization");
                if(jwt != null) {
                    String login = getUserLoginFromJWT(jwt);
                    User user = database.getUserByLogin(login);
                    if(user != null)
                        return new Success(new HttpPrincipal(login,"admin"));
                }
                return new Failure(403);
            }
        };

        server.createContext("/", exchange -> {
            byte[] response = "{\"status\": \"ok\" }".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });

        server.createContext("/login", exchange -> {
            if (exchange.getRequestMethod().equals("POST")) {
                User user = objectMapper.readValue(exchange.getRequestBody(), User.class);
                User dbUser= database.getUserByLogin(user.getLogin());
                if(dbUser != null)
                {
                    if(dbUser.getPassword().equals(user.getPassword()))
                    {
                        String jwt=createJWT(dbUser.getLogin());
                        System.out.println(getUserLoginFromJWT(jwt));
                        exchange.getResponseHeaders().set("Authorization", jwt);
                        exchange.sendResponseHeaders(200, 0);
                    }
                    else
                        exchange.sendResponseHeaders(401, 0);
                } else
                    exchange.sendResponseHeaders(401, 0);
            } else
                exchange.sendResponseHeaders(405, 0);
            exchange.close();
        });

        server.createContext("/api/good/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String requestString = path.substring(PATH.length());
            int requestId = 0;

            try{
                requestId = Integer.parseInt(requestString);
            } catch (NumberFormatException e) {
                exchange.sendResponseHeaders(404, 0);
                exchange.close();
            }

            Unit selectedProduct = database.getProductBySku(requestId);

            if (exchange.getRequestMethod().equals("GET")) {
                if (selectedProduct != null) {
                    byte[] response = objectMapper.writeValueAsBytes(selectedProduct);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.length);
                    exchange.getResponseBody().write(response);
                } else
                    exchange.sendResponseHeaders(404, 0);
            }

            else if (exchange.getRequestMethod().equals("POST")){
                Unit updatedProduct = objectMapper.readValue(exchange.getRequestBody(), Unit.class);

                if (updatedProduct != null && selectedProduct != null) {
                    if(updatedProduct.getPrice() > 0 && updatedProduct.getAmount() > 0) {
                        database.updateProductByValuesAndSku(selectedProduct, updatedProduct);
                        byte[] response = "{\"status\": \"No Content\" }".getBytes(StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(204, response.length);
                        exchange.getResponseBody().write(response);
                    } else
                        exchange.sendResponseHeaders(409, 0);
                } else
                    exchange.sendResponseHeaders(404, 0);
            }

            else if (exchange.getRequestMethod().equals("DELETE")){
                if(selectedProduct != null){
                    database.deleteProductByValuesAndSku(selectedProduct);
                    byte[] response = "{\"status\": \"No Content\" }".getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(204, response.length);
                    exchange.getResponseBody().write(response);
                } else
                    exchange.sendResponseHeaders(404, 0);
            } else
                exchange.sendResponseHeaders(404, 0);
            exchange.close();
        }).setAuthenticator(authenticator);


        server.createContext("/api/good", exchange -> {
            if (exchange.getRequestMethod().equals("PUT")) {
                Unit createdProduct = objectMapper.readValue(exchange.getRequestBody(), Unit.class);

                if(createdProduct.getPrice() > 0 && createdProduct.getAmount() > 0){
                    int createdId = database.insertProduct(createdProduct).getSku();
                    byte[] response = ("{ \"status\": \"Created\",  \"product-id\": \""+ createdId + "\" }").getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(201, response.length);
                    exchange.getResponseBody().write(response);
                } else
                    exchange.sendResponseHeaders(409, 0);
            } else
                exchange.sendResponseHeaders(404, 0);
            exchange.close();
        }).setAuthenticator(authenticator);
    }

    private static String createJWT(String login) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        Key signingKey = new SecretKeySpec(API_KEY_SECRET_BYTES, signatureAlgorithm.getJcaName());
        return Jwts
               .builder()
               .setIssuedAt(new Date())
               .setSubject(login)
               .signWith(signingKey, signatureAlgorithm)
               .compact();
    }

    private static String getUserLoginFromJWT(String jwt) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        Key signingKey = new SecretKeySpec(API_KEY_SECRET_BYTES, signatureAlgorithm.getJcaName());
        return  Jwts
                .parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(jwt).getBody()
                .getSubject();
    }

}