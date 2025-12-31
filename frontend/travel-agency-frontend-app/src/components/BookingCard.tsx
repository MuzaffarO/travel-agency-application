import defaultTourImage from "../assets/default-tour.png";
import Button from "./Button";
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
import type { Booking } from "../services/getBookings";
import StatusBar from "./StatusBar";
import Modal from "../ui/Modal";
import { useState } from "react";
import IconDropdown from "./IconDropdown";
import axios from "axios";
import { BACK_URL } from "../constants";
import toast from "react-hot-toast";
import SuccessToast from "../ui/SuccessToast";

type BookingCardProps = {
  booking: Booking;
  onSendReview: (booking: Booking) => void;
};

const BookingCard = ({ booking }: BookingCardProps) => {
  const { name, destination, tourImageUrl, tourDetails, travelAgent, state } =
    booking;

  const [isCancelModalOpen, setIsCancelModalOpen] = useState(false);
  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
  const [isDocumentsModalOpen, setIsDocumentsModalOpen] = useState(false);
  const [cancellationReason, setCancellationReason] = useState(
    "Customer's Emergency"
  );
  const [isCancelling, setIsCancelling] = useState(false);
  const [isConfirming, setIsConfirming] = useState(false);

  const paymentDocs = booking.customerDetails?.documents?.payments || [];
  const guestDocs = booking.customerDetails?.documents?.guestDocuments || [];

  const onCancelModalClose = () => setIsCancelModalOpen(false);
  const onConfirmModalClose = () => setIsConfirmModalOpen(false);
  const onDocumentsModalClose = () => setIsDocumentsModalOpen(false);

  const handleCancelBooking = async () => {
    try {
      setIsCancelling(true);
      const user = JSON.parse(localStorage.getItem("user") || "{}");
      const token = user.token;

      await axios.delete(`${BACK_URL}/bookings/${booking.id}`, {
        headers: { Authorization: `Bearer ${token}` },
        data: { cancellationReason },
      });
      toast.custom(
        (t) => (
          <SuccessToast
            t={t}
            message="Booking cancelled successfully."
            title="Success"
          />
        ),
        { duration: 5000 }
      );
      setIsCancelModalOpen(false);
      window.location.reload();
    } catch (error) {
      console.error("Failed to cancel booking:", error);
      alert("Failed to cancel booking. Please try again.");
    } finally {
      setIsCancelling(false);
    }
  };

  const handleConfirmBooking = async () => {
    try {
      setIsConfirming(true);
      const user = JSON.parse(localStorage.getItem("user") || "{}");
      const token = user.token;

      await axios.post(
        `${BACK_URL}/bookings/${booking.id}/confirm`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );
      toast.custom(
        (t) => (
          <SuccessToast
            t={t}
            message="Booking confirmed successfully."
            title="Success"
          />
        ),
        { duration: 5000 }
      );
      setIsConfirmModalOpen(false);
      window.location.reload();
    } catch (error) {
      console.error("Failed to confirm booking:", error);
      toast.error("Failed to confirm booking. Please try again.", {
        duration: 5000,
      });
    } finally {
      setIsConfirming(false);
    }
  };

  const formattedFreeCancellationDate = booking.freeCancellationUntil
    ? new Date(booking.freeCancellationUntil).toLocaleDateString("en-US", {
        year: "numeric",
        month: "long",
        day: "numeric",
      })
    : null;

  const allDocuments: { name: string; url: string }[] = [];
  const payments = booking.customerDetails?.documents?.payments || [];
  const guests = booking.customerDetails?.documents?.guestDocuments || [];

  payments.forEach((doc, i) =>
    allDocuments.push({ name: `Payment ${i + 1}`, url: doc.fileUrl })
  );
  guests.forEach((guest) =>
    guest.documents?.forEach((doc) =>
      allDocuments.push({ name: doc.fileName, url: doc.fileUrl })
    )
  );

  return (
    <div className="rounded-xl bg-white shadow-card max-w-2xl p-6 gap-6 flex flex-col">
      <Modal isOpen={isCancelModalOpen} onClose={onCancelModalClose}>
        <div className="p-6 w-[544px]">
          <h2 className="h2 text-blue-09 pb-8">Cancel</h2>
          <div>
            <h3 className="h3 text-blue-09 pb-2">Booking details</h3>
            <div className="flex flex-col gap-1 pb-4">
              <div className="flex">
                <p className="body-bold text-blue-09 w-[140px]">Customer:</p>
                <p className="body text-blue-09">
                  {booking.customerDetails?.name}
                </p>
              </div>
              <div className="flex">
                <p className="body-bold text-blue-09 w-[140px]">
                  Contact email:
                </p>
                <p className="body text-blue-09">
                  {booking.customerDetails?.email}
                </p>
              </div>
              <div className="flex">
                <p className="body-bold text-blue-09 w-[140px]">Tour name:</p>
                <p className="body text-blue-09">{booking.name}</p>
              </div>
              <div className="flex">
                <p className="body-bold text-blue-09 w-[140px]">Location:</p>
                <p className="body text-blue-09">{booking.destination}</p>
              </div>
              <div className="flex">
                <p className="body-bold text-blue-09 w-[140px]">Start date:</p>
                <p className="body text-blue-09">{booking.tourDetails.date}</p>
              </div>
              <div className="flex">
                <p className="body-bold text-blue-09 w-[140px]">Duration:</p>
                <p className="body text-blue-09">{booking.duration}</p>
              </div>
              <div className="flex">
                <p className="body-bold text-blue-09 w-[140px]">Meal plan:</p>
                <p className="body text-blue-09">
                  {booking.tourDetails.mealPlans}
                </p>
              </div>
            </div>
            <div className="pb-8">
              <p className="body-bold text-blue-09">Cancellation reason</p>
              <IconDropdown
                defaultValue="Customer's Emergency"
                options={[
                  "Customer's Emergency",
                  "Hotel Emergency",
                  "Safety Concerns",
                  "Insufficient Bookings",
                ]}
                onSelect={(value) => setCancellationReason(value)}
              />
            </div>
            <div className="flex gap-2 justify-end">
              <Button
                onClick={() => setIsCancelModalOpen(false)}
                variant="secondary"
                disabled={isCancelling}
              >
                Keep the booking
              </Button>
              <Button
                variant="primary"
                onClick={handleCancelBooking}
                disabled={isCancelling}
              >
                {isCancelling ? "Cancelling..." : "Cancel the booking"}
              </Button>
            </div>
          </div>
        </div>
      </Modal>

      <Modal isOpen={isConfirmModalOpen} onClose={onConfirmModalClose}>
        <div className="p-6 w-[544px]">
          <div className="flex items-center gap-2">
            <h2 className="h2 text-blue-09">{booking.name}</h2>
          </div>
          <p className="caption text-[#677883] pb-6">{booking.destination}</p>

          <div>
            <h3 className="h3 text-blue-09 pb-2">Booking details</h3>
            <div className="flex flex-col gap-1 pb-4">
              <div className="flex">
                <p className="body-bold text-blue-09 w-[140px]">Customer:</p>
                <p className="body text-blue-09">
                  {booking.customerDetails?.name}
                </p>
              </div>
              <div className="flex">
                <p className="body-bold text-blue-09 w-[140px]">
                  Contact email:
                </p>
                <p className="body text-blue-09">
                  {booking.customerDetails?.email}
                </p>
              </div>
              <div className="flex">
                <p className="body-bold text-blue-09 w-[140px]">Start date:</p>
                <p className="body text-blue-09">{booking.tourDetails.date}</p>
              </div>
              <div className="flex">
                <p className="body-bold text-blue-09 w-[140px]">Duration:</p>
                <p className="body text-blue-09">{booking.duration}</p>
              </div>
              <div className="flex">
                <p className="body-bold text-blue-09 w-[140px]">Meal plan:</p>
                <p className="body text-blue-09">
                  {booking.tourDetails.mealPlans}
                </p>
              </div>
            </div>
            {booking.tourDetails.documents !== "0 items" && (
              <>
                <h3 className="h3 text-blue-09 pb-2">Documents</h3>

                {paymentDocs.length === 0 && guestDocs.length === 0 ? (
                  <p className="body text-blue-09 pb-4">
                    No documents uploaded
                  </p>
                ) : (
                  <>
                    {paymentDocs.length > 0 && (
                      <div className="pb-4">
                        <p className="body-bold text-blue-09 pb-2">Passport:</p>
                        {paymentDocs.map((doc, index) => (
                          <a
                            key={index}
                            href={doc.fileUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="body text-[#0EA5E9] underline block"
                          >
                            Payment document {index + 1}
                          </a>
                        ))}
                      </div>
                    )}

                    {guestDocs.length > 0 && (
                      <div className="pb-4">
                        <p className="body-bold text-blue-09 pb-2">Payment:</p>
                        {guestDocs.map((guest, guestIndex) => (
                          <div key={guestIndex} className="pb-2">
                            <p className="body text-blue-09">
                              {guest.userName}:
                            </p>
                            {guest.documents?.map((doc, docIndex) => (
                              <a
                                key={docIndex}
                                href={doc.fileUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="body text-[#0EA5E9] underline block ml-4"
                              >
                                {doc.fileName}
                              </a>
                            ))}
                          </div>
                        ))}
                      </div>
                    )}
                  </>
                )}
              </>
            )}
            <p className="body text-blue-09 body-bold justify-self-end pb-6">
              Total price: {booking.tourDetails.totalPrice}
            </p>

            <Button
              variant="primary"
              onClick={handleConfirmBooking}
              disabled={isConfirming}
              className="w-full"
            >
              {isConfirming ? "Confirming..." : "Confirm"}
            </Button>
          </div>
        </div>
      </Modal>

      <Modal isOpen={isDocumentsModalOpen} onClose={onDocumentsModalClose}>
        <div className="p-6 w-[544px]">
          <h2 className="h2 text-blue-09 pb-4">Documents</h2>
          {allDocuments.length === 0 ? (
            <p className="body text-blue-09">No documents uploaded</p>
          ) : (
            <ul className="flex flex-col gap-3">
              {allDocuments.map((doc, idx) => (
                <li className="flex justify-between" key={idx}>
                  <div className="flex gap-2">
                    <File size={24} className="text-blue-09" />
                    <a
                      href={doc.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="body text-blue-09"
                    >
                      {doc.name}
                    </a>
                  </div>
                  <a
                    href={doc.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="button-text text-blue-05"
                  >
                    Download
                  </a>
                </li>
              ))}
            </ul>
          )}
        </div>
      </Modal>
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
              <Wallet size={16} /> Total price: {tourDetails.totalPrice}
            </p>
            <p
              className={`body text-blue-09 flex items-center gap-2 ${tourDetails.documents !== "0 items" && "underline cursor-pointer"}`}
              onClick={() => {
                if (tourDetails.documents !== "0 items")
                  setIsDocumentsModalOpen(true);
              }}
            >
              <File size={16} /> Documents uploaded: {tourDetails.documents}
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
        <div className="flex justify-between items-center w-full">
          {booking.state === "BOOKED" ||
          (booking.state === "CONFIRMED" && formattedFreeCancellationDate) ? (
            <p className="text-green-04 body">
              Free cancellation until {formattedFreeCancellationDate}
            </p>
          ) : null}
          {booking.state === "CANCELLED" ? (
            <div></div>
          ) : (
            <div className="mt-auto ml-auto flex justify-end gap-2">
              {booking.state === "BOOKED" && (
                <>
                  <Button
                    variant="secondary"
                    onClick={() => setIsCancelModalOpen(true)}
                  >
                    Cancel
                  </Button>
                  <Button
                    variant="primary"
                    onClick={() => setIsConfirmModalOpen(true)}
                  >
                    Check and confirm
                  </Button>
                </>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default BookingCard;
