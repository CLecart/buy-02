export interface UserProfile {
  id: string;
  userId: string;
  totalOrders: number;
  totalSpent: number;
  averageOrderValue: number;
  favoriteProductIds?: string[];
  mostPurchasedProductIds?: string[];
  createdAt?: string;
  updatedAt?: string;
}

export interface SellerProfile {
  id: string;
  sellerId: string;
  storeName: string;
  storeDescription?: string;
  totalProductsSold: number;
  totalRevenue: number;
  averageRating: number;
  totalReviews: number;
  bestSellingProductIds?: string[];
  verified?: boolean;
  createdAt?: string;
  updatedAt?: string;
}
