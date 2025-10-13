import { useDispatch } from "react-redux";
import { useFieldArray, useForm } from "react-hook-form";
import { useEffect } from "react";
import Modal from "../../../ui/Modal";
import { Star } from "lucide-react";
import Input from "../../../ui/Input";
import Button from "../../Button";
import type { RootState } from "../../../store/store";
import {
  closeModal,
  openModal,
  type BookingModalProps,
} from "../../../store/modal/modalSlice";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  bookingFormSchema,
  type BookingFormData,
} from "../../../schemas/bookingSchema";
import DateDropdown from "../../DateDropdown";
import MealDropdown from "../../MealDropdown";
import GuestsDropdown from "../../GuestsDropdown";
import { useAppSelector } from "../../../store/hooks/useAppSelector";
import { bookTour } from "../../../services/bookTour";
import { setGuests } from "../../../store/guests/guestsSlice";

const isBookingModalProps = (props: unknown): props is BookingModalProps => {
  return (
    typeof props === "object" &&
    props !== null &&
    "tourId" in props &&
    "destination" in props
  );
};

const BookingFormModal = () => {
  const dispatch = useDispatch();
  const { activeModal, modalProps } = useAppSelector(
    (state: RootState) => state.modal
  );
  const isOpen = activeModal === "bookingForm";

  const user = useAppSelector((state: RootState) => state.user);
  const guests = useAppSelector((state: RootState) => state.guests);

  const {
    register,
    handleSubmit,
    control,
    setValue,
    getValues,
    formState: { errors },
  } = useForm<BookingFormData>({
    defaultValues: { guests: [] },
    resolver: zodResolver(bookingFormSchema),
  });

  const { fields, append } = useFieldArray({ control, name: "guests" });

  useEffect(() => {
    if (!isOpen) return;

    const currentGuests = getValues("guests") || [];

    const newGuests: BookingFormData["guests"] = [];

    for (let i = 0; i < guests.adults; i++) {
      newGuests.push(
        currentGuests[i] || { type: "adult", firstName: "", lastName: "" }
      );
    }

    for (let i = 0; i < guests.children; i++) {
      const idx = guests.adults + i;
      newGuests.push(
        currentGuests[idx] || { type: "child", firstName: "", lastName: "" }
      );
    }

    setValue("guests", newGuests);
  }, [isOpen, guests.adults, guests.children, append, setValue, getValues]);

  if (!isOpen || !modalProps || !isBookingModalProps(modalProps)) return null;

  const {
    tourName,
    rating,
    destination,
    priceFrom,
    tourId,
    durations,
    startDate,
    mealPlans,
  } = modalProps;

  const onSubmit = async (formData: BookingFormData) => {
    const requestBody = {
      userId: user.userName,
      tourId,
      date: startDate ?? new Date().toISOString().split("T")[0],
      duration: durations?.[0] || "7 days",
      mealPlan: mealPlans?.[0] || "Standard",
      guests: {
        adult: guests.adults,
        children: guests.children,
      },
      personalDetails: formData.guests.map((g, i) => ({
        firstName: g.firstName || `Guest ${i + 1}`,
        lastName: g.lastName || "",
      })),
    };

    try {
      await bookTour(requestBody, user.token);
      dispatch(closeModal());
      dispatch(
        openModal({
          name: "confirmReserve",
          props: {
            tourName,
            startDate: startDate ?? new Date().toISOString().split("T")[0],
            duration: durations?.[0] || "7 days",
            mealPlan: mealPlans?.[0] || "Standard",
            adults: guests.adults,
            children: guests.children,
          },
        })
      );
    } catch (err) {
      console.error(err);
      alert("Failed to book the tour");
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={() => dispatch(closeModal())}>
      <form
        onSubmit={handleSubmit(onSubmit)}
        className="grid gap-8 overflow-y-auto max-h-[95vh] p-6"
      >
        <div className="flex flex-col">
          <div className="flex items-center gap-[18px]">
            <h2 className="h2">{tourName}</h2>
            <p className="caption flex justify-end items-center gap-1 pb-1">
              <Star size={16} fill="#0b3857" /> {rating.toFixed(1)}
            </p>
          </div>
          <p className="text-[12px]">{destination}</p>
        </div>

        <div className="flex flex-col gap-4">
          {fields.map((field, index) => (
            <div key={field.id} className="flex flex-col gap-2">
              <h3 className="h3">
                {fields.length === 1
                  ? "Personal Details"
                  : `Personal Details (Customer ${index + 1})`}
              </h3>
              <div className="flex gap-4">
                <Input
                  id={`firstName-${index}`}
                  {...register(`guests.${index}.firstName`)}
                  label="First name"
                  placeholder="Enter your first name"
                  helperText="e.g. Johnson"
                  error={errors.guests?.[index]?.firstName?.message}
                />
                <Input
                  id={`lastName-${index}`}
                  {...register(`guests.${index}.lastName`)}
                  label="Last name"
                  placeholder="Enter your last name"
                  helperText="e.g. Doe"
                  error={errors.guests?.[index]?.lastName?.message}
                />
              </div>
            </div>
          ))}

          <div className="flex flex-col gap-3">
            <h3 className="h3 mb-1">Tour details</h3>
            <DateDropdown />
            <GuestsDropdown
              classNames=""
              onChange={({ adults, children }) => {
                dispatch(setGuests({ adults, children }));

                const newGuests = [
                  ...Array.from({ length: adults }, () => ({
                    type: "adult" as const,
                    firstName: "",
                    lastName: "",
                  })),
                  ...Array.from({ length: children }, () => ({
                    type: "child" as const,
                    firstName: "",
                    lastName: "",
                  })),
                ];

                setValue("guests", newGuests);
              }}
            />
            <MealDropdown />
          </div>

          <p className="text-[14px] font-extrabold self-end mt-3">
            Total price: ${priceFrom}
          </p>
        </div>

        <Button>Book the tour</Button>
      </form>
    </Modal>
  );
};

export default BookingFormModal;
