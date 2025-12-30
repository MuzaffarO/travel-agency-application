import api from "./api";

export interface UpdatePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface UpdatePasswordResponse {
  message: string;
}

export const updatePassword = async (
  token: string,
  email: string,
  data: UpdatePasswordRequest
): Promise<UpdatePasswordResponse> => {
  const response = await api.put<UpdatePasswordResponse>(
    `/users/${encodeURIComponent(email)}/password`,
    data,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return response.data;
};

