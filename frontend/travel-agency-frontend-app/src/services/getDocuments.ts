import { BACK_URL } from "../constants";

export type PaymentDocument = {
  id: string;
  fileName: string;
};

export type GuestDocument = {
  userName: string;
  documents: {
    id: string;
    fileName: string;
  }[];
};

export type BookingDocuments = {
  payments: PaymentDocument[];
  guestDocuments: GuestDocument[];
};

export const fetchBookingDocuments = async (
  bookingId: string,
  token: string
): Promise<BookingDocuments> => {
  const response = await fetch(`${BACK_URL}/bookings/${bookingId}/documents`, {
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error("Failed to fetch booking documents");
  }

  return response.json();
};
