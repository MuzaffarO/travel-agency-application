import { useState, useEffect } from "react";
import { useFieldArray, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import Modal from "../../../ui/Modal";
import Button from "../../Button";
import Input from "../../../ui/Input";
import IconDropdown from "../../IconDropdown";
import { Calendar, Utensils, UsersRound } from "lucide-react";
import { updateBooking, type UpdateBookingRequest } from "../../../services/updateBooking";
import { type Booking } from "../../../services/getBookings";
import { getBooking } from "../../../services/getBooking";
import { bookingFormSchema, type BookingFormData } from "../../../schemas/bookingSchema";
import axios from "axios";
import { BACK_URL } from "../../../constants";

type TourData = {
  id: string;
  startDates: string[];
  durations: string[];
  mealPlans: string[];
  guestQuantity: {
    adultsMaxValue: number;
    childrenMaxValue: number;
  };
};

type EditTourModalProps = {
  booking: Booking;
  isOpen: boolean;
  onClose: () => void;
  onEditSuccess?: (bookingId: string) => void;
};

const mealPlanKeyMap: Record<string, string> = {
  "Breakfast (BB)": "BB",
  "Half-board (HB)": "HB",
  "Full-board (FB)": "FB",
  "All inclusive (AI)": "AI",
  BB: "BB",
  HB: "HB",
  FB: "FB",
  AI: "AI",
};

const mealPlanLabelMap: Record<string, string> = {
  BB: "Breakfast (BB)",
  HB: "Half-board (HB)",
  FB: "Full-board (FB)",
  AI: "All inclusive (AI)",
};

const EditTourModal: React.FC<EditTourModalProps> = ({
  booking,
  isOpen,
  onClose,
  onEditSuccess,
}) => {
  const [fullBookingData, setFullBookingData] = useState<Booking | null>(null);
  const [tourData, setTourData] = useState<TourData | null>(null);
  const [loading, setLoading] = useState(false);
  const [fetchingBooking, setFetchingBooking] = useState(false);
  const [fetchingTour, setFetchingTour] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isInitialized, setIsInitialized] = useState(false);

  // Form state
  const [selectedDate, setSelectedDate] = useState<string>("");
  const [selectedMealPlan, setSelectedMealPlan] = useState<string>("");
  const [selectedAdults, setSelectedAdults] = useState<number>(1);
  const [selectedChildren, setSelectedChildren] = useState<number>(0);

  const {
    register,
    handleSubmit,
    control,
    setValue,
    getValues,
    formState: { errors },
    reset,
  } = useForm<BookingFormData>({
    defaultValues: { guests: [] },
    resolver: zodResolver(bookingFormSchema),
  });

  const { fields } = useFieldArray({ control, name: "guests" });

  // Reset state when modal closes
  useEffect(() => {
    if (!isOpen) {
      setFullBookingData(null);
      setTourData(null);
      setIsInitialized(false);
      setSelectedDate("");
      setSelectedMealPlan("");
      setSelectedAdults(1);
      setSelectedChildren(0);
      setError(null);
      reset({ guests: [] });
    }
  }, [isOpen, reset]);

  // Fetch full booking details when modal opens
  useEffect(() => {
    if (isOpen && booking?.id && !fullBookingData) {
      setFetchingBooking(true);
      const storedUser = localStorage.getItem("user");
      const token = storedUser ? JSON.parse(storedUser).token : undefined;
      
      if (token) {
        getBooking(booking.id, token)
          .then((fullBooking) => {
            if (fullBooking) {
              setFullBookingData(fullBooking);
            } else {
              // Fallback to the booking prop if fetch fails
              setFullBookingData(booking);
            }
          })
          .catch((err) => {
            console.error("Failed to fetch full booking details:", err);
            // Fallback to the booking prop
            setFullBookingData(booking);
          })
          .finally(() => {
            setFetchingBooking(false);
          });
      } else {
        // No token, use the booking prop
        setFullBookingData(booking);
        setFetchingBooking(false);
      }
    }
  }, [isOpen, booking?.id, fullBookingData]);

  // Fetch tour data after booking data is loaded
  useEffect(() => {
    if (fullBookingData?.tourDetails?.tourId && !tourData && !fetchingTour) {
      setFetchingTour(true);
      axios
        .get<TourData>(`${BACK_URL}/tours/${fullBookingData.tourDetails.tourId}`)
        .then((response) => {
          setTourData(response.data);
        })
        .catch((err) => {
          console.error("Failed to fetch tour data", err);
          setError("Failed to load tour details");
        })
        .finally(() => {
          setFetchingTour(false);
        });
    }
  }, [fullBookingData?.tourDetails?.tourId, tourData, fetchingTour]);

  // Parse guests from booking and initialize form (only once when data is ready)
  useEffect(() => {
    if (fullBookingData && tourData && isOpen && !isInitialized) {
      // Try to parse guests from the guests string (e.g., "John Doe (2 adults, 1 child)")
      const guestsString = fullBookingData.tourDetails.guests || "";
      
      // Try multiple patterns to match guest counts
      // Pattern 1: Parse from tourDetails.guests string (e.g., "John Doe (2 adults, 1 child)" or "John Doe (2 adults)")
      let adultsMatch = guestsString.match(/\((\d+)\s*adult/i);
      let childrenMatch = guestsString.match(/(\d+)\s*child/i);
      
      // Pattern 2: Look in customerDetails.name for "(X adults)" or "(X adults, Y children)"
      if (!adultsMatch && fullBookingData.customerDetails?.name) {
        adultsMatch = fullBookingData.customerDetails.name.match(/\((\d+)\s*adult/i);
        if (!childrenMatch) {
          childrenMatch = fullBookingData.customerDetails.name.match(/(\d+)\s*child/i);
        }
      }
      
      // Pattern 3: Use personalDetails count (total guests = adults + children)
      // If we have personalDetails, we can use that to validate/calculate
      const personalDetailsCount = fullBookingData.customerDetails?.documents?.guestDocuments?.length || 0;
      
      let adults = 1;
      let children = 0;
      
      if (adultsMatch) {
        adults = parseInt(adultsMatch[1], 10);
      }
      
      if (childrenMatch) {
        children = parseInt(childrenMatch[1], 10);
      }
      
      // If we parsed adults but not children, and we have personalDetails count
      // Calculate children: total guests - adults = children
      if (adultsMatch && !childrenMatch && personalDetailsCount > 0) {
        if (personalDetailsCount > adults) {
          children = personalDetailsCount - adults;
        }
      }
      
      // If we couldn't parse from string, try using personalDetails count
      // Assume all are adults if we can't determine
      if (!adultsMatch && personalDetailsCount > 0) {
        adults = personalDetailsCount;
        children = 0;
      }
      
      // Ensure we have at least 1 adult
      if (adults < 1) adults = 1;
      
      setSelectedAdults(adults);
      setSelectedChildren(children);

      // Initialize guest name fields from booking
      // Priority: customerDetails.documents.guestDocuments (most reliable source)
      const initialGuests: BookingFormData["guests"] = [];
      
      // Try to get guest names from customerDetails.documents.guestDocuments first
      // Limit to the actual number of adults + children we parsed
      if (fullBookingData.customerDetails?.documents?.guestDocuments && 
          fullBookingData.customerDetails.documents.guestDocuments.length > 0) {
        const maxGuests = adults + children;
        fullBookingData.customerDetails.documents.guestDocuments
          .slice(0, maxGuests)
          .forEach((guestDoc, i) => {
            const fullName = guestDoc.userName || "";
            const nameParts = fullName.split(" ");
            // Skip if name is empty or just "guest"
            if (fullName && fullName.trim().toLowerCase() !== "guest") {
              initialGuests.push({
                type: i < adults ? "adult" : "child",
                firstName: nameParts[0] || "",
                lastName: nameParts.slice(1).join(" ") || "",
              });
            }
          });
      }
      
      // Fallback to guestsList if available
      if (initialGuests.length === 0 && fullBookingData.guestsList && fullBookingData.guestsList.length > 0) {
        fullBookingData.guestsList.slice(0, adults + children).forEach((guest, i) => {
          const nameParts = (guest.name || "").split(" ");
          initialGuests.push({
            type: i < adults ? "adult" : "child",
            firstName: nameParts[0] || "",
            lastName: nameParts.slice(1).join(" ") || "",
          });
        });
      }
      
      // Final fallback: try to parse from customerDetails.name (might include adult count)
      if (initialGuests.length === 0 && fullBookingData.customerDetails?.name) {
        // Remove the "(X adults)" part if present
        const customerName = fullBookingData.customerDetails.name.replace(/\s*\(\d+\s*adult/i, "").trim();
        const nameParts = customerName.split(" ");
        if (nameParts[0]) {
          initialGuests.push({
            type: "adult",
            firstName: nameParts[0] || "",
            lastName: nameParts.slice(1).join(" ") || "",
          });
        }
      }

      // Fill remaining slots with empty entries
      while (initialGuests.length < adults + children) {
        initialGuests.push({
          type: initialGuests.length < adults ? "adult" : "child",
          firstName: "",
          lastName: "",
        });
      }

      reset({ guests: initialGuests });

      // Set current meal plan - convert from code format if needed
      const mealPlanFromBooking = fullBookingData.tourDetails.mealPlans || "";
      // If it's a code like "BB", convert to label format
      if (mealPlanLabelMap[mealPlanFromBooking]) {
        setSelectedMealPlan(mealPlanLabelMap[mealPlanFromBooking]);
      } else {
        setSelectedMealPlan(mealPlanFromBooking);
      }

      // Parse and set the date
      const bookingDateStr = fullBookingData.tourDetails.date || "";
      const dateMatch = bookingDateStr.match(/^([^\(]+)/);
      if (dateMatch && tourData.startDates) {
        const datePart = dateMatch[1].trim();
        const matchingDate = tourData.startDates.find((isoDate) => {
          try {
            const formatted = new Date(isoDate).toLocaleDateString("en-US", {
              year: "numeric",
              month: "short",
              day: "numeric",
            });
            return formatted === datePart;
          } catch {
            return false;
          }
        });
        if (matchingDate) {
          setSelectedDate(matchingDate);
        } else if (tourData.startDates?.[0]) {
          setSelectedDate(tourData.startDates[0]);
        }
      } else if (tourData.startDates?.[0]) {
        setSelectedDate(tourData.startDates[0]);
      }

      setIsInitialized(true);
    }
  }, [fullBookingData, tourData, isOpen, isInitialized, reset]);

  // Update guest fields when adults/children count changes (only after initialization)
  useEffect(() => {
    if (!isOpen || !isInitialized) return;

    const currentGuests = getValues("guests") || [];
    const newGuests: BookingFormData["guests"] = [];

    // Preserve existing names when possible
    for (let i = 0; i < selectedAdults; i++) {
      newGuests.push(
        currentGuests[i] || { type: "adult", firstName: "", lastName: "" }
      );
    }

    for (let i = 0; i < selectedChildren; i++) {
      const idx = selectedAdults + i;
      newGuests.push(
        currentGuests[idx] || { type: "child", firstName: "", lastName: "" }
      );
    }

    setValue("guests", newGuests, { shouldDirty: false });
  }, [isOpen, isInitialized, selectedAdults, selectedChildren, setValue, getValues]);


  const createDateOptions = () => {
    if (!tourData?.startDates) return [];
    return tourData.startDates.map((date) => {
      try {
        const formattedDate = new Date(date).toLocaleDateString("en-US", {
          year: "numeric",
          month: "short",
          day: "numeric",
        });
        return formattedDate;
      } catch {
        return date;
      }
    });
  };

  const getCurrentDateOption = () => {
    if (!selectedDate) return "";
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

  const createMealPlanOptions = () => {
    return tourData?.mealPlans || [];
  };

  const createAdultOptions = () => {
    if (!tourData) return ["1 adult"];
    const maxAdults = tourData.guestQuantity?.adultsMaxValue || 10;
    return Array.from({ length: maxAdults }, (_, i) => 
      `${i + 1} adult${i > 0 ? "s" : ""}`
    );
  };

  const createChildrenOptions = () => {
    if (!tourData) return ["0 children"];
    const maxChildren = tourData.guestQuantity?.childrenMaxValue || 5;
    return Array.from({ length: maxChildren + 1 }, (_, i) => 
      `${i} child${i !== 1 ? "ren" : ""}`
    );
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

  const handleAdultsSelect = (option: string) => {
    const match = option.match(/(\d+)/);
    if (match) {
      setSelectedAdults(parseInt(match[1], 10));
    }
  };

  const handleChildrenSelect = (option: string) => {
    const match = option.match(/(\d+)/);
    if (match) {
      setSelectedChildren(parseInt(match[1], 10));
    }
  };

  const onSubmit = async (formData: BookingFormData) => {
    if (!tourData) {
      setError("Tour data not loaded");
      return;
    }

    if (!selectedDate || !selectedMealPlan) {
      setError("Please fill in all required fields");
      return;
    }

    if (selectedAdults + selectedChildren === 0) {
      setError("At least one guest is required");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const storedUser = localStorage.getItem("user");
      const token = storedUser ? JSON.parse(storedUser).token : undefined;

      if (!token) {
        throw new Error("User token not found");
      }

      // Get current duration from booking (duration is not editable per requirements)
      const currentBooking = fullBookingData || booking;
      const duration = currentBooking.tourDetails.duration || tourData.durations?.[0] || "7 days";

      // Convert meal plan to code format
      const mealPlanCode = mealPlanKeyMap[selectedMealPlan] || selectedMealPlan;

      const personalDetails = formData.guests.map((g) => ({
        firstName: g.firstName || "Guest",
        lastName: g.lastName || "",
      }));

      const updateData: UpdateBookingRequest = {
        date: selectedDate,
        duration: duration,
        mealPlan: mealPlanCode,
        guests: {
          adult: selectedAdults,
          children: selectedChildren,
        },
        personalDetails: personalDetails,
      };

      await updateBooking(currentBooking.id, updateData, token);
      
      onEditSuccess?.(currentBooking.id);
      onClose();
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError("Failed to update booking. Please try again.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} className="md:w-[600px] max-h-[95vh] flex flex-col">
      <form
        onSubmit={handleSubmit(onSubmit)}
        className="flex flex-col max-h-[95vh] overflow-hidden"
      >
        {/* Header */}
        <div className="p-6 pb-4 border-b border-grey-05">
          <h2 className="h2">Edit Booking</h2>
        </div>

        {/* Scrollable Content */}
        <div className="flex-1 overflow-y-auto p-6 pt-4">
        {(fetchingBooking || fetchingTour) ? (
          <div className="py-8 text-center">Loading booking details...</div>
        ) : error && !loading ? (
            <div className="text-sm p-4 bg-red-100 text-red-600 rounded-lg mb-4">
              {error}
            </div>
          ) : (
            <div className="flex flex-col gap-6">
            <div className="flex flex-col gap-2">
              <label className="body-bold">Tour</label>
              <p className="body text-blue-09">{(fullBookingData || booking).name}</p>
            </div>

              {/* Guest Name Fields */}
              <div className="flex flex-col gap-4">
                {fields.map((field, index) => (
                  <div key={field.id} className="flex flex-col gap-2">
                    <h3 className="h3">
                      {fields.length === 1
                        ? "Personal Details"
                        : `Personal Details (Guest ${index + 1})`}
                    </h3>
                    <div className="flex gap-4">
                      <Input
                        id={`firstName-${index}`}
                        {...register(`guests.${index}.firstName`)}
                        label="First name"
                        placeholder="Enter first name"
                        error={errors.guests?.[index]?.firstName?.message}
                      />
                      <Input
                        id={`lastName-${index}`}
                        {...register(`guests.${index}.lastName`)}
                        label="Last name"
                        placeholder="Enter last name"
                        error={errors.guests?.[index]?.lastName?.message}
                      />
                    </div>
                  </div>
                ))}
              </div>

              {/* Tour Details */}
              <div className="flex flex-col gap-4">
                <h3 className="h3">Tour details</h3>

                <div className="flex flex-col gap-2">
                  <label className="body-bold">Start Date</label>
                  <IconDropdown
                    key={selectedDate}
                    icon={<Calendar size={16} />}
                    options={createDateOptions()}
                    defaultValue={getCurrentDateOption()}
                    onSelect={handleDateSelect}
                    placeholder="Select date"
                  />
                </div>

                <div className="flex flex-col gap-2">
                  <label className="body-bold">Meal Plan</label>
                  <IconDropdown
                    key={selectedMealPlan}
                    icon={<Utensils size={16} />}
                    options={createMealPlanOptions()}
                    defaultValue={selectedMealPlan || "Select meal plan"}
                    onSelect={handleMealPlanSelect}
                    placeholder="Select meal plan"
                  />
                </div>

                <div className="flex flex-col gap-2">
                  <label className="body-bold">Number of Guests</label>
                  <div className="grid grid-cols-2 gap-4">
                    <IconDropdown
                      key={`adults-${selectedAdults}`}
                      icon={<UsersRound size={16} />}
                      options={createAdultOptions()}
                      defaultValue={`${selectedAdults} adult${selectedAdults > 1 ? "s" : ""}`}
                      onSelect={handleAdultsSelect}
                      placeholder="Adults"
                    />
                    <IconDropdown
                      key={`children-${selectedChildren}`}
                      icon={<UsersRound size={16} />}
                      options={createChildrenOptions()}
                      defaultValue={`${selectedChildren} child${selectedChildren !== 1 ? "ren" : ""}`}
                      onSelect={handleChildrenSelect}
                      placeholder="Children"
                    />
                  </div>
                </div>
              </div>

              {error && (
                <div className="text-sm p-4 bg-red-100 text-red-600 rounded-lg">
                  {error}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Fixed Footer with Buttons */}
        <div className="p-6 pt-4 border-t border-grey-05 bg-white">
          <div className="flex justify-end gap-2">
            <Button
              variant="secondary"
              onClick={() => onClose()}
              disabled={loading}
            >
              Cancel
            </Button>
            <button
              type="submit"
              disabled={loading || fetchingTour}
              className="px-4 py-2 bg-[#027EAC] hover:bg-[#015878] text-white border-0 disabled:opacity-50 disabled:cursor-not-allowed duration-200 cursor-pointer text-sm font-bold rounded-lg"
            >
              {loading ? "Saving..." : "Save Changes"}
            </button>
          </div>
        </div>
      </form>
    </Modal>
  );
};

export default EditTourModal;
