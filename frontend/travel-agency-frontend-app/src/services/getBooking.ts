import api from "./api.ts";
import { type Booking } from "./getBookings";

export const getBooking = async (bookingId: string, token?: string): Promise<Booking | null> => {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  try {
    const response = await api.get(`/bookings`, { headers });
    const bookings: Booking[] = response.data.bookings || [];
    
    // Find the specific booking by ID
    const booking = bookings.find((b: Booking) => b.id === bookingId);
    
    if (!booking) return null;

    // Process the booking similar to getBookings
    const guestNames =
      booking.customerDetails?.documents?.guestDocuments?.map(
        (g) => g.userName
      ) || [];

    return {
      ...booking,
      tourDetails: {
        ...booking.tourDetails,
        guests:
          guestNames.length > 0
            ? guestNames.join(", ")
            : booking.tourDetails.guests,
      },
    };
  } catch (error) {
    console.error("Failed to fetch booking:", error);
    return null;
  }
};


