import { useDispatch } from "react-redux";
import { useFieldArray, useForm } from "react-hook-form";
import { useEffect, useState } from "react";
import Modal from "../../../ui/Modal";
import { Star, Calendar, Utensils } from "lucide-react";
import Input from "../../../ui/Input";
import Button from "../../Button";
import IconDropdown from "../../IconDropdown";
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
import GuestsDropdown from "../../GuestsDropdown";
import { useAppSelector } from "../../../store/hooks/useAppSelector";
import { bookTour } from "../../../services/bookTour";
import { setGuests } from "../../../store/guests/guestsSlice";
import axios from "axios";
import { BACK_URL } from "../../../constants";

type TourData = {
  id: string;
  startDates: string[];
  durations: string[];
  mealPlans: string[];
};

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

  const [tourData, setTourData] = useState<TourData | null>(null);
  const [selectedDate, setSelectedDate] = useState<string>("");
  const [selectedMealPlan, setSelectedMealPlan] = useState<string>("");
  const [fetchingTour, setFetchingTour] = useState(false);

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

  // Fetch tour details when modal opens
  useEffect(() => {
    if (isOpen && modalProps && isBookingModalProps(modalProps) && modalProps.tourId) {
      setFetchingTour(true);
      axios
        .get<TourData>(`${BACK_URL}/tours/${modalProps.tourId}`)
        .then((response) => {
          setTourData(response.data);
          if (response.data.startDates?.[0]) {
            setSelectedDate(response.data.startDates[0]);
          }
          if (response.data.mealPlans?.[0]) {
            setSelectedMealPlan(response.data.mealPlans[0]);
          }
        })
        .catch((err) => {
          console.error("Failed to fetch tour data", err);
        })
        .finally(() => {
          setFetchingTour(false);
        });
    }
  }, [isOpen, modalProps]);

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
  } = modalProps;

  const createDateOptions = () => {
    if (!tourData?.startDates) return [];
    return tourData.startDates.map((date) => {
      try {
        return new Date(date).toLocaleDateString("en-US", {
          year: "numeric",
          month: "short",
          day: "numeric",
        });
      } catch {
        return date;
      }
    });
  };

  const getCurrentDateLabel = () => {
    if (!selectedDate) return "Select date";
    try {
      return new Date(selectedDate).toLocaleDateString("en-US", {
        year: "numeric",
        month: "short",
        day: "numeric",
      });
    } catch {
      return selectedDate;
    }
  };

  const handleDateSelect = (option: string) => {
    if (!tourData?.startDates) return;
    
    const matchingDate = tourData.startDates.find((date) => {
      try {
        const formatted = new Date(date).toLocaleDateString("en-US", {
          year: "numeric",
          month: "short",
          day: "numeric",
        });
        return formatted === option;
      } catch {
        return date === option;
      }
    });
    
    if (matchingDate) {
      setSelectedDate(matchingDate);
    }
  };

  const handleMealPlanSelect = (option: string) => {
    setSelectedMealPlan(option);
  };

  const mealPlanKeyMap: Record<string, string> = {
    "Breakfast (BB)": "BB",
    "Half-board (HB)": "HB",
    "Full-board (FB)": "FB",
    "All inclusive (AI)": "AI",
  };

  const onSubmit = async (formData: BookingFormData) => {
    if (!selectedDate || !selectedMealPlan) {
      alert("Please select a date and meal plan");
      return;
    }

    const mealPlanCode = mealPlanKeyMap[selectedMealPlan] || selectedMealPlan;

    const requestBody = {
      userId: user.userName,
      tourId,
      date: selectedDate,
      duration: durations?.[0] || "7 days",
      mealPlan: mealPlanCode,
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
            startDate: selectedDate,
            duration: durations?.[0] || "7 days",
            mealPlan: selectedMealPlan,
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
            
            {fetchingTour ? (
              <p>Loading tour details...</p>
            ) : (
              <>
                <div className="flex flex-col gap-2">
                  <label className="body-bold">Start Date</label>
                  <IconDropdown
                    icon={<Calendar size={16} />}
                    options={createDateOptions()}
                    defaultValue={getCurrentDateLabel()}
                    onSelect={handleDateSelect}
                    placeholder="Select date"
                  />
                </div>

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

                <div className="flex flex-col gap-2">
                  <label className="body-bold">Meal Plan</label>
                  <IconDropdown
                    icon={<Utensils size={16} />}
                    options={tourData?.mealPlans || []}
                    defaultValue={selectedMealPlan || "Select meal plan"}
                    onSelect={handleMealPlanSelect}
                    placeholder="Select meal plan"
                  />
                </div>
              </>
            )}
          </div>

          <p className="text-[14px] font-extrabold self-end mt-3">
            Total price: ${priceFrom}
          </p>
        </div>

        <Button disabled={fetchingTour}>Book the tour</Button>
      </form>
    </Modal>
  );
};

export default BookingFormModal;
