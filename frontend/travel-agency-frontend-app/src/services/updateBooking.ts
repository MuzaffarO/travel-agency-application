import axios from "axios";
import { BACK_URL } from "../constants";

export interface UpdateBookingRequest {
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

export const updateBooking = async (
  bookingId: string,
  data: UpdateBookingRequest,
  token?: string
) => {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await axios.patch(`${BACK_URL}/bookings/${bookingId}`, data, {
    headers,
  });
  return response.data;
};


