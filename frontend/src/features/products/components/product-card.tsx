import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardFooter } from "@/components/ui/card";
import { useAddToCart } from "@/features/cart/api/cart";
import { Link } from "react-router-dom";
import { Product } from "../types";

interface ProductCardProps {
  product: Product;
}

export const ProductCard = ({ product }: ProductCardProps) => {
  const addToCart = useAddToCart();

  const handleAddToCart = () => {
    addToCart.mutate({ productId: product.id.toString(), quantity: 1 });
  };

  // Get the primary image or first active image
  const primaryImage =
    product.images?.find((img) => img.isPrimary && img.isActive) ||
    product.images?.find((img) => img.isActive);

  return (
    <Card className="overflow-hidden hover:shadow-lg transition-shadow">
      <Link to={`/products/${product.id}`} className="block">
        <div className="bg-muted h-48 flex items-center justify-center">
          {primaryImage ? (
            <img
              src={primaryImage.url}
              alt={primaryImage.altText || product.name}
              className="h-full w-full object-cover"
            />
          ) : (
            <span className="text-muted-foreground">No Image</span>
          )}
        </div>
        <CardContent className="p-4">
          <div className="flex items-start justify-between gap-2 mb-2">
            <h3 className="font-semibold text-foreground line-clamp-1">
              {product.name}
            </h3>
            {product.stockQuantity === 0 && (
              <Badge variant="destructive" className="shrink-0">
                Out of Stock
              </Badge>
            )}
          </div>
          <p className="text-muted-foreground text-sm mb-2 line-clamp-2">
            {product.description}
          </p>
          {product.category && (
            <Badge variant="secondary" className="text-xs">
              {product.category}
            </Badge>
          )}
        </CardContent>
      </Link>
      <CardFooter className="p-4 pt-0 flex justify-between items-center">
        <span className="text-lg font-bold text-foreground">
          ${product.price.toFixed(2)}
        </span>
        <Button
          onClick={handleAddToCart}
          disabled={addToCart.isPending || product.stockQuantity === 0}
          size="sm"
        >
          {addToCart.isPending ? "Adding..." : "Add to Cart"}
        </Button>
      </CardFooter>
    </Card>
  );
};
