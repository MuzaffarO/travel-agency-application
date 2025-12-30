import api from "./api";

export type DeleteTourResponse = {
  message: string;
};

export const deleteTour = async (
  tourId: string,
  token: string
): Promise<DeleteTourResponse> => {
  const response = await api.delete<DeleteTourResponse>(`/tours/${tourId}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  return response.data;
};

