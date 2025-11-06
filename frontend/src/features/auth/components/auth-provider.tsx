import { createContext, ReactNode, useContext } from "react";
import { useCurrentUser } from "../api/get-current-user";
import { User } from "../types";

interface AuthContextValue {
  user: User | null;
  isLoading: boolean;
  isError: boolean;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const { data: user, isLoading, isError } = useCurrentUser();

  console.log("AuthProvider render - user:", user, "isLoading:", isLoading);

  const value: AuthContextValue = {
    user: user ?? null,
    isLoading,
    isError,
    isAuthenticated: !!user,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
