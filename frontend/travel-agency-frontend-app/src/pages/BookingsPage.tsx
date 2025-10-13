import { useEffect, useState } from "react";
import { getBookings } from "../services/getBookings";
import TourTabs from "../components/Tabs";
import BookingCard from "../components/BookingCard";
import { type Booking } from "../services/getBookings";

const BookingsPage = () => {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [activeTab, setActiveTab] = useState("All tours");

  useEffect(() => {
    const fetchBookings = async () => {
      setLoading(true);
      setError(null);
      try {
        const storedUser = localStorage.getItem("user");
        if (!storedUser) {
          setError("User not found");
          setLoading(false);
          return;
        }

        const user = JSON.parse(storedUser) as { token?: string };
        const token = user.token;

        if (!token) {
          setError("Token not found");
          setLoading(false);
          return;
        }

        const data = await getBookings(token);
        const normalizedBookings = data.map((b) => ({
          ...b,
          cancelledInfo:
            b.state === "CANCELLED"
              ? { by: "Tourist", reason: "-" }
              : b.cancelledInfo,
        }));

        setBookings(normalizedBookings);
      } catch (err: unknown) {
        if (err instanceof Error) {
          console.error("Failed to fetch bookings", err);
          setError(err.message || "Failed to fetch bookings");
        } else {
          console.error("Failed to fetch bookings", err);
          setError("Failed to fetch bookings");
        }
      } finally {
        setLoading(false);
      }
    };

    fetchBookings();
  }, []);

  if (loading) {
    return (
      <div className="pt-10 text-center text-2xl text-blue-09">
        Loading bookings...
      </div>
    );
  }

  if (error) {
    return (
      <div className="pt-10 text-center text-red-500 text-xl">{error}</div>
    );
  }

  const filteredBookings = bookings.filter((b) => {
    switch (activeTab) {
      case "All tours":
        return true;
      case "Booked":
        return b.state === "BOOKED";
      case "Confirmed":
        return b.state === "CONFIRMED";
      case "Started":
        return b.state === "STARTED";
      case "Finished":
        return b.state === "FINISHED";
      case "Cancelled":
        return b.state === "CANCELLED";
      default:
        return true;
    }
  });

  return (
    <div className="pt-10">
      <TourTabs activeTab={activeTab} onTabChange={setActiveTab} />
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {filteredBookings.length > 0 ? (
          filteredBookings.map((booking) => (
            <BookingCard
              key={booking.id}
              booking={booking}
              onSendReview={(b) => b}
            />
          ))
        ) : (
          <p className="text-3xl flex items-center justify-center col-start-1 col-end-3 h-80 text-blue-09">
            No bookings yet
          </p>
        )}
      </div>
    </div>
  );
};

export default BookingsPage;
