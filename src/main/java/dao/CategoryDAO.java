package dao;

import model.Category;
import exception.DAOException;
import exception.DuplicateRecordException;

import java.util.List;

public interface CategoryDAO {
    List<Category> getAllCategories() throws DAOException;
    void addCategory(Category category) throws DAOException, DuplicateRecordException;
    void deleteCategory(String category) throws DAOException;
}