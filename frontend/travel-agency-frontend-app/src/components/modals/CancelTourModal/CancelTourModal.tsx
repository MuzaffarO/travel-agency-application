import { useState } from "react";
import Modal from "../../../ui/Modal";
import Button from "../../Button";
import { cancelBooking } from "../../../services/CancelBooking";

type CancelTourProps = {
  bookingId: string;
  tourName: string;
  startDate: string;
  duration: string;
  mealPlan: string;
  guests: string;
  onClose: () => void;
  isOpen: boolean;
  onCancelSuccess?: (bookingId: string) => void;
};

const CancelTourModal: React.FC<CancelTourProps> = ({
  bookingId,
  tourName,
  startDate,
  duration,
  mealPlan,
  guests,
  onClose,
  isOpen,
  onCancelSuccess,
}) => {
  const [isExpired, setIsExpired] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleCancelClick = async () => {
    const tourStart = new Date(startDate);
    const today = new Date();
    const diffDays = Math.floor(
      (tourStart.getTime() - today.getTime()) / (1000 * 60 * 60 * 24)
    );

    if (!isExpired) {
      setIsExpired(true);

      if (diffDays <= 10) {
        setError(
          "Free cancellation period is over, charges are non-refundable."
        );
      } else {
        setError(
          "Please note, that the free cancelation period for this booking is over, the charges are non-refundable."
        );
      }

      return;
    }

    setLoading(true);
    setError(null);

    try {
      const storedUser = localStorage.getItem("user");
      const token = storedUser ? JSON.parse(storedUser).token : undefined;

      if (!token) throw new Error("User token not found");

      await cancelBooking(bookingId, token);
      onCancelSuccess?.(bookingId);

      console.log("Booking canceled successfully:", bookingId);
      onClose();
    } catch (err: unknown) {
      if (err instanceof Error) setError(err.message);
      else setError("Failed to cancel booking");
    } finally {
      setLoading(false);
    }
  };

  const handleKeepClick = () => {
    onClose();
    setIsExpired(false);
  };

  return (
    <Modal isOpen={isOpen} onClose={handleKeepClick} className="md:w-[544px]">
      <div className="p-6 flex flex-col">
        <h2 className="h2 mb-8">Cancel Booking</h2>
        {isExpired ? (
          <p className="text-sm p-4 bg-red-100 text-red-600 rounded-lg">
            {error ||
              "Free cancellation period is over, charges are non-refundable."}
          </p>
        ) : (
          <p className="text-sm p-4 bg-yellow-100 rounded-lg">
            Free cancellation is possible until 5 January 2025.
          </p>
        )}
        <p className="text-sm mt-3">
          Are you sure you want to cancel your booking at{" "}
          <strong>{tourName}</strong> starting on{" "}
          <strong>
            {startDate} ({duration}), {mealPlan}
          </strong>{" "}
          for <strong>{guests}</strong>?
        </p>
        <div className="self-end flex gap-2 mt-8">
          <Button
            variant="secondary"
            onClick={handleCancelClick}
            disabled={loading}
          >
            {loading ? "Canceling..." : "Cancel the booking"}
          </Button>
          <Button onClick={handleKeepClick} disabled={loading}>
            Keep the booking
          </Button>
        </div>
      </div>
    </Modal>
  );
};

export default CancelTourModal;
