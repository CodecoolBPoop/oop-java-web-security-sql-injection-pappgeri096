package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ani on 2016.11.13..
 */
public class TodoDaoImplWithJdbc implements TodoDao {

    private static final String DATABASE = "jdbc:postgresql://localhost:5432/todolist";
    private static final String DB_USER = System.getenv("USER");
    private static final String DB_PASSWORD = System.getenv("PASSWORD");

    @Override
    public void add(Todo todo) {
        String query = "INSERT INTO todos (title, id, status) " +
                "VALUES ('" + todo.title + "', '" + todo.id + "', '" + todo.status + "');";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("INSERT INTO todos (title, id, status) VALUES (?,?,?);");
            preparedStatement.setString(1,todo.title);
            preparedStatement.setString(2,todo.id);
            preparedStatement.setString(3,todo.status.toString());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // executeQuery(query);
    }

    @Override
    public Todo find(String id) {

        String query = "SELECT * FROM todos WHERE id ='" + id + "';";

        try {
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM todos WHERE id = ?");
            preparedStatement.setString(1,id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                Todo result = new Todo(resultSet.getString("title"),
                        resultSet.getString("id"),
                        Status.valueOf(resultSet.getString("status")));
                return result;
            } else {
                return null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
   }

    @Override
    public void update(String id, String title) {
        String query = "UPDATE todos SET title = '" + title + "' WHERE id = '" + id + "';";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("UPDATE todos SET title = ? WHERE id = ?;");
            preparedStatement.setString(1,title);
            preparedStatement.setString(2,id);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //executeQuery(query);
    }

    @Override
    public List<Todo> ofStatus(String statusString) {
        return (statusString == null || statusString.isEmpty()) ?
                all() : ofStatus(Status.valueOf(statusString.toUpperCase()));
    }

    @Override
    public List<Todo> ofStatus(Status status) {
        String query = "SELECT * FROM todos WHERE status = ?;";

        List<Todo> resultList = new ArrayList<>();

        try {
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1,status.toString());
            ResultSet resultSet = preparedStatement.executeQuery(query);
            while (resultSet.next()){
                Todo actTodo = new Todo(resultSet.getString("title"),
                        resultSet.getString("id"),
                        Status.valueOf(resultSet.getString("status")));
                resultList.add(actTodo);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultList;
    }

    @Override
    public void remove(String id) {
        String query = "DELETE FROM todos WHERE id = '" + id +"';";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("DELETE FROM todos WHERE id = ?;");
            preparedStatement.setString(1,id);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //executeQuery(query);
    }

    @Override
    public void removeCompleted() {
        String query = "DELETE FROM todos WHERE status = '" + Status.COMPLETE +"';";
        executeQuery(query);
    }

    @Override
    public void toggleStatus(String id) {
        Todo todo = find(id);

        if (null == todo) {
            return;
        }

        Status newStatus = (todo.status == Status.ACTIVE) ? Status.COMPLETE : Status.ACTIVE;
        String query = "UPDATE todos SET status = ? WHERE id = ?;";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(query);
            preparedStatement.setString(1,newStatus.toString());
            preparedStatement.setString(2,id);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void toggleAll(boolean complete) {
        Status newStatus = complete ? Status.COMPLETE : Status.ACTIVE;
        String query = "UPDATE todos SET status = '" + newStatus + "';";
        executeQuery(query);
    }

    @Override
    public List<Todo> all() {
        String query = "SELECT * FROM todos;";

        List<Todo> resultList = new ArrayList<>();

        try (Connection connection = getConnection();
             Statement statement =connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query);
        ){
            while (resultSet.next()){
                Todo actTodo = new Todo(resultSet.getString("title"),
                        resultSet.getString("id"),
                        Status.valueOf(resultSet.getString("status")));
                resultList.add(actTodo);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultList;
    }

    // package private so test can see it, but TodoList not
    void deleteAll() {
        String query = "DELETE FROM todos;";
        executeQuery(query);
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DATABASE,
                DB_USER,
                DB_PASSWORD);
    }

    private void executeQuery(String query) {
        try (Connection connection = getConnection();
             Statement statement =connection.createStatement();
        ){
            statement.execute(query);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
