import { Alert, AlertDescription } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Carousel,
  CarouselContent,
  CarouselItem,
  CarouselNext,
  CarouselPrevious,
} from "@/components/ui/carousel";
import { Skeleton } from "@/components/ui/skeleton";
import { useAddToCart } from "@/features/cart/api/cart";
import { useProduct } from "@/features/products";
import { useParams } from "react-router-dom";

export const ProductDetailRoute = () => {
  const { id } = useParams<{ id: string }>();
  const { data: product, isLoading, isError } = useProduct(id!);
  const addToCart = useAddToCart();

  const handleAddToCart = () => {
    if (product) {
      addToCart.mutate({ productId: product.id.toString(), quantity: 1 });
    }
  };

  if (isLoading) {
    return (
      <div className="grid md:grid-cols-2 gap-8">
        <Skeleton className="h-96 w-full" />
        <div className="space-y-4">
          <Skeleton className="h-8 w-3/4" />
          <Skeleton className="h-6 w-1/4" />
          <Skeleton className="h-24 w-full" />
          <Skeleton className="h-12 w-32" />
        </div>
      </div>
    );
  }

  if (isError || !product) {
    return (
      <div className="py-8">
        <Alert variant="destructive">
          <AlertDescription>
            Failed to load product. Please try again later.
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  const activeImages = product.images?.filter((img) => img.isActive) || [];

  return (
    <div className="max-w-7xl mx-auto">
      <div className="grid md:grid-cols-2 gap-8">
        {/* Image Carousel */}
        <div className="space-y-4">
          {activeImages.length > 0 ? (
            <Carousel className="w-full">
              <CarouselContent>
                {activeImages.map((image) => (
                  <CarouselItem key={image.id}>
                    <div className="aspect-square bg-muted rounded-lg overflow-hidden">
                      <img
                        src={image.url}
                        alt={image.altText || product.name}
                        className="w-full h-full object-cover"
                      />
                    </div>
                  </CarouselItem>
                ))}
              </CarouselContent>
              {activeImages.length > 1 && (
                <>
                  <CarouselPrevious />
                  <CarouselNext />
                </>
              )}
            </Carousel>
          ) : (
            <div className="aspect-square bg-muted rounded-lg flex items-center justify-center">
              <span className="text-muted-foreground">No Images Available</span>
            </div>
          )}
        </div>

        {/* Product Details */}
        <div className="space-y-6">
          <div>
            <h1 className="text-3xl font-bold text-foreground mb-2">
              {product.name}
            </h1>
            <div className="flex items-center gap-2 mb-4">
              {product.category && (
                <Badge variant="secondary">{product.category}</Badge>
              )}
              {product.brand && (
                <Badge variant="outline">{product.brand}</Badge>
              )}
              {product.stockQuantity === 0 && (
                <Badge variant="destructive">Out of Stock</Badge>
              )}
              {product.stockQuantity > 0 && product.stockQuantity <= 10 && (
                <Badge
                  variant="outline"
                  className="text-orange-600 border-orange-600"
                >
                  Only {product.stockQuantity} left
                </Badge>
              )}
            </div>
          </div>

          <div className="text-4xl font-bold text-foreground">
            ${product.price.toFixed(2)}
          </div>

          <div className="prose prose-sm max-w-none">
            <p className="text-muted-foreground">{product.description}</p>
          </div>

          {product.sku && (
            <div className="text-sm text-muted-foreground">
              <span className="font-semibold">SKU:</span> {product.sku}
            </div>
          )}

          {product.dimensions && (
            <div className="text-sm text-muted-foreground">
              <span className="font-semibold">Dimensions:</span>{" "}
              {product.dimensions}
            </div>
          )}

          {product.weight && (
            <div className="text-sm text-muted-foreground">
              <span className="font-semibold">Weight:</span> {product.weight} kg
            </div>
          )}

          <div className="pt-4">
            <Button
              onClick={handleAddToCart}
              disabled={addToCart.isPending || product.stockQuantity === 0}
              size="lg"
              className="w-full md:w-auto"
            >
              {addToCart.isPending ? "Adding to Cart..." : "Add to Cart"}
            </Button>
          </div>

          {addToCart.isSuccess && (
            <Alert>
              <AlertDescription>
                Product added to cart successfully!
              </AlertDescription>
            </Alert>
          )}
        </div>
      </div>
    </div>
  );
};
