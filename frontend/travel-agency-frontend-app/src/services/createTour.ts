import api from "./api";

export type CreateTourRequest = {
  name: string;
  destination: string;
  startDates: string[];
  durations: string[];
  mealPlans: string[];
  priceFrom: number;
  priceByDuration: Record<string, number>;
  mealSupplementsPerDay: Record<string, number>;
  tourType?: string;
  maxAdults: number;
  maxChildren: number;
  availablePackages: number;
  imageUrls?: string[];
  summary?: string;
  accommodation?: string;
  hotelName?: string;
  hotelDescription?: string;
  customDetails?: Record<string, string>;
  freeCancellation?: string;
  freeCancellationDaysBefore?: number;
};

export type CreateTourResponse = {
  tourId: string;
  message: string;
};

export const createTour = async (
  data: CreateTourRequest,
  token: string
): Promise<CreateTourResponse> => {
  const response = await api.post<CreateTourResponse>("/tours", data, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  return response.data;
};

