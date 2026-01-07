package model;

public class Category {
	private String categoryName;

    public Category() {}

    public Category(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public String getCategory() { return categoryName; }
    public void setCategory(String category) { this.categoryName = category; }

}