import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { useCart } from "@/features/cart/api/cart";
import { CartItemCard } from "@/features/cart/components/cart-item-card";

export const CartRoute = () => {
  const { data: cart, isLoading, isError } = useCart();

  if (isLoading) {
    return (
      <div>
        <h1 className="text-3xl font-bold mb-8">Shopping Cart</h1>
        <div className="space-y-4">
          {[...Array(3)].map((_, i) => (
            <Card key={i} className="p-4">
              <div className="flex items-center gap-4">
                <Skeleton className="w-20 h-20" />
                <div className="flex-1 space-y-2">
                  <Skeleton className="h-4 w-3/4" />
                  <Skeleton className="h-4 w-1/4" />
                </div>
                <Skeleton className="h-8 w-24" />
              </div>
            </Card>
          ))}
        </div>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="py-8">
        <Alert variant="destructive">
          <AlertDescription>
            Failed to load cart. Please try again later.
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  const cartItems = cart?.items || [];

  return (
    <div>
      <h1 className="text-3xl font-bold mb-8">Shopping Cart</h1>
      {cartItems.length === 0 ? (
        <Card className="p-12">
          <p className="text-center text-muted-foreground">
            Your cart is empty
          </p>
        </Card>
      ) : (
        <>
          <div className="space-y-4 mb-6">
            {cartItems.map((item) => (
              <CartItemCard key={item.id} item={item} />
            ))}
          </div>
          <Card>
            <CardContent className="p-6">
              <Separator className="mb-4" />
              <div className="flex justify-between items-center text-xl font-bold mb-4">
                <span>Total:</span>
                <span>${cart?.total.toFixed(2)}</span>
              </div>
              <Button size="lg" className="w-full">
                Proceed to Checkout
              </Button>
            </CardContent>
          </Card>
        </>
      )}
    </div>
  );
};
