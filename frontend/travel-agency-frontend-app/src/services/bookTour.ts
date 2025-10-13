import axios from "axios";
import { BACK_URL } from "../constants";

export interface BookingRequest {
  userId: string;
  tourId: string;
  date: string;
  duration: string;
  mealPlan: string;
  guests: {
    adult: number;
    children: number;
  };
  personalDetails: {
    firstName: string;
    lastName: string;
  }[];
}

export const bookTour = async (data: BookingRequest, token?: string) => {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await axios.post(`${BACK_URL}/bookings`, data, { headers });
  return response.data;
};
