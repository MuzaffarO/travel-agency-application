import api from "./api";
import type { Tour } from "../pages/MainPage";

export type MyToursResponse = {
  tours: Tour[];
};

export const getMyTours = async (token: string): Promise<Tour[]> => {
  const response = await api.get<MyToursResponse>("/tours/my", {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  return response.data.tours;
};

