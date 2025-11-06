import { api } from "@/lib/api-client";
import { useQuery } from "@tanstack/react-query";
import { ProductsResponse } from "../types";

interface GetProductsParams {
  page?: number;
  size?: number;
  sort?: string;
}

export const getProducts = async (
  params?: GetProductsParams
): Promise<ProductsResponse> => {
  const response = await api.get<ProductsResponse>("/catalog/products", {
    params: {
      page: params?.page ?? 0,
      size: params?.size ?? 20,
      sort: params?.sort,
    },
  });
  return response.data;
};

export const useProducts = (params?: GetProductsParams) => {
  return useQuery({
    queryKey: ["products", params],
    queryFn: () => getProducts(params),
  });
};
