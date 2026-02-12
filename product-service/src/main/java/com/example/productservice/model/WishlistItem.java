package com.example.productservice.model;

/**
 * Represents an item in the user's wishlist.
 */
public class WishlistItem {

	/**
	 * The unique identifier of the product.
	 */
	private String productId;

	/**
	 * The name of the product.
	 */
	private String productName;

	/**
	 * The price of the product.
	 */
	private double price;

	/**
	 * Default constructor.
	 */
	public WishlistItem() {}

	/**
	 * Constructs a WishlistItem with all fields.
	 * @param productId the product ID
	 * @param productName the product name
	 * @param price the product price
	 */
	public WishlistItem(String productId, String productName, double price) {
		this.productId = productId;
		this.productName = productName;
		this.price = price;
	}

	/**
	 * Gets the product ID.
	 * @return the product ID
	 */
	public String getProductId() {
		return productId;
	}

	/**
	 * Sets the product ID.
	 * @param productId the product ID
	 */
	public void setProductId(String productId) {
		this.productId = productId;
	}

	/**
	 * Gets the product name.
	 * @return the product name
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * Sets the product name.
	 * @param productName the product name
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}

	/**
	 * Gets the product price.
	 * @return the product price
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * Sets the product price.
	 * @param price the product price
	 */
	public void setPrice(double price) {
		this.price = price;
	}
}

