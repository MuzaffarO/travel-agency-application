import defaultTourImage from "../../assets/default-tour.png";
import Button from "../Button";
import {
  MapPin,
  Calendar,
  Utensils,
  Contact,
  File,
  Phone,
  Wallet,
  MessageCircle,
  Mail,
} from "lucide-react";
import type { Booking } from "../../services/getBookings";
import StatusBar from "../StatusBar";
import { useEffect, useState } from "react";
import {
  fetchBookingDocuments,
  type BookingDocuments,
} from "../../services/getDocuments";

type MyTourCardProps = {
  booking: Booking;
  onCancel?: (booking: Booking) => void;
  onEdit?: (booking: Booking) => void;
  onUploadDocuments?: (booking: Booking) => void;
  onSendReview: (booking: Booking) => void;
};

const MyTourCard = ({
  booking,
  onCancel,
  onEdit,
  onSendReview,
  onUploadDocuments,
}: MyTourCardProps) => {
  const {
    name,
    destination,
    tourImageUrl,
    tourDetails,
    travelAgent,
    cancelledInfo,

    state,
  } = booking;

  const [documents, setDocuments] = useState<BookingDocuments | null>(null);
  const [loadingDocs, setLoadingDocs] = useState(true);

  useEffect(() => {
    const user = JSON.parse(localStorage.getItem("user") || "{}");
    const token = user?.token;
    if (!token) return;

    setLoadingDocs(true);
    fetchBookingDocuments(booking.id, token)
      .then((data) => setDocuments(data))
      .catch((err) => console.error("Failed to load documents", err))
      .finally(() => setLoadingDocs(false));
  }, [booking.id]);

  const hasDocuments =
    !!documents &&
    (documents.payments.length > 0 ||
      documents.guestDocuments.some((g) => g.documents.length > 0));

  return (
    <div className="rounded-xl bg-white shadow-card max-w-2xl p-6 gap-6 flex flex-col">
      <div className="flex-1 flex flex-col">
        <div className="pb-6">
          <StatusBar bookingStatus={state} />
        </div>
        <div className="flex gap-4 pb-6">
          <img
            className="w-[120px] h-[64px] object-cover rounded-xl"
            src={tourImageUrl || defaultTourImage}
            alt={name}
          />
          <div>
            <p className="h3">{name}</p>
            <p className="caption text-[#677883] flex gap-2">
              <MapPin size={16} /> {destination}
            </p>
          </div>
        </div>

        <div className="flex gap-6 pb-6">
          <div className="flex-1 flex flex-col gap-1">
            <p className="body-bold pb-2">Tour details</p>
            <p className="body text-blue-09 flex items-center gap-2">
              <Calendar size={16} /> {tourDetails.date}
            </p>
            <p className="body text-blue-09 flex items-center gap-2">
              <Utensils size={16} /> {tourDetails.mealPlans}
            </p>
            <p className="body text-blue-09 flex items-center gap-2">
              <Contact size={16} /> {tourDetails.guests}
            </p>
            <p className="body text-blue-09 flex items-center gap-2">
              <Wallet size={16} /> Total price {tourDetails.totalPrice}
            </p>
            <p className="body text-blue-09 flex items-center gap-2">
              <File size={16} />
              {loadingDocs
                ? "Loading documents..."
                : documents
                  ? `Documents uploaded: ${
                      documents.payments.length +
                      documents.guestDocuments.flatMap((g) => g.documents)
                        .length
                    }`
                  : "No documents uploaded"}
            </p>
          </div>

          <div className="flex-1 flex flex-col gap-1">
            <p className="body-bold pb-2">Travel agent</p>
            <p className="body text-blue-09 flex items-center gap-2">
              <Contact size={16} /> {travelAgent.name}
            </p>
            <p className="body text-blue-09 flex items-center gap-2">
              <Mail size={16} /> {travelAgent.email}
            </p>
            <p className="body text-blue-09 flex items-center gap-2">
              <Phone size={16} /> {travelAgent.phone || "N/A"}
            </p>
            <p className="body text-blue-09 flex items-center gap-2">
              <MessageCircle size={16} /> {travelAgent.messenger || "N/A"}
            </p>
          </div>
        </div>

        {cancelledInfo ? (
          <div className="flex flex-col gap-2 mt-2">
            <p className="body-bold text-red-04">
              Cancelled by: <span className="body">{cancelledInfo.by}</span>
            </p>
            <p className="body-bold text-red-04">
              Reason:{" "}
              <span className="body">{cancelledInfo.reason || "-"}</span>
            </p>
          </div>
        ) : (
          <div className="mt-auto flex justify-end gap-2">
            {(booking.state === "BOOKED" || booking.state === "CONFIRMED") && (
              <>
                <Button variant="secondary" onClick={() => onCancel?.(booking)}>
                  Cancel
                </Button>
                <Button variant="secondary" onClick={() => onEdit?.(booking)}>
                  Edit
                </Button>
              </>
            )}

            {!loadingDocs &&
              !hasDocuments &&
              (booking.state === "BOOKED" || booking.state === "CONFIRMED") && (
                <Button onClick={() => onUploadDocuments?.(booking)}>
                  {booking.state === "BOOKED" && "Upload documents"}
                  {booking.state === "CONFIRMED" && "Update documents"}
                </Button>
              )}

            {booking.state === "STARTED" && (
              <Button onClick={() => onSendReview?.(booking)}>
                Give feedback
              </Button>
            )}
            {booking.state === "FINISHED" && (
              <Button onClick={() => onSendReview?.(booking)}>
                Update feedback
              </Button>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default MyTourCard;
