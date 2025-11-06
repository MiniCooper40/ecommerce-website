import { LoginForm } from "@/features/auth/components/login-form";

export const LoginRoute = () => {
  return (
    <div className="max-w-md mx-auto">
      <h1 className="text-3xl font-bold text-gray-900 mb-8 text-center">
        Login
      </h1>
      <LoginForm />
    </div>
  );
};
