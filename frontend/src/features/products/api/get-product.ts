import { api } from "@/lib/api-client";
import { useQuery } from "@tanstack/react-query";
import { Product } from "../types";

export const getProduct = async (id: string): Promise<Product> => {
  const response = await api.get<Product>(`/catalog/products/${id}`);
  return response.data;
};

export const useProduct = (id: string) => {
  return useQuery({
    queryKey: ["products", id],
    queryFn: () => getProduct(id),
    enabled: !!id,
  });
};
