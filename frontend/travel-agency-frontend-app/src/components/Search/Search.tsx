import Button from "../Button";
import DateDropdown from "../DateDropdown";
import GuestsDropdown from "../GuestsDropdown";
import LocationDropdown from "../LocationDropdown/LocationDropdown";
import MealDropdown from "../MealDropdown";
import TourTypeDropdown from "../TourTypeDropdown/TourTypeDropdown";

type SearchProps = {
  onSearch?: () => void;
}

const Search = ({onSearch}: SearchProps) => {

  return (
    <section className="flex gap-4 px-6 py-4 items-stretch bg-white rounded-xl mb-12 flex-wrap justify-center [&>*:not(:last-child)]:flex-1">
      <LocationDropdown />
      <DateDropdown />
      <GuestsDropdown />
      <MealDropdown  />
      <TourTypeDropdown />
      <Button onClick={onSearch}>
        Search
      </Button>
    </section>
  );
};

export default Search;
