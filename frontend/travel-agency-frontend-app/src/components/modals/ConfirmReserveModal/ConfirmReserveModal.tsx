import { useDispatch, useSelector } from "react-redux";
import Modal from "../../../ui/Modal";
import type { RootState } from "../../../store/store";
import {
  closeModal,
  type ConfirmReserveProps,
} from "../../../store/modal/modalSlice";

const ConfirmReserveModal = () => {
  const dispatch = useDispatch();
  const { activeModal, modalProps } = useSelector(
    (state: RootState) => state.modal
  );

  const isOpen = activeModal === "confirmReserve";

  if (!isOpen || !modalProps) return null;

  const { tourName, startDate, duration, mealPlan, adults } =
    modalProps as ConfirmReserveProps;

  return (
    <Modal
      isOpen={isOpen}
      onClose={() => dispatch(closeModal())}
      className="md:w-[544px]"
    >
      <div className="p-6">
        <h2 className="h2 mb-8">Booking confirmation</h2>
        <p className="text-[14px] p-4 bg-yellow-01 rounded-[8px]">
          Free cancellation is possible until January 5, 2025.
        </p>
        <p className="text-[14px] my-3">
          You have booked a tour at <strong>{tourName}</strong>, starting date{" "}
          <strong>
            {startDate} ({duration}), {mealPlan}
          </strong>{" "}
          for{" "}
          <strong>
            {adults} adult{adults > 1 ? "s" : ""}
          </strong>{" "}
          successfully.
        </p>

        <p className="text-[14px]">
          Please upload your travel documents to the booking on the "My Tours"
          page and wait for the Travel Agent to contact you.
        </p>
      </div>
    </Modal>
  );
};

export default ConfirmReserveModal;
