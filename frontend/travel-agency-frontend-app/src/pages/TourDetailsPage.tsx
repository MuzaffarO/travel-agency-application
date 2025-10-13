import { Calendar, MapPin, Star, UsersRound, Utensils } from "lucide-react";
import { useParams } from "react-router-dom";
import { useState, useEffect } from "react";
import axios from "axios";
import { toast } from "react-hot-toast";
import ImageGrid from "../components/ImageGrid";
import Button from "../components/Button";
import IconDropdown from "../components/IconDropdown";
import Reviews from "../components/Reviews";
import SuccessToast from "../ui/SuccessToast";
import { BACK_URL } from "../constants";
import { useNavigate } from "react-router-dom";

type TourData = {
  id: string;
  name: string;
  destination: string;
  rating: number;
  reviews: number;
  imageUrls: string[];
  summary: string;
  freeCancellationDaysBefore: number;
  durations: string[];
  accommodation: string;
  hotelName: string;
  hotelDescription: string;
  mealPlans: string[];
  customDetails: { [key: string]: string };
  startDates: string[];
  guestQuantity: {
    adultsMaxValue: number;
    childrenMaxValue: number;
    totalMaxValue: number;
  };
  price: { [duration: string]: string };
  mealSupplementsPerDay: { [mealPlan: string]: string };
};

