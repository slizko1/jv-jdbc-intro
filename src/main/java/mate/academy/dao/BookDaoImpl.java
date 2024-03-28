package mate.academy.dao;

import exceptions.DataProcessingException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mate.academy.ConnectionUtil;
import mate.academy.lib.Dao;
import mate.academy.models.Book;

@Dao
public class BookDaoImpl implements BookDao {
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String PRICE = "price";

    @Override
    public Book create(Book book) {
        String query = "INSERT INTO book (title, price) VALUES (?, ?)";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, book.getTitle());
            statement.setBigDecimal(2, book.getPrice());
            validateChanges(statement.executeUpdate());
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                book = new Book(resultSet.getLong(1), book.getTitle(), book.getPrice());
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can't create book. " + book, e);
        }
        return book;
    }

    @Override
    public Optional<Book> findById(Long id) {
        String query = "SELECT * FROM book WHERE id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(getBook(resultSet));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataProcessingException("Can't find book by id " + id, e);
        }
    }

    @Override
    public List<Book> findAll() {
        String query = "SELECT * FROM book";
        List<Book> books = new ArrayList<>();
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                books.add(getBook(resultSet));
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can't find all books", e);
        }
        return books;
    }

    @Override
    public Book update(Book book) {
        String query = "UPDATE book SET title = ?, price = ? WHERE id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(query)) {
            statement.setString(1, book.getTitle());
            statement.setBigDecimal(2, book.getPrice());
            statement.setLong(3, book.getId());
            validateChanges(statement.executeUpdate());
        } catch (SQLException e) {
            throw new DataProcessingException("Can't update book", e);
        }
        return book;
    }

    @Override
    public boolean deleteById(Long id) {
        String query = "DELETE FROM book WHERE id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, id);
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new DataProcessingException("Can't delete book by id " + id, e);
        }
    }

    private static void validateChanges(int affectedRows) {
        if (affectedRows < 1) {
            throw new RuntimeException("Expected to change at least 1 row, but was "
                    + affectedRows);
        }
    }

    private Book getBook(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong(ID);
        String title = resultSet.getString(TITLE);
        BigDecimal price = resultSet.getBigDecimal(PRICE);
        return new Book(id, title, price);
    }
}