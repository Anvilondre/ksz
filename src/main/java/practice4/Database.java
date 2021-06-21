package practice4;

import java.security.InvalidParameterException;
import java.sql.*;

public class Database {
    private Connection conn;

    public void initialize(){
        try{
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite::memory:");

            PreparedStatement statement = conn.prepareStatement(
                    "create table if not exists 'product' (" +
                            "'sku' INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "'name' text," +
                            "'price' double," +
                            "'amount' double)");
            statement.executeUpdate();
            statement.close();

            statement = conn.prepareStatement(
                    "create table if not exists 'users'  (" +
                            "'id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "'login' text unique, " +
                            "'password' text)");
            statement.executeUpdate();
            statement.close();

        }
        catch(ClassNotFoundException e){
            System.out.println("No JDBC driver");
            e.printStackTrace();
            System.exit(0);
        }catch (SQLException e){
            System.out.println("Incorrect SQL");
            e.printStackTrace();
        }     
    }

    public Unit insertProduct(Unit product){
        try{
            PreparedStatement statement = conn.prepareStatement("INSERT INTO product(name,price,amount) VALUES (?,?,?)");
            statement.setString(1, product.getName());
            statement.setDouble(2, product.getPrice());
            statement.setDouble(3, product.getAmount());
            statement.executeUpdate();

            ResultSet resultSet = statement.getGeneratedKeys();
            product.setSku(resultSet.getInt("last_insert_rowid()"));
            statement.close();
            return product;
        }catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Can`t insert unit ", e);
        }
    }

    public User insertUser(User user){
        try{
            PreparedStatement statement = conn.prepareStatement("INSERT INTO users(login, password) VALUES (?,?)");
            statement.setString(1, user.getLogin());
            statement.setString(2, user.getPassword());

            statement.executeUpdate();
            ResultSet resultSet=  statement.getGeneratedKeys();
            user.setId(resultSet.getInt("last_insert_rowid()"));
            statement.close();
            return user;
        }catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Can`t insert user ", e);
        }
    }

    public User getUserByLogin(String login){
        try{
            Statement st = conn.createStatement();
            ResultSet resultSet = st.executeQuery("SELECT * FROM users WHERE login = '" + login + "'");

           if(resultSet.next())
               return new User(resultSet.getInt("id"),
                               resultSet.getString("login"),
                               resultSet.getString("password"));

        } catch(SQLException e){
            System.out.println("Incorrect SQL query");
            e.printStackTrace();
        }
        return null;
    }

    public Unit getProductBySku(int sku){
        try{
            Statement st = conn.createStatement();
            ResultSet resultSet = st.executeQuery("SELECT * FROM product WHERE sku = " + sku);

            if(resultSet.next())
                return new Unit(resultSet.getInt("sku"),
                                resultSet.getString("name"),
                                resultSet.getDouble("price"),
                                resultSet.getDouble("amount"));


        } catch(SQLException e){
            System.out.println("Incorrect SQL query");
            e.printStackTrace();
        }
        return null;
    }

    public void updateProductByValuesAndSku(Unit product, Unit change) throws InvalidParameterException {
        if(product.getSku() == null){
            throw new InvalidParameterException("updateProductValuesAndId: inserted product without sku");
        }
        String sql= "UPDATE product " +
                    "SET name= '" + change.getName() + "', "+
                    "price=" + change.getPrice() + ", " +
                    "amount=" + change.getAmount() + " " +
                    "WHERE sku=" + product.getSku() + " " +
                    "AND name= '" + product.getName() + "' " +
                    "AND price=" + product.getPrice() + " " +
                    "AND amount=" + product.getAmount();

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void updateProductByValues(Unit product, Unit change) throws InvalidParameterException {
        String sql =
                        "UPDATE product" + " " +
                        "SET name='" + change.getName() + "', "+
                        "price=" + change.getPrice() + ", " +
                        "amount=" + change.getAmount() + " " +
                        "WHERE" + " " +
                        "name='" + product.getName() + "' " +
                        "AND price=" + product.getPrice() + " " +
                        "AND amount=" + product.getAmount();

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void deleteProductByValuesAndSku(Unit unit) throws InvalidParameterException {
        if(unit.getSku() == null)
            throw new InvalidParameterException("deleteProductValuesAndId: inserted unit without id");

        String sql=
                "DELETE from product" + " " +
                "WHERE sku="+unit.getSku() + " " +
                "AND name= '" + unit.getName() + "' " +
                "AND price=" + unit.getPrice() + "  " +
                "AND amount=" + unit.getAmount();

            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
    }

    public void deleteProductByValues(Unit product) throws InvalidParameterException {
        String sql= "DELETE from product" + " " +
                    "WHERE" + " " +
                    "name='" + product.getName() + "' " +
                    "AND price=" + product.getPrice() + " " +
                    "AND amount=" + product.getAmount();

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private static Unit convertProduct(ResultSet resultSet) throws SQLException {
        return  new Unit(resultSet.getInt("sku"),
                         resultSet.getString("name"),
                         resultSet.getDouble("price"),
                         resultSet.getDouble("amount"));
    }
}
