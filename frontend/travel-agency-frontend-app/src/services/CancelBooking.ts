import axios from "axios";
import { BACK_URL } from "../constants";

export const cancelBooking = async (bookingId: string, token?: string) => {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const response = await axios.delete(`${BACK_URL}/bookings/${bookingId}`, {
    headers,
  });
  return response.data;
};
