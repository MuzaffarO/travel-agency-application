import { useState } from "react";
import Button from "../Button";
import DateDropdown from "../DateDropdown";
import LocationDropdown from "../LocationDropdown/LocationDropdown";
import ReportTypeSelector from "../ReportTypeSelector";
import { useAppSelector } from "../../store/hooks/useAppSelector";

type ReportMenuProps = {
  onGenerateReport: (filters: {
    type: string | null;
    startDate: string | null;
    location: string | null;
    endDate: string | null;
  }) => void;
};

const ReportMenu = ({ onGenerateReport }: ReportMenuProps) => {
  const [type, setType] = useState<string | null>(null);

  const { startDate, endDate } = useAppSelector((state) => state.dates);

  const selectedLocation = useAppSelector(
    (state) => state.location.selectedLocation
  );

  return (
    <section className="grid grid-cols-1 md:grid-cols-4 gap-4 px-4 py-6 bg-white rounded-[8px]">
      <ReportTypeSelector onChange={(val) => setType(val)} />
      <DateDropdown
        noIcon
        customLabel="Select Period"
        showDuration={false}
        allowPastDates={true}
      />
      <LocationDropdown noIcon customLabel="Select Location" />
      <Button
        className="text-sm font-light"
        onClick={() =>
          onGenerateReport({
            type,
            startDate,
            endDate,
            location: selectedLocation,
          })
        }
      >
        Generate Report
      </Button>
    </section>
  );
};

export default ReportMenu;
