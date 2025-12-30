import api from "./api";

export interface DeleteTravelAgentResponse {
  message: string;
}

export const deleteTravelAgent = async (
  token: string,
  email: string
): Promise<DeleteTravelAgentResponse> => {
  const response = await api.delete<DeleteTravelAgentResponse>(
    `/admin/travel-agents/${encodeURIComponent(email)}`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return response.data;
};

