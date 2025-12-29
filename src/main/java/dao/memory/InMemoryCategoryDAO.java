package dao.memory;

import java.util.List;

import dao.CategoryDAO;
import exception.DAOException;
import exception.DuplicateRecordException;
import exception.RecordNotFoundException;
import model.Category;

public class InMemoryCategoryDAO implements CategoryDAO {

	@Override
	public List<Category> getAllCategories() throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addCategory(Category category) throws DAOException, DuplicateRecordException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteCategory(String category) throws DAOException, RecordNotFoundException {
		// TODO Auto-generated method stub

	}

}
