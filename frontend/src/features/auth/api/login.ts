import { api } from "@/lib/api-client";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { AuthResponse, LoginInput } from "../types";

export const loginUser = async (input: LoginInput): Promise<AuthResponse> => {
  const response = await api.post<AuthResponse>("/auth/login", input);
  return response.data;
};

export const useLogin = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: loginUser,
    onSuccess: (data) => {
      console.log("Login successful:", data);
      console.log("Setting user data:", data.user);
      // Update the currentUser query cache with the user data from login response
      queryClient.setQueryData(["currentUser"], data.user);
      // Invalidate to trigger re-render of components using this query
      queryClient.invalidateQueries({ queryKey: ["currentUser"] });
      console.log(
        "Current user cache after set:",
        queryClient.getQueryData(["currentUser"])
      );
      // Redirect to home page
      navigate("/");
    },
    onError: (error) => {
      console.error("Login failed:", error);
    },
  });
};
