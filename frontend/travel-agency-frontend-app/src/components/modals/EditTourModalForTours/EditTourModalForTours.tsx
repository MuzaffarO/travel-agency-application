import { useState, useEffect } from "react";
import Modal from "../../../ui/Modal";
import Button from "../../Button";
import Input from "../../../ui/Input";
import Textarea from "../../../ui/Textarea";
import { updateTour, type UpdateTourRequest } from "../../../services/updateTour";
import { Plus, X } from "lucide-react";
import type { Tour } from "../../../pages/MainPage";
import axios from "axios";
import { BACK_URL } from "../../../constants";

type EditTourModalProps = {
  tour: Tour;
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: () => void;
};

type TourDetailData = {
  id: string;
  name: string;
  destination: string;
  startDates: string[];
  durations: string[];
  mealPlans: string[];
  price: Record<string, string>;
  mealSupplementsPerDay: Record<string, string>;
  tourType?: string;
  guestQuantity: {
    adultsMaxValue: number;
    childrenMaxValue: number;
  };
  imageUrls?: string[];
  summary?: string;
  accommodation?: string;
  hotelName?: string;
  hotelDescription?: string;
  customDetails?: Record<string, string>;
  freeCancellation?: string;
  freeCancellationDaysBefore?: number;
};

const EditTourModal: React.FC<EditTourModalProps> = ({
  tour,
  isOpen,
  onClose,
  onSuccess,
}) => {
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [tourData, setTourData] = useState<TourDetailData | null>(null);
  
  const [formData, setFormData] = useState<UpdateTourRequest>({
    name: "",
    destination: "",
    startDates: [],
    durations: [],
    mealPlans: [],
    priceByDuration: {},
    mealSupplementsPerDay: {},
    maxAdults: 1,
    maxChildren: 0,
    availablePackages: 1,
    tourType: "",
    imageUrls: [],
    summary: "",
    accommodation: "",
    hotelName: "",
    hotelDescription: "",
    customDetails: {},
    freeCancellation: "",
    freeCancellationDaysBefore: 0,
  });

  useEffect(() => {
    if (isOpen && tour?.id) {
      setFetching(true);
      axios
        .get<TourDetailData>(`${BACK_URL}/tours/${tour.id}`)
        .then((response) => {
          setTourData(response.data);
          const data = response.data;
          
          // Convert price strings to numbers
          const priceByDuration: Record<string, number> = {};
          Object.entries(data.price || {}).forEach(([key, value]) => {
            const num = parseFloat(value.replace("$", "").replace(",", ""));
            if (!isNaN(num)) priceByDuration[key] = num;
          });

          // Convert meal supplement strings to numbers
          const mealSupplementsPerDay: Record<string, number> = {};
          Object.entries(data.mealSupplementsPerDay || {}).forEach(([key, value]) => {
            const num = parseFloat(value.replace("$", "").replace(",", ""));
            if (!isNaN(num)) mealSupplementsPerDay[key] = num;
          });

          // Convert meal plans from display format to codes (e.g., "Breakfast (BB)" -> "BB")
          const mealPlanCodeMap: Record<string, string> = {
            "Breakfast (BB)": "BB",
            "Half-board (HB)": "HB",
            "Full-board (FB)": "FB",
            "All inclusive (AI)": "AI",
          };
          const mealPlanCodes = (data.mealPlans || []).map((mp: string) => {
            // If it's already a code, use it; otherwise convert from display format
            if (["BB", "HB", "FB", "AI"].includes(mp)) return mp;
            return mealPlanCodeMap[mp] || mp;
          });

          setFormData({
            name: data.name,
            destination: data.destination,
            startDates: data.startDates || [],
            durations: data.durations || [],
            mealPlans: mealPlanCodes,
            priceByDuration,
            mealSupplementsPerDay,
            maxAdults: data.guestQuantity?.adultsMaxValue || 1,
            maxChildren: data.guestQuantity?.childrenMaxValue || 0,
            tourType: data.tourType || "",
            imageUrls: data.imageUrls || [],
            summary: data.summary || "",
            accommodation: data.accommodation || "",
            hotelName: data.hotelName || "",
            hotelDescription: data.hotelDescription || "",
            customDetails: data.customDetails || {},
            freeCancellation: data.freeCancellation || "",
            freeCancellationDaysBefore: data.freeCancellationDaysBefore || 0,
          });
        })
        .catch((err) => {
          console.error("Failed to fetch tour data", err);
          setError("Failed to load tour details");
        })
        .finally(() => {
          setFetching(false);
        });
    }
  }, [isOpen, tour?.id]);

  const [newStartDate, setNewStartDate] = useState("");
  const [newDuration, setNewDuration] = useState("");
  const [newDurationPrice, setNewDurationPrice] = useState("");
  const [newMealPlan, setNewMealPlan] = useState("");
  const [newMealPlanPrice, setNewMealPlanPrice] = useState("");
  const [newImageUrl, setNewImageUrl] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    // Filter out any empty strings from arrays
    const cleanedStartDates = (formData.startDates || []).filter(date => date && date.trim().length > 0);
    const cleanedDurations = (formData.durations || []).filter(duration => duration && duration.trim().length > 0);
    const cleanedMealPlans = (formData.mealPlans || []).filter(plan => plan && plan.trim().length > 0);

    if (cleanedStartDates.length === 0) {
      setError("At least one start date is required");
      setLoading(false);
      return;
    }
    if (cleanedDurations.length === 0) {
      setError("At least one duration is required");
      setLoading(false);
      return;
    }
    if (cleanedMealPlans.length === 0) {
      setError("At least one meal plan is required");
      setLoading(false);
      return;
    }

    setLoading(true);
    try {
      const storedUser = localStorage.getItem("user");
      if (!storedUser) {
        throw new Error("User not found");
      }

      const user = JSON.parse(storedUser) as { token?: string };
      const token = user.token;

      if (!token) {
        throw new Error("Token not found");
      }

      await updateTour(tour.id, {
        ...formData,
        startDates: cleanedStartDates,
        durations: cleanedDurations,
        mealPlans: cleanedMealPlans,
      }, token);
      onSuccess?.();
      onClose();
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError("Failed to update tour. Please try again.");
      }
    } finally {
      setLoading(false);
    }
  };

  const addStartDate = () => {
    const trimmedDate = newStartDate.trim();
    // Validate ISO date format (YYYY-MM-DD)
    if (trimmedDate && /^\d{4}-\d{2}-\d{2}$/.test(trimmedDate) && !formData.startDates?.includes(trimmedDate)) {
      // Validate it's a valid date
      const date = new Date(trimmedDate);
      if (!isNaN(date.getTime())) {
        setFormData({
          ...formData,
          startDates: [...(formData.startDates || []), trimmedDate],
        });
        setNewStartDate("");
        setError(null);
      } else {
        setError("Please enter a valid date");
      }
    } else if (trimmedDate && !/^\d{4}-\d{2}-\d{2}$/.test(trimmedDate)) {
      setError("Date must be in YYYY-MM-DD format");
    }
  };

  const removeStartDate = (date: string) => {
    setFormData({
      ...formData,
      startDates: formData.startDates?.filter((d) => d !== date) || [],
    });
  };

  const addDuration = () => {
    const trimmedDuration = newDuration.trim();
    // Validate duration format
    if (trimmedDuration && trimmedDuration.length > 0 && !formData.durations?.includes(trimmedDuration)) {
      const price = parseFloat(newDurationPrice) || 0;
      if (price <= 0) {
        setError("Duration price must be greater than 0");
        return;
      }
      setFormData({
        ...formData,
        durations: [...(formData.durations || []), trimmedDuration],
        priceByDuration: {
          ...(formData.priceByDuration || {}),
          [trimmedDuration]: price,
        },
      });
      setNewDuration("");
      setNewDurationPrice("");
      setError(null); // Clear any previous errors
    } else if (trimmedDuration && formData.durations?.includes(trimmedDuration)) {
      setError("This duration is already added");
    }
  };

  const removeDuration = (duration: string) => {
    const newPriceByDuration = { ...(formData.priceByDuration || {}) };
    delete newPriceByDuration[duration];
    setFormData({
      ...formData,
      durations: formData.durations?.filter((d) => d !== duration) || [],
      priceByDuration: newPriceByDuration,
    });
  };

  const addMealPlan = () => {
    // Ensure we only use the code (BB, HB, FB, AI), not the display format
    const mealPlanCode = newMealPlan.trim().toUpperCase();
    if (mealPlanCode && ["BB", "HB", "FB", "AI"].includes(mealPlanCode) && !formData.mealPlans?.includes(mealPlanCode)) {
      const price = parseFloat(newMealPlanPrice) || 0;
      setFormData({
        ...formData,
        mealPlans: [...(formData.mealPlans || []), mealPlanCode],
        mealSupplementsPerDay: {
          ...(formData.mealSupplementsPerDay || {}),
          [mealPlanCode]: price,
        },
      });
      setNewMealPlan("");
      setNewMealPlanPrice("");
    }
  };

  const removeMealPlan = (mealPlan: string) => {
    const newMealSupplements = { ...(formData.mealSupplementsPerDay || {}) };
    delete newMealSupplements[mealPlan];
    setFormData({
      ...formData,
      mealPlans: formData.mealPlans?.filter((m) => m !== mealPlan) || [],
      mealSupplementsPerDay: newMealSupplements,
    });
  };

  const addImageUrl = () => {
    if (newImageUrl && !formData.imageUrls?.includes(newImageUrl)) {
      setFormData({
        ...formData,
        imageUrls: [...(formData.imageUrls || []), newImageUrl],
      });
      setNewImageUrl("");
    }
  };

  const removeImageUrl = (url: string) => {
    setFormData({
      ...formData,
      imageUrls: formData.imageUrls?.filter((u) => u !== url) || [],
    });
  };

  if (fetching) {
    return (
      <Modal isOpen={isOpen} onClose={onClose}>
        <div className="p-6">Loading tour details...</div>
      </Modal>
    );
  }

  return (
    <Modal isOpen={isOpen} onClose={onClose} className="md:w-[800px] max-h-[95vh] flex flex-col">
      <form onSubmit={handleSubmit} className="flex flex-col max-h-[95vh] overflow-hidden">
        <div className="p-6 pb-4 border-b border-grey-05">
          <h2 className="h2">Edit Tour</h2>
        </div>

        <div className="flex-1 overflow-y-auto p-6 pt-4">
          {error && (
            <div className="text-sm p-4 bg-red-100 text-red-600 rounded-lg mb-4">
              {error}
            </div>
          )}

          <div className="flex flex-col gap-6">
            <Input
              label="Tour Name"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="Enter tour name"
            />

            <Input
              label="Destination"
              value={formData.destination}
              onChange={(e) => setFormData({ ...formData, destination: e.target.value })}
              placeholder="Enter destination"
            />

            <div className="flex flex-col gap-2">
              <label className="body-bold">Start Dates</label>
              <p className="caption text-[#677883]">Select dates in YYYY-MM-DD format</p>
              <div className="flex gap-2">
                <Input
                  type="date"
                  value={newStartDate}
                  onChange={(e) => setNewStartDate(e.target.value)}
                  placeholder="Add start date"
                />
                <Button type="button" onClick={addStartDate} disabled={!newStartDate}>
                  <Plus size={16} />
                </Button>
              </div>
              <div className="flex flex-wrap gap-2 mt-2">
                {formData.startDates?.map((date) => (
                  <span
                    key={date}
                    className="px-3 py-1 bg-blue-03 rounded-lg flex items-center gap-2"
                  >
                    {date}
                    <button
                      type="button"
                      onClick={() => removeStartDate(date)}
                      className="text-red-600"
                    >
                      <X size={14} />
                    </button>
                  </span>
                ))}
              </div>
            </div>

            <div className="flex flex-col gap-2">
              <label className="body-bold">Durations</label>
              <p className="caption text-[#677883]">Format: "7 days", "10 days", "14 days", etc.</p>
              <div className="flex gap-2">
                <Input
                  value={newDuration}
                  onChange={(e) => setNewDuration(e.target.value)}
                  placeholder="e.g., 7 days"
                />
                <Input
                  type="number"
                  value={newDurationPrice}
                  onChange={(e) => setNewDurationPrice(e.target.value)}
                  placeholder="Price ($)"
                  min="0"
                  step="0.01"
                />
                <Button type="button" onClick={addDuration} disabled={!newDuration.trim() || !newDurationPrice}>
                  <Plus size={16} />
                </Button>
              </div>
              <div className="flex flex-wrap gap-2 mt-2">
                {formData.durations?.map((duration) => (
                  <span
                    key={duration}
                    className="px-3 py-1 bg-blue-03 rounded-lg flex items-center gap-2"
                  >
                    {duration} (${formData.priceByDuration?.[duration] || 0})
                    <button
                      type="button"
                      onClick={() => removeDuration(duration)}
                      className="text-red-600"
                    >
                      <X size={14} />
                    </button>
                  </span>
                ))}
              </div>
            </div>

            <div className="flex flex-col gap-2">
              <label className="body-bold">Meal Plans</label>
              <div className="flex gap-2">
                <select
                  value={newMealPlan}
                  onChange={(e) => setNewMealPlan(e.target.value)}
                  className="px-3 py-2 border border-grey-05 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-09"
                >
                  <option value="">Select meal plan</option>
                  <option value="BB">Breakfast (BB)</option>
                  <option value="HB">Half-board (HB)</option>
                  <option value="FB">Full-board (FB)</option>
                  <option value="AI">All inclusive (AI)</option>
                </select>
                <Input
                  type="number"
                  value={newMealPlanPrice}
                  onChange={(e) => setNewMealPlanPrice(e.target.value)}
                  placeholder="Price per day"
                />
                <Button type="button" onClick={addMealPlan} disabled={!newMealPlan}>
                  <Plus size={16} />
                </Button>
              </div>
              <div className="flex flex-wrap gap-2 mt-2">
                {formData.mealPlans?.map((mealPlan) => {
                  const mealPlanLabels: Record<string, string> = {
                    BB: "Breakfast (BB)",
                    HB: "Half-board (HB)",
                    FB: "Full-board (FB)",
                    AI: "All inclusive (AI)",
                  };
                  return (
                    <span
                      key={mealPlan}
                      className="px-3 py-1 bg-blue-03 rounded-lg flex items-center gap-2"
                    >
                      {mealPlanLabels[mealPlan] || mealPlan} (${formData.mealSupplementsPerDay?.[mealPlan] || 0}/day)
                      <button
                        type="button"
                        onClick={() => removeMealPlan(mealPlan)}
                        className="text-red-600"
                      >
                        <X size={14} />
                      </button>
                    </span>
                  );
                })}
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <Input
                label="Tour Type"
                value={formData.tourType}
                onChange={(e) => setFormData({ ...formData, tourType: e.target.value })}
                placeholder="e.g., City Break"
              />
              <Input
                label="Available Packages"
                type="number"
                value={formData.availablePackages}
                onChange={(e) => setFormData({ ...formData, availablePackages: parseInt(e.target.value) || 1 })}
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <Input
                label="Max Adults"
                type="number"
                value={formData.maxAdults}
                onChange={(e) => setFormData({ ...formData, maxAdults: parseInt(e.target.value) || 1 })}
              />
              <Input
                label="Max Children"
                type="number"
                value={formData.maxChildren}
                onChange={(e) => setFormData({ ...formData, maxChildren: parseInt(e.target.value) || 0 })}
              />
            </div>

            <div className="flex flex-col gap-2">
              <label className="body-bold">Image URLs</label>
              <div className="flex gap-2">
                <Input
                  value={newImageUrl}
                  onChange={(e) => setNewImageUrl(e.target.value)}
                  placeholder="Add image URL"
                />
                <Button type="button" onClick={addImageUrl}>
                  <Plus size={16} />
                </Button>
              </div>
              <div className="flex flex-wrap gap-2 mt-2">
                {formData.imageUrls?.map((url) => (
                  <span
                    key={url}
                    className="px-3 py-1 bg-blue-03 rounded-lg flex items-center gap-2"
                  >
                    {url.substring(0, 30)}...
                    <button
                      type="button"
                      onClick={() => removeImageUrl(url)}
                      className="text-red-600"
                    >
                      <X size={14} />
                    </button>
                  </span>
                ))}
              </div>
            </div>

            <Textarea
              label="Summary"
              value={formData.summary}
              onChange={(e) => setFormData({ ...formData, summary: e.target.value })}
              placeholder="Enter tour summary"
            />

            <Textarea
              label="Accommodation"
              value={formData.accommodation}
              onChange={(e) => setFormData({ ...formData, accommodation: e.target.value })}
              placeholder="Enter accommodation details"
            />

            <Input
              label="Hotel Name"
              value={formData.hotelName}
              onChange={(e) => setFormData({ ...formData, hotelName: e.target.value })}
              placeholder="Enter hotel name"
            />

            <Textarea
              label="Hotel Description"
              value={formData.hotelDescription}
              onChange={(e) => setFormData({ ...formData, hotelDescription: e.target.value })}
              placeholder="Enter hotel description"
            />

            <div className="grid grid-cols-2 gap-4">
              <Input
                label="Free Cancellation"
                value={formData.freeCancellation}
                onChange={(e) => setFormData({ ...formData, freeCancellation: e.target.value })}
                placeholder="e.g., Free cancellation"
              />
              <Input
                label="Free Cancellation Days Before"
                type="number"
                value={formData.freeCancellationDaysBefore}
                onChange={(e) => setFormData({ ...formData, freeCancellationDaysBefore: parseInt(e.target.value) || 0 })}
                placeholder="0"
              />
            </div>
          </div>
        </div>

        <div className="p-6 pt-4 border-t border-grey-05 bg-white">
          <div className="flex justify-end gap-2">
            <Button variant="secondary" onClick={onClose} disabled={loading}>
              Cancel
            </Button>
            <button
              type="submit"
              disabled={loading}
              className="px-4 py-2 bg-[#027EAC] hover:bg-[#015878] text-white border-0 disabled:opacity-50 disabled:cursor-not-allowed duration-200 cursor-pointer text-sm font-bold rounded-lg"
            >
              {loading ? "Updating..." : "Update Tour"}
            </button>
          </div>
        </div>
      </form>
    </Modal>
  );
};

export default EditTourModal;

