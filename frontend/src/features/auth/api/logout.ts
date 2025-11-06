import { api } from "@/lib/api-client";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";

export const logoutUser = async (): Promise<void> => {
  await api.post("/auth/logout");
};

export const useLogout = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: logoutUser,
    onSuccess: () => {
      console.log("Logout successful");
      // Clear the current user from cache
      queryClient.setQueryData(["currentUser"], null);
      // Invalidate queries to ensure fresh data on next login
      queryClient.invalidateQueries({ queryKey: ["currentUser"] });
      // Redirect to login page
      navigate("/login");
    },
    onError: (error) => {
      console.error("Logout failed:", error);
    },
  });
};
