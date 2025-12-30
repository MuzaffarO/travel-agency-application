import api from "./api";

export type UpdateTourRequest = {
  name?: string;
  destination?: string;
  startDates?: string[];
  durations?: string[];
  mealPlans?: string[];
  priceFrom?: number;
  priceByDuration?: Record<string, number>;
  mealSupplementsPerDay?: Record<string, number>;
  tourType?: string;
  maxAdults?: number;
  maxChildren?: number;
  availablePackages?: number;
  imageUrls?: string[];
  summary?: string;
  accommodation?: string;
  hotelName?: string;
  hotelDescription?: string;
  customDetails?: Record<string, string>;
  freeCancellation?: string;
  freeCancellationDaysBefore?: number;
};

export type UpdateTourResponse = {
  tourId: string;
  message: string;
};

export const updateTour = async (
  tourId: string,
  data: UpdateTourRequest,
  token: string
): Promise<UpdateTourResponse> => {
  const response = await api.put<UpdateTourResponse>(`/tours/${tourId}`, data, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  return response.data;
};

