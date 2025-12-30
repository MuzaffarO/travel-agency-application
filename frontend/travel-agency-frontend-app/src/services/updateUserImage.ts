import api from "./api";

export interface UpdateUserImageRequest {
  imageBase64: string;
}

export interface UpdateUserImageResponse {
  message: string;
  imageUrl: string;
}

export const updateUserImage = async (
  token: string,
  email: string,
  imageBase64: string
): Promise<UpdateUserImageResponse> => {
  const response = await api.put<UpdateUserImageResponse>(
    `/users/${encodeURIComponent(email)}/image`,
    { imageBase64 },
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return response.data;
};

