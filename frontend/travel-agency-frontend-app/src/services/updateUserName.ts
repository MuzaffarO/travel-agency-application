import api from "./api";

export interface UpdateUserNameRequest {
  firstName?: string;
  lastName?: string;
}

export interface UpdateUserNameResponse {
  message: string;
}

export const updateUserName = async (
  token: string,
  email: string,
  data: UpdateUserNameRequest
): Promise<UpdateUserNameResponse> => {
  const response = await api.put<UpdateUserNameResponse>(
    `/users/${encodeURIComponent(email)}/name`,
    data,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return response.data;
};

