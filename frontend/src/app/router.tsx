import { DashboardLayout } from "@/components/layouts/dashboard-layout";
import { Route, Routes } from "react-router-dom";
import { CartRoute } from "./routes/cart";
import { LandingRoute } from "./routes/landing";
import { LoginRoute } from "./routes/login";
import { ProductDetailRoute } from "./routes/product-detail";
import { ProductsRoute } from "./routes/products";

export const AppRouter = () => {
  return (
    <Routes>
      <Route
        path="/"
        element={
          <DashboardLayout>
            <LandingRoute />
          </DashboardLayout>
        }
      />
      <Route
        path="/products"
        element={
          <DashboardLayout>
            <ProductsRoute />
          </DashboardLayout>
        }
      />
      <Route
        path="/products/:id"
        element={
          <DashboardLayout>
            <ProductDetailRoute />
          </DashboardLayout>
        }
      />
      <Route
        path="/cart"
        element={
          <DashboardLayout>
            <CartRoute />
          </DashboardLayout>
        }
      />
      <Route
        path="/login"
        element={
          <DashboardLayout>
            <LoginRoute />
          </DashboardLayout>
        }
      />
    </Routes>
  );
};
