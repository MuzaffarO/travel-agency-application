import api from "./api";

export interface CreateTravelAgentRequest {
  email: string;
  firstName: string;
  lastName: string;
  role: "TRAVEL_AGENT" | "ADMIN";
  password: string;
  phone?: string;
  messenger?: string;
}

export interface CreateTravelAgentResponse {
  email: string;
  message: string;
}

export const createTravelAgent = async (
  token: string,
  data: CreateTravelAgentRequest
): Promise<CreateTravelAgentResponse> => {
  const response = await api.post<CreateTravelAgentResponse>(
    "/admin/travel-agents",
    data,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return response.data;
};

