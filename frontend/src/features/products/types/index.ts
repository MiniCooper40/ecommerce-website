export interface ProductImage {
  id: number;
  s3Key: string;
  s3Bucket: string;
  url: string;
  fileName: string;
  contentType?: string;
  fileSize?: number;
  altText?: string;
  displayOrder: number;
  isPrimary: boolean;
  isActive: boolean;
  createdAt?: number[];
  updatedAt?: number[];
}

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  images?: ProductImage[];
  category?: string;
  brand?: string;
  stockQuantity: number;
  sku?: string;
  weight?: number;
  dimensions?: string;
  isActive?: boolean;
  createdAt?: number[];
  updatedAt?: number[];
}

// Spring Page response structure
export interface PageableResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}

export type ProductsResponse = PageableResponse<Product>;
