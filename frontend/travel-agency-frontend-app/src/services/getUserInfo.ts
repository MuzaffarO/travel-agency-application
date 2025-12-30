import api from "./api";

export interface UserInfo {
  firstName: string;
  lastName: string;
  imageUrl?: string;
  role: string;
}

export const getUserInfo = async (
  token: string,
  email: string
): Promise<UserInfo> => {
  const response = await api.get<UserInfo>(
    `/users/${encodeURIComponent(email)}`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return response.data;
};

