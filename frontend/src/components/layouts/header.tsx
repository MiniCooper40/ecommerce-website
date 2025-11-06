import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth, useLogout } from "@/features/auth";
import { Link } from "react-router-dom";

export const Header = () => {
  const { user, isLoading } = useAuth();
  const { mutate: logout } = useLogout();

  console.log("Header render - user:", user, "isLoading:", isLoading);

  const isAdmin =
    user?.roles?.includes("ROLE_ADMIN") || user?.roles?.includes("ADMIN");

  return (
    <header className="border-b bg-background">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          <Link to="/" className="text-xl font-bold text-foreground">
            EcommerceApp
          </Link>

          <nav className="hidden md:flex space-x-8">
            <Link
              to="/"
              className="text-muted-foreground hover:text-foreground transition-colors"
            >
              Home
            </Link>
            <Link
              to="/products"
              className="text-muted-foreground hover:text-foreground transition-colors"
            >
              Products
            </Link>
            <Link
              to="/cart"
              className="text-muted-foreground hover:text-foreground transition-colors"
            >
              Cart
            </Link>
            {isAdmin && (
              <Link
                to="/admin"
                className="text-muted-foreground hover:text-foreground transition-colors"
              >
                Admin
              </Link>
            )}
          </nav>

          <div className="flex items-center space-x-4">
            {isLoading ? (
              <Skeleton className="h-10 w-20" />
            ) : user ? (
              <div className="flex items-center space-x-4">
                <span className="text-sm text-muted-foreground">
                  {user.firstName} {user.lastName}
                </span>
                <Button onClick={() => logout()} variant="outline">
                  Logout
                </Button>
              </div>
            ) : (
              <Button asChild>
                <Link to="/login">Login</Link>
              </Button>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};
