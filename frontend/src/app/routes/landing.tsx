import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { paths } from "@/config/paths";
import { Link } from "react-router-dom";

export const LandingRoute = () => {
  return (
    <div className="space-y-12">
      <section className="text-center py-12">
        <h1 className="text-4xl font-bold mb-4">Welcome to EcommerceApp</h1>
        <p className="text-xl text-muted-foreground mb-8">
          Discover amazing products at unbeatable prices
        </p>
        <Button asChild size="lg">
          <Link to={paths.products.list.getHref()}>Shop Now</Link>
        </Button>
      </section>

      <section>
        <h2 className="text-2xl font-bold mb-8">Featured Products</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {/* Product cards will be rendered here */}
          <Card>
            <CardContent className="p-4">
              <div className="bg-muted h-48 rounded-md mb-4"></div>
              <h3 className="font-semibold">Sample Product</h3>
              <p className="text-muted-foreground">$99.99</p>
            </CardContent>
          </Card>
        </div>
      </section>
    </div>
  );
};
