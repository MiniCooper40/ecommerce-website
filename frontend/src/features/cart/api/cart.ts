import { api } from "@/lib/api-client";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import * as yup from "yup";
import { Cart } from "../types";

// Schema for adding items to cart
export const addToCartInputSchema = yup.object({
  productId: yup.string().required("Product ID is required"),
  quantity: yup.number().min(1, "Quantity must be at least 1").required(),
});

export type AddToCartInput = yup.InferType<typeof addToCartInputSchema>;

// Schema for updating cart item quantity
export const updateCartItemInputSchema = yup.object({
  itemId: yup.string().required("Item ID is required"),
  quantity: yup.number().min(0, "Quantity must be at least 0").required(),
});

export type UpdateCartItemInput = yup.InferType<
  typeof updateCartItemInputSchema
>;

// Get cart
export const getCart = async (): Promise<Cart> => {
  const response = await api.get<Cart>("/cart");
  return response.data;
};

export const useCart = () => {
  return useQuery({
    queryKey: ["cart"],
    queryFn: getCart,
  });
};

// Add to cart
export const addToCart = async (input: AddToCartInput): Promise<Cart> => {
  const response = await api.post<Cart>("/cart/items", input);
  return response.data;
};

export const useAddToCart = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: addToCart,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["cart"] });
    },
  });
};

// Update cart item
export const updateCartItem = async (
  input: UpdateCartItemInput
): Promise<Cart> => {
  const response = await api.put<Cart>(`/cart/items/${input.itemId}`, {
    quantity: input.quantity,
  });
  return response.data;
};

export const useUpdateCartItem = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: updateCartItem,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["cart"] });
    },
  });
};

// Remove from cart
export const removeFromCart = async (itemId: string): Promise<Cart> => {
  const response = await api.delete<Cart>(`/cart/items/${itemId}`);
  return response.data;
};

export const useRemoveFromCart = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: removeFromCart,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["cart"] });
    },
  });
};
