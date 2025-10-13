import axios from "axios";
import {BACK_URL} from "../constants.ts";

export interface ReviewRequest {
  bookingId: string;
  rate: number;
  comment: string;
}

export const sendReview = async (tourId: string, newReview: ReviewRequest, token?: string) => {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await axios.post(`${BACK_URL}/tours/${tourId}/feedbacks`, newReview, {
    headers,
  });

  return response.data;
}