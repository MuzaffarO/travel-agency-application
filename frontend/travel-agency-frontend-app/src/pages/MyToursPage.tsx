import { useEffect, useState } from "react";
import MyTourCard from "../components/MyTourCard/MyTourCard";
import TourTabs from "../components/Tabs";
import { type Booking, getBookings } from "../services/getBookings";
import CancelTourModal from "../components/modals/CancelTourModal/CancelTourModal";
import UploadDocsModal from "../components/modals/UploadDocsModal";
import FeedbackModal from "../components/modals/FeedbackModal";
import EditTourModal from "../components/modals/EditTourModal/EditTourModal";
import { useNavigate } from "react-router-dom";
import { useSelector } from "react-redux";
import type { RootState } from "../store/store";
import { getMyTours } from "../services/getMyTours";
import type { Tour } from "./MainPage";
import Card from "../components/Card";

const statusMapping: Record<string, string> = {
  BOOKED: "Booked",
  CONFIRMED: "Confirmed",
  STARTED: "Started",
  FINISHED: "Finished",
  CANCELLED: "Cancelled",
};

const MyToursPage = () => {
  const { role } = useSelector((state: RootState) => state.user);
  const isTravelAgent = role === "TRAVEL_AGENT" || role === "ADMIN";
  
  // For bookings (CUSTOMER)
  const [bookingForCancel, setBookingForCancel] = useState<Booking | null>(
    null
  );
  const [bookingForFeedback, setBookingForFeedback] = useState<Booking | null>(
    null
  );
  const [bookingForEdit, setBookingForEdit] = useState<Booking | null>(
    null
  );
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [uploadDocsBooking, setUploadDocsBooking] = useState<Booking | null>(
    null
  );

  // For tours (TRAVEL_AGENT/ADMIN)
  const [tours, setTours] = useState<Tour[]>([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [activeTab, setActiveTab] = useState("All tours");

  const navigate = useNavigate();

  useEffect(() => {
    const fetchData = async () => {
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

        if (isTravelAgent) {
          // For TRAVEL_AGENT/ADMIN: fetch their tours
          const data = await getMyTours(token);
          setTours(data);
        } else {
          // For CUSTOMER: fetch their bookings
          const data = await getBookings(token);
          const normalizedBookings = data.map((b) => ({
            ...b,
            cancelledInfo:
              b.state === "CANCELLED"
                ? { by: "Tourist", reason: "-" }
                : b.cancelledInfo,
          }));
          setBookings(normalizedBookings);
        }
      } catch (err: unknown) {
        if (err instanceof Error) {
          console.error("Failed to fetch data", err);
          setError(err.message || "Failed to fetch data");
        } else {
          console.error("Failed to fetch data", err);
          setError("Failed to fetch data");
        }
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [isTravelAgent, navigate]);

  const filteredBookings =
    activeTab === "All tours"
      ? bookings
      : bookings.filter((b) => statusMapping[b.state] === activeTab);

  if (loading) return <div className="pt-10">Loading...</div>;
  if (error) return <p>Error: {error}</p>;

  // For TRAVEL_AGENT/ADMIN: show tours
  if (isTravelAgent) {
    return (
      <div className="pt-10">
        <h2 className="text-2xl font-bold text-blue-09 mb-6">My Tours</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {tours.length > 0 ? (
            tours.map((tour) => (
              <Card
                key={tour.id}
                id={tour.id}
                name={tour.name}
                destination={tour.destination}
                startDate={tour.startDate}
                durations={tour.durations}
                mealPlans={tour.mealPlans}
                price={tour.price}
                rating={tour.rating}
                reviews={tour.reviews}
                imageUrl={tour.imageUrl}
                freeCancelation={tour.freeCancelation}
                tourType={tour.tourType}
              />
            ))
          ) : (
            <p className="text-3xl flex items-center justify-center col-start-1 col-end-4 h-80 text-blue-09">
              No tours yet
            </p>
          )}
        </div>
      </div>
    );
  }

  // For CUSTOMER: show bookings
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
              onEdit={(b) => setBookingForEdit(b)}
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

      {bookingForEdit && (
        <EditTourModal
          booking={bookingForEdit}
          isOpen={!!bookingForEdit}
          onClose={() => setBookingForEdit(null)}
          onEditSuccess={(bookingId) => {
            // Refresh bookings list after successful edit
            const fetchBookings = async () => {
              try {
                const storedUser = localStorage.getItem("user");
                if (!storedUser) return;
                
                const user = JSON.parse(storedUser) as { token?: string };
                const token = user.token;
                
                if (!token) return;
                
                const data = await getBookings(token);
                const normalizedBookings = data.map((b) => ({
                  ...b,
                  cancelledInfo:
                    b.state === "CANCELLED"
                      ? { by: "Tourist", reason: "-" }
                      : b.cancelledInfo,
                }));
                
                setBookings(normalizedBookings);
              } catch (err) {
                console.error("Failed to refresh bookings", err);
              }
            };
            
            fetchBookings();
            setBookingForEdit(null);
          }}
        />
      )}
    </div>
  );
};

export default MyToursPage;
