import api from "./api.ts";

export interface CancelledInfo {
  by: string;
  reason: string;
}

export interface Guest {
  name: string;
  documents: {
    fileName: string;
    fileUrl: string;
  }[];
}

export interface GuestDocument {
  userName: string;
  documents: {
    fileName: string;
    fileUrl: string;
  }[];
}

export interface CustomerDetails {
  name: string;
  email: string;
  phone: string;
  documents: {
    payments: { fileUrl: string }[];
    guestDocuments: GuestDocument[];
  };
}

export interface Booking {
  id: string;
  state: string;
  tourImageUrl: string;
  name: string;
  destination: string;
  freeCancellationUntil: string;
  duration: string;
  price: string;
  tourDetails: {
    tourId: string,
    date: string;
    duration: string;
    mealPlans: string;
    guests: string;
    totalPrice: string;
    documents: string;
    freeCancelation?: string;
  };
  travelAgent: {
    name: string;
    email: string;
    phone: string;
    messenger: string;
  };
  cancelledInfo?: CancelledInfo;
  customerDetails?: CustomerDetails;
  guestsList?: Guest[];
}

export const getBookings = async (token?: string): Promise<Booking[]> => {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await api.get(`/bookings`, { headers });
  console.log("Bookings from backend:", response.data.bookings);

  return response.data.bookings.map((booking: Booking) => {
    const guestNames =
      booking.customerDetails?.documents?.guestDocuments?.map(
        (g: GuestDocument) => g.userName
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
  });
};
