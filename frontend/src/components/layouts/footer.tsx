import { Separator } from "@/components/ui/separator";

export const Footer = () => {
  return (
    <footer className="bg-secondary text-secondary-foreground">
      <div className="container mx-auto px-4 py-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div>
            <h3 className="text-lg font-semibold mb-4">EcommerceApp</h3>
            <p className="text-muted-foreground">
              Your one-stop shop for quality products.
            </p>
          </div>

          <div>
            <h4 className="text-md font-semibold mb-4">Quick Links</h4>
            <ul className="space-y-2 text-muted-foreground">
              <li>
                <a
                  href="/about"
                  className="hover:text-foreground transition-colors"
                >
                  About
                </a>
              </li>
              <li>
                <a
                  href="/contact"
                  className="hover:text-foreground transition-colors"
                >
                  Contact
                </a>
              </li>
              <li>
                <a
                  href="/privacy"
                  className="hover:text-foreground transition-colors"
                >
                  Privacy Policy
                </a>
              </li>
            </ul>
          </div>

          <div>
            <h4 className="text-md font-semibold mb-4">Categories</h4>
            <ul className="space-y-2 text-muted-foreground">
              <li>
                <a
                  href="/products?category=electronics"
                  className="hover:text-foreground transition-colors"
                >
                  Electronics
                </a>
              </li>
              <li>
                <a
                  href="/products?category=clothing"
                  className="hover:text-foreground transition-colors"
                >
                  Clothing
                </a>
              </li>
              <li>
                <a
                  href="/products?category=books"
                  className="hover:text-foreground transition-colors"
                >
                  Books
                </a>
              </li>
            </ul>
          </div>

          <div>
            <h4 className="text-md font-semibold mb-4">Support</h4>
            <ul className="space-y-2 text-muted-foreground">
              <li>
                <a
                  href="/faq"
                  className="hover:text-foreground transition-colors"
                >
                  FAQ
                </a>
              </li>
              <li>
                <a
                  href="/shipping"
                  className="hover:text-foreground transition-colors"
                >
                  Shipping
                </a>
              </li>
              <li>
                <a
                  href="/returns"
                  className="hover:text-foreground transition-colors"
                >
                  Returns
                </a>
              </li>
            </ul>
          </div>
        </div>

        <Separator className="my-8" />

        <div className="text-center text-muted-foreground">
          <p>&copy; 2025 EcommerceApp. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
};