const TourDetails = () => {
  const { id } = useParams<{ id: string }>();

  const [tourData, setTourData] = useState<TourData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const [selectedStartDate, setSelectedStartDate] = useState<string>("");
  const [selectedDuration, setSelectedDuration] = useState<string>("");
  const [selectedAdults, setSelectedAdults] = useState<number>(1);
  const [selectedChildren, setSelectedChildren] = useState<number>(0);
  const [selectedMealPlan, setSelectedMealPlan] = useState<string>("");

  const navigate = useNavigate();

  const mealPlanKeyMap: Record<string, string> = {
    "Breakfast (BB)": "BB",
    "Half-board (HB)": "HB",
    "Full-board (FB)": "FB",
    "All inclusive (AI)": "AI",
  };

  useEffect(() => {
    const fetchTourData = async () => {
      try {
        setLoading(true);
        const response = await axios.get<TourData>(`${BACK_URL}/tours/${id}`);
        setTourData(response.data);

        if (response.data) {
          setSelectedStartDate(response.data.startDates?.[0] || "");
          setSelectedDuration(response.data.durations?.[0] || "");
          setSelectedMealPlan(response.data.mealPlans?.[0] || "");
        }
      } catch (err) {
        setError(
          err instanceof Error ? err.message : "Failed to fetch tour data"
        );
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchTourData();
    }
  }, [id]);

  const calculateTotalPrice = () => {
    if (!tourData || !selectedDuration || !selectedMealPlan) return "N/A";

    const basePrice = parseFloat(
      tourData.price[selectedDuration]?.replace("$", "") || "0"
    );

    const mealKey = mealPlanKeyMap[selectedMealPlan] || selectedMealPlan;

    const mealSupplement = parseFloat(
      tourData.mealSupplementsPerDay[mealKey]?.replace("$", "") || "0"
    );

    const durationDays = parseInt(selectedDuration.split(" ")[0]) || 0;
    const mealSupplementTotal = mealSupplement * durationDays;

    const perPersonTotal = basePrice + mealSupplementTotal;
    const totalGuests = selectedAdults + selectedChildren;

    const totalPrice = perPersonTotal * totalGuests;

    return `$${totalPrice.toFixed(0)}`;
  };

  const createDateDurationOptions = () => {
    if (!tourData) return [];

    const options: string[] = [];
    tourData.startDates?.forEach((date) => {
      tourData.durations?.forEach((duration) => {
        const formattedDate = new Date(date).toLocaleDateString("en-US", {
          month: "short",
          day: "numeric",
        });
        options.push(`${formattedDate}, ${duration}`);
      });
    });
    return options;
  };

  const handleDateDurationChange = (value: string) => {
    const [formattedDateStr, duration] = value.split(", ");

    const selectedDate = tourData?.startDates?.find((date) => {
      const formatted = new Date(date).toLocaleDateString("en-US", {
        month: "short",
        day: "numeric",
      });
      return formatted === formattedDateStr;
    });

    if (selectedDate && duration) {
      setSelectedStartDate(selectedDate);
      setSelectedDuration(duration);
    }
  };

  const createAdultOptions = () => {
    if (!tourData) return ["1 adult"];
    return Array.from(
      { length: tourData.guestQuantity?.adultsMaxValue || 1 },
      (_, i) => `${i + 1} adult${i > 0 ? "s" : ""}`
    );
  };

  const createChildrenOptions = () => {
    if (!tourData) return ["0 children"];
    return Array.from(
      { length: (tourData.guestQuantity?.childrenMaxValue || 0) + 1 },
      (_, i) => `${i} child${i !== 1 ? "ren" : ""}`
    );
  };

  const handleBooking = async () => {
    if (
      !tourData ||
      !selectedStartDate ||
      !selectedDuration ||
      !selectedMealPlan
    ) {
      toast.error("Please fill in all fields.");
      return;
    }

    const token = localStorage.user
      ? JSON.parse(localStorage.user).token
      : null;

    if (!token) {
      toast.error("You must be logged in to book a tour.");
      return;
    }

    const bookingData = {
      tourId: tourData.id,
      date: selectedStartDate,
      duration: selectedDuration,
      mealPlan: mealPlanKeyMap[selectedMealPlan] || selectedMealPlan,
      guests: {
        adult: selectedAdults,
        children: selectedChildren,
      },
      personalDetails: [
        {
          firstName: "John",
          lastName: "Doe",
        },
      ],
    };

    console.log(bookingData);
    console.log(token);

    try {
      await axios.post(`${BACK_URL}/bookings`, bookingData, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      navigate("/");

      toast.custom(
        (t) => (
          <SuccessToast
            t={t}
            title="Booking Confirmed"
            message="Your tour has been booked successfully. We’ll contact you soon."
          />
        ),
        { duration: 5000 }
      );
    } catch {
      toast.error("Booking failed. Please try again.");
    }
  };

  if (loading) {
    return <div className="pt-10 text-center">Loading tour details...</div>;
  }

  if (error) {
    return <div className="pt-10 text-center text-red-500">Error: {error}</div>;
  }

  if (!tourData) {
    return <div className="pt-10 text-center">Tour not found</div>;
  }

  return (
    <div className="pt-10">
      <div className="flex justify-between pb-10">
        <div>
          <h1 className="h1 pb-2 text-blue-09">{tourData.name}</h1>
          <p className="caption text-grey-07 flex gap-1">
            <MapPin size={16} />
            {tourData.destination}
          </p>
        </div>
        <p className="h1 text-blue-09 flex items-center gap-1">
          <Star size={16} fill="#0b3857" />
          {tourData.rating || "N/A"}
        </p>
      </div>
      <ImageGrid imageUrls={tourData.imageUrls} />
      <div className="mt-10 flex gap-10 justify-between items-start pb-10">
        <div className="rounded-xl flex-5 shadow-card bg-white p-6">
          <p className="body-bold text-blue-09 pb-4">{tourData.summary}</p>
          <h2 className="h2 pb-4 text-blue-09">About the tour</h2>
          <p className="body-bold text-blue-09">
            Free cancellation until {tourData.freeCancellationDaysBefore} days
            before
          </p>
          <p className="body text-blue-09 pb-3">
            Cancel your booking up to {tourData.freeCancellationDaysBefore} days
            before the tour starts
          </p>
          <p className="body-bold text-blue-09 pb-3">
            Duration - {tourData.durations?.join(", ")}
          </p>
          <p className="body-bold text-blue-09">Accommodation</p>
          <p className="body text-blue-09 pb-3">
            {tourData.accommodation}
            {tourData.hotelName && ` Stay at ${tourData.hotelName}.`}
            {tourData.hotelDescription && ` ${tourData.hotelDescription}`}
          </p>
          <p className="body-bold text-blue-09">Meal plans</p>
          <p className="body text-blue-09 pb-3">
            {tourData.mealPlans?.join(", ")}
          </p>
          {Object.keys(tourData.customDetails || {}).map((key) => (
            <div key={key} className="pb-3">
              <p className="body-bold text-blue-09">{key}</p>
              <p className="body text-blue-09">{tourData.customDetails[key]}</p>
            </div>
          ))}
        </div>
        <div className="flex-3 bg-white rounded-xl shadow-card p-6 flex flex-col gap-3">
          <IconDropdown
            options={createDateDurationOptions()}
            defaultValue={
              selectedStartDate && selectedDuration
                ? `${new Date(selectedStartDate).toLocaleDateString("en-US", {
                    month: "short",
                    day: "numeric",
                  })}, ${selectedDuration}`
                : "Select date and duration"
            }
            placeholder="Select date and duration"
            icon={<Calendar className="text-blue-09" />}
            onSelect={handleDateDurationChange}
          />
          <IconDropdown
            options={createAdultOptions()}
            defaultValue="1 adult"
            placeholder="Select adults"
            icon={<UsersRound className="text-blue-09" />}
            onSelect={(value) =>
              setSelectedAdults(parseInt(value.split(" ")[0]))
            }
          />
          <IconDropdown
            options={createChildrenOptions()}
            defaultValue="0 children"
            placeholder="Select children"
            icon={<UsersRound className="text-blue-09" />}
            onSelect={(value) =>
              setSelectedChildren(parseInt(value.split(" ")[0]))
            }
          />
          <IconDropdown
            options={tourData.mealPlans || ["Breakfast (BB)"]}
            defaultValue={tourData.mealPlans?.[0] || "Breakfast (BB)"}
            placeholder="Select meal plan"
            icon={<Utensils className="text-blue-09" />}
            onSelect={setSelectedMealPlan}
          />
          <div className="self-end text-right">
            <p className="body-bold text-blue-09 pb-5">
              Total price: {calculateTotalPrice()}
            </p>
          </div>
          <Button className="w-full" onClick={handleBooking}>
            Book the tour
          </Button>
        </div>
      </div>
      <Reviews tourId={tourData.id} />
    </div>
  );
};

export default TourDetails;
