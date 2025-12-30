import api from "./api";

export interface TravelAgent {
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  createdAt: string;
  createdBy: string;
  phone?: string;
  messenger?: string;
}

export interface ListTravelAgentsResponse {
  agents: TravelAgent[];
}

export const listTravelAgents = async (
  token: string
): Promise<TravelAgent[]> => {
  const response = await api.get<ListTravelAgentsResponse>(
    "/admin/travel-agents",
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return response.data.agents;
};

