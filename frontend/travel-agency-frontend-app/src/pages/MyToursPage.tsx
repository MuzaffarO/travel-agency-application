import { useEffect, useState } from "react";
import MyTourCard from "../components/MyTourCard/MyTourCard";
import TourTabs from "../components/Tabs";
import { type Booking, getBookings } from "../services/getBookings";
import CancelTourModal from "../components/modals/CancelTourModal/CancelTourModal";
import UploadDocsModal from "../components/modals/UploadDocsModal";
import FeedbackModal from "../components/modals/FeedbackModal";
import {useNavigate} from "react-router-dom";

const statusMapping: Record<string, string> = {
  BOOKED: "Booked",
  CONFIRMED: "Confirmed",
  STARTED: "Started",
  FINISHED: "Finished",
  CANCELLED: "Cancelled",
};

const MyToursPage = () => {
  // const [selectedBooking, setSelectedBooking] = useState<Booking | null>(null);
  const [bookingForCancel, setBookingForCancel] = useState<Booking | null>(
    null
  );
  const [bookingForFeedback, setBookingForFeedback] = useState<Booking | null>(
    null
  );

  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [activeTab, setActiveTab] = useState("All tours");

  const [uploadDocsBooking, setUploadDocsBooking] = useState<Booking | null>(
    null
  );

  const navigate = useNavigate();

  useEffect(() => {
    const fetchBookings = async () => {
      setLoading(true);
      setError(null);
      try {
        const storedUser = localStorage.getItem("user");
        if (!storedUser) {
          setError("User not found");
          setLoading(false);
          navigate('/login');
          return;
        }

        const user = JSON.parse(storedUser) as { token?: string };
        const token = user.token;

        if (!token) {
          setError("Token not found");
          setLoading(false);
          return;
        }
        console.log(token);
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

  const filteredBookings =
    activeTab === "All tours"
      ? bookings
      : bookings.filter((b) => statusMapping[b.state] === activeTab);

  if (loading) return <div className="pt-10">Loading...</div>;
  if (error) return <p>Error: {error}</p>;

  return (
    <div className="pt-10">
      <TourTabs activeTab={activeTab} onTabChange={setActiveTab} />

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {filteredBookings.length > 0 ? (
          filteredBookings.map((booking) => (
            <MyTourCard
              key={booking.id}
              booking={booking}
              onCancel={(b) => setBookingForCancel(b)}
              onEdit={(b) => console.log("Edit", b.id)}
              onUploadDocuments={() => setUploadDocsBooking(booking)}
              onSendReview={(b) => setBookingForFeedback(b)}
            />
          ))
        ) : (
          <p className="text-3xl flex items-center justify-center col-start-1 col-end-3 h-80 text-blue-09">
            No bookings yet
          </p>
        )}
      </div>

      {bookingForFeedback && (
        <FeedbackModal
          isOpen={!!bookingForFeedback}
          bookingId={bookingForFeedback.id}
          tourId={bookingForFeedback.tourDetails.tourId}
          onClose={() => setBookingForFeedback(null)}
        />
      )}

      {bookingForCancel && (
        <CancelTourModal
          isOpen={!!bookingForCancel}
          bookingId={bookingForCancel.id}
          tourName={bookingForCancel.name}
          startDate={bookingForCancel.tourDetails.date}
          duration={bookingForCancel.tourDetails.duration}
          mealPlan={bookingForCancel.tourDetails.mealPlans}
          guests={bookingForCancel.tourDetails.guests}
          onClose={() => setBookingForCancel(null)}
          onCancelSuccess={(bookingId) => {
            setBookings((prev) =>
              prev.map((b) =>
                b.id === bookingId
                  ? {
                      ...b,
                      cancelledInfo:
                        b.state === "CANCELLED"
                          ? { by: "Tourist", reason: "-" }
                          : b.cancelledInfo,
                    }
                  : b
              )
            );
            setBookingForCancel(null);
          }}
        />
      )}

      {uploadDocsBooking && (
        <UploadDocsModal
          booking={uploadDocsBooking}
          onClose={() => setUploadDocsBooking(null)}
        />
      )}
    </div>
  );
};

export default MyToursPage;
