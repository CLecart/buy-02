-- V1__create_ecommerce_schema.sql
-- Migration: create ecommerce schema for carts, orders, wishlists, and profiles
-- Compatible with PostgreSQL. For other DBs adjust types and JSONB usage.

BEGIN;

-- Carts and cart items
CREATE TABLE carts (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(100) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE cart_items (
  id VARCHAR(36) PRIMARY KEY,
  cart_id VARCHAR(36) NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
  product_id VARCHAR(36) NOT NULL,
  seller_id VARCHAR(100),
  product_name TEXT,
  quantity INTEGER NOT NULL CHECK (quantity > 0),
  price NUMERIC(12,2) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Orders and order items
CREATE TABLE orders (
  id VARCHAR(36) PRIMARY KEY,
  buyer_id VARCHAR(100) NOT NULL,
  buyer_email VARCHAR(255),
  total_amount NUMERIC(12,2) NOT NULL,
  status VARCHAR(50) NOT NULL,
  payment_method VARCHAR(50),
  payment_reference VARCHAR(255),
  shipping_address TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE order_items (
  id VARCHAR(36) PRIMARY KEY,
  order_id VARCHAR(36) NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  product_id VARCHAR(36) NOT NULL,
  seller_id VARCHAR(100),
  product_name TEXT,
  quantity INTEGER NOT NULL CHECK (quantity > 0),
  price NUMERIC(12,2) NOT NULL
);

-- Wishlist (bonus)
CREATE TABLE wishlists (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(100) NOT NULL,
  product_id VARCHAR(36) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  UNIQUE(user_id, product_id)
);

-- Profiles for analytics
CREATE TABLE user_profiles (
  user_id VARCHAR(100) PRIMARY KEY,
  total_spent NUMERIC(14,2) DEFAULT 0,
  order_count INTEGER DEFAULT 0,
  best_products JSONB DEFAULT '[]'::jsonb,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE seller_profiles (
  seller_id VARCHAR(100) PRIMARY KEY,
  total_revenue NUMERIC(14,2) DEFAULT 0,
  total_orders INTEGER DEFAULT 0,
  best_selling_products JSONB DEFAULT '[]'::jsonb,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Indexes for common queries
CREATE INDEX idx_orders_buyer_id ON orders(buyer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_cart_user_id ON carts(user_id);
CREATE INDEX idx_wishlist_user ON wishlists(user_id);

COMMIT;
