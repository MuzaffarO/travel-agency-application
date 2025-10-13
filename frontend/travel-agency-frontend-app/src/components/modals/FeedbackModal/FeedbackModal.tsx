import { useState } from "react";
import { useDispatch } from "react-redux";
import Modal from "../../../ui/Modal";
import StarRating from "../../../ui/StarRating";

import Button from "../../Button";
import { closeModal } from "../../../store/modal/modalSlice";
import { useAppSelector } from "../../../store/hooks/useAppSelector.ts";
import {
  type ReviewRequest,
  sendReview,
} from "../../../services/sendReview.ts";

type FeedbackModalProps = {
  isOpen: boolean;
  onClose: () => void;
  bookingId: string;
  tourId: string;
};

const FeedbackModal = ({
    isOpen,
    onClose,
    bookingId,
    tourId,
}: FeedbackModalProps) => {
  const dispatch = useDispatch();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [rating, setRating] = useState(0);
  const [reviewContent, setReviewContent] = useState<string>("");

  const token = useAppSelector((state) => state.user.token);

  const handleClose = () => {
    onClose();
    dispatch(closeModal());
  };

  const validateFeedback = () => {
    if (rating === 0) {
      return "Please select a rating.";
    }
    if (rating <= 3 && reviewContent.trim() === "") {
      return "Comment is required for ratings 1-3 stars.";
    }
    return null;
  };

  const handleSendReview = async () => {
    const validationError = validateFeedback();

    if (validationError) {
      setError(validationError);
      return;
    }

    const requestBody: ReviewRequest = {
      bookingId,
      rate: rating,
      comment: reviewContent,
    };

    setLoading(true);
    setError(null);

    try {
      await sendReview(tourId, requestBody, token);
      console.log("Review sent successfully:", bookingId);
      onClose();
    } catch (err) {
      if (err instanceof Error) setError(err.message);
      else setError("Failed to send the review");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={handleClose} className="md:w-[544px]">
      <div className="p-6 flex flex-col">
        <h2 className="h2 mb-8">Feedback</h2>
        <p className="body-bold mb-1">Please rate your experience*</p>
        <div className="flex items-center justify-between gap-4">
          <StarRating rating={rating} setRating={setRating} />
          <p className="text-[14px]">{rating}/5 stars</p>
        </div>
        <label className="block mt-4 font-bold text-[14px]">Comment</label>
        <textarea
          value={reviewContent}
          onChange={(e) => setReviewContent(e.target.value)}
          placeholder="Add your comments"
          maxLength={500}
          rows={4}
          className="w-full p-4 mt-1 border border-grey-05 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-05"
        />
        {error && <p className="text-red-500 text-sm mt-2">{error}</p>}
        <div className="flex gap-2 mt-8 self-end">
          <Button variant="secondary" onClick={handleClose}>
            Cancel
          </Button>
          <Button onClick={handleSendReview} disabled={loading}>
            Submit
          </Button>
        </div>
      </div>
    </Modal>
  );
};

export default FeedbackModal;
