import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { useRemoveFromCart, useUpdateCartItem } from "../api/cart";
import { CartItem } from "../types";

interface CartItemCardProps {
  item: CartItem;
}

export const CartItemCard = ({ item }: CartItemCardProps) => {
  const updateCartItem = useUpdateCartItem();
  const removeFromCart = useRemoveFromCart();

  const handleIncrement = () => {
    updateCartItem.mutate({ itemId: item.id, quantity: item.quantity + 1 });
  };

  const handleDecrement = () => {
    if (item.quantity > 1) {
      updateCartItem.mutate({ itemId: item.id, quantity: item.quantity - 1 });
    }
  };

  const handleRemove = () => {
    removeFromCart.mutate(item.id);
  };

  return (
    <Card className="p-4">
      <div className="flex items-center gap-4">
        <div className="w-20 h-20 bg-muted rounded flex-shrink-0">
          {item.productImageUrl ? (
            <img
              src={item.productImageUrl}
              alt={item.productName}
              className="w-full h-full object-cover rounded"
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-muted-foreground text-xs">
              No Image
            </div>
          )}
        </div>
        <div className="flex-1">
          <h3 className="font-semibold text-foreground">{item.productName}</h3>
          <p className="text-muted-foreground">
            ${item.productPrice.toFixed(2)}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button
            onClick={handleDecrement}
            disabled={updateCartItem.isPending || item.quantity <= 1}
            variant="outline"
            size="icon"
            className="h-8 w-8"
          >
            -
          </Button>
          <span className="px-3 font-medium">{item.quantity}</span>
          <Button
            onClick={handleIncrement}
            disabled={updateCartItem.isPending}
            variant="outline"
            size="icon"
            className="h-8 w-8"
          >
            +
          </Button>
        </div>
        <div className="text-right">
          <p className="font-semibold text-foreground mb-1">
            ${item.subtotal.toFixed(2)}
          </p>
          <Button
            onClick={handleRemove}
            disabled={removeFromCart.isPending}
            variant="ghost"
            size="sm"
            className="text-destructive hover:text-destructive h-auto p-0"
          >
            {removeFromCart.isPending ? "Removing..." : "Remove"}
          </Button>
        </div>
      </div>
    </Card>
  );
};
