import {useCallback, useEffect, useState} from "react";
import Card from "../components/Card";
import Dropdown from "../components/Dropdown/Dropdown";
import Search from "../components/Search/Search.tsx";
import {selectSearchParameters} from "../store/selectSearchParameters.ts";
import {useAppSelector} from "../store/hooks/useAppSelector.ts";
import {fetchTours} from "../services/fetchTours.ts";
import {useDispatch} from "react-redux";
import type {RootState} from "../store/store.ts";
import {openModal} from "../store/modal/modalSlice.ts";
import {resetGuests, setGuests} from "../store/guests/guestsSlice.ts";
import BookingFormModal from "../components/modals/BookingFormModal/BookingFormModal.tsx";
import IsNotLoggedModal from "../components/modals/IsNotLoggedModal/IsNotLoggedModal.tsx";
import ConfirmReserveModal from "../components/modals/ConfirmReserveModal/ConfirmReserveModal.tsx";
import {resetDates} from "../store/dates/datesSlice.ts";
import {resetLocation} from "../store/location/locationSlice.ts";
import {resetMealOptions} from "../store/meal/mealSlice.ts";
import {resetTourTypes} from "../store/tourTypes/tourTypesSlice.ts";
import {selectHasActiveFilters} from "../store/selectHasActiveFilters.ts";

export type Tour = {
  id: string;
  name: string;
  destination: string;
  startDate: string;
  durations: string[];
  mealPlans: string[];
  price: string;
  rating: number;
  reviews: number;
  imageUrl: string;
  freeCancelation: string;
  tourType: string;
};

// type ApiResponse = {
//   tours: Tour[];
//   page: number;
//   pageSize: number;
//   totalPages: number;
//   totalItems: number;
// };

const MainPage = () => {
  const filters = useAppSelector(selectSearchParameters);
  const hasFilters = useAppSelector(selectHasActiveFilters);
  const dispatch = useDispatch();

  const user = useAppSelector((state: RootState) => state.user);
  const guests = useAppSelector((state: RootState) => state.guests);
  const [tours, setTours] = useState<Tour[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const getTours = useCallback(
    async (filtersParam?: ReturnType<typeof selectSearchParameters>) => {
      try {
        setLoading(true);
        setError(null);

        const data = await fetchTours(filtersParam);
        console.log(data);
        setTours(data.tours);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to fetch tours");
        console.error("Error fetching tours:", err);
      } finally {
        setLoading(false);
      }
    },
    []
  );

  useEffect(() => {
    getTours();
  }, [getTours]);

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", { month: "short", day: "numeric" });
  };

  const extractPrice = (priceString: string) => {
    const match = priceString.match(/from \$([\d.]+) for 1 person/);
    return match ? parseFloat(match[1]) : 0;
  };

  const formatMealPlans = (mealPlans: string[]) => {
    return mealPlans.join(", ");
  };

  const handleSortingTours = (selectedOption: string) => {
    if (selectedOption === "Top rated first") {
      setTours(prev => [...prev].sort((a, b) => b.rating - a.rating));
    }

    if (selectedOption === "Most popular") {
      setTours(prev => [...prev].sort((a, b) => b.reviews - a.reviews));
    }
  };

  const handleResetFilters = () => {
    dispatch(resetDates());
    dispatch(resetGuests());
    dispatch(resetLocation());
    dispatch(resetMealOptions());
    dispatch(resetTourTypes());
  }

  if (loading) {
    return (
      <div className="pt-10">
        <div className="flex justify-center items-center h-64">
          <p className="body">Loading tours...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="pt-10">
        <div className="flex justify-center items-center h-64">
          <p className="body text-red-04">Error: {error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="pt-10">
      <h1 className="h1 text-blue-09 text-center pb-6">
        Search for your next tour
      </h1>

      <Search onSearch={() => getTours(filters)}/>

      <div className={`flex items-center pb-8 ${hasFilters ? 'justify-between' : 'justify-end'}`}>
        {hasFilters &&
          <p
            className="font-bold underline cursor-pointer"
            onClick={handleResetFilters}
          >
            Clear all filters
          </p>
        }
        <div className='flex items-center gap-4'>
          <p className="body-bold">Sort by:</p>
          <Dropdown
            options={["Top rated first", "Most popular"]}
            onSelect={handleSortingTours}
          />
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {tours.map((tour) => (
          <Card
            id={tour.id}
            key={tour.id}
            title={tour.name}
            location={tour.destination}
            rating={tour.rating}
            reviewCount={tour.reviews}
            date={formatDate(tour.startDate)}
            duration={tour.durations}
            imageUrl={tour.imageUrl}
            boardType={formatMealPlans(tour.mealPlans)}
            priceFrom={extractPrice(tour.price)}
            currency="$"
            freeCancellationDate={
              tour.freeCancelation
                ? formatDate(tour.freeCancelation)
                : undefined
            }
            onBookTour={() => {
              if (!user.isAuth) {
                dispatch(openModal({ name: "IsNotLogged", props: {} }));
                return;
              }

              dispatch(
                setGuests({
                  adults: guests.adults,
                  children: guests.children,
                })
              );

              dispatch(
                openModal({
                  name: "bookingForm",
                  props: {
                    tourId: tour.id,
                    tourName: tour.name,
                    rating: tour.rating,
                    priceFrom: extractPrice(tour.price),
                    destination: tour.destination,
                    startDate: tour.startDate,
                    mealPlans: tour.mealPlans,
                    durations: tour.durations,
                  },
                })
              );
            }}
          />
        ))}
      </div>
      <BookingFormModal/>
      <IsNotLoggedModal/>
      <ConfirmReserveModal/>
    </div>
  );
};

export default MainPage;
