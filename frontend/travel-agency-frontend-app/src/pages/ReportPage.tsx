import { useState } from "react";
import ReportMenu from "../components/ReportMenu";
import ReportTable from "../components/ReportTable";
import Dropdown from "../components/Dropdown/Dropdown";

const mockReports = {
  STAFF_PERFORMANCE: {
    columns: [
      "Travel Agent",
      "TA e-mail",
      "Report period start",
      "Report period end",
      "Tours sold",
      "Delta of tours sold to previous period %",
      "Average Feedback rate",
      "Minimum Feedback rate",
      "Delta of Avg Feedback %",
      "Revenue (USD)",
      "Delta Revenue %",
    ],
    rows: [
      [
        "Diana Rossi",
        "DiRossi@sparkl.com",
        "15.09.2025",
        "22.09.2025",
        128,
        "-4%",
        3.7,
        2,
        "-10%",
        229120,
        "-3%",
      ],
      [
        "Lola Diaz",
        "LolaDiaz@sparkl.com",
        "05.08.2025",
        "02.09.2025",
        130,
        "+10%",
        4.5,
        3,
        "+5%",
        246500,
        "+5%",
      ],
    ],
  },
  SALES: {
    columns: [
      "Tour name",
      "Destination country",
      "Destination city",
      "Report period start",
      "Report period end",
      "Tours sold",
      "Delta of tours sold to previous period %",
      "Average Feedback rate (1 to 5)",
      "Minimum Feedback rate (1 to 5)",
      "Delta of Avg Feedback %",
      "Revenue (USD)",
      "Delta Revenue %",
    ],
    rows: [
      [
        "Sunny Beach Escape",
        "Spain",
        "Barcelona",
        "01.09.2025",
        "15.09.2025",
        240,
        "+12%",
        4.3,
        3,
        "+6%",
        480000,
        "+8%",
      ],
      [
        "Mountain Adventure",
        "Switzerland",
        "Zermatt",
        "10.08.2025",
        "20.08.2025",
        95,
        "-5%",
        4.7,
        4,
        "-3%",
        285000,
        "-2%",
      ],
      [
        "Cultural Journey",
        "Japan",
        "Tokyo",
        "05.09.2025",
        "12.09.2025",
        150,
        "+9%",
        4.5,
        3,
        "+4%",
        375000,
        "+7%",
      ],
    ],
  },
};

const parseDate = (str: string) => {
  const [day, month, year] = str.split(".").map(Number);
  return new Date(year, month - 1, day);
};

const ReportPage = () => {
  const [loading, setLoading] = useState(false);

  const [filters, setFilters] = useState<{
    type: string | null;
    startDate: string | null;
    endDate: string | null;
    location: string | null;
  }>({
    type: null,
    startDate: null,
    endDate: null,
    location: null,
  });

  const getFilteredData = () => {
    if (!filters.type) return null;

    const report = mockReports[filters.type as keyof typeof mockReports];
    if (!report) return null;

    const start = filters.startDate ? new Date(filters.startDate) : null;
    const end = filters.endDate ? new Date(filters.endDate) : null;

    const filteredRows = report.rows.filter((row) => {
      const dateStartIndex = filters.type === "SALES" ? 3 : 2;
      const dateEndIndex = filters.type === "SALES" ? 4 : 3;

      const rowStart = start ? parseDate(String(row[dateStartIndex])) : null;
      const rowEnd = end ? parseDate(String(row[dateEndIndex])) : null;

      const matchesDate =
        !start || !end || (rowEnd! >= start && rowStart! <= end);

      let matchesLocation = true;

      if (filters.location && filters.location.trim() !== "") {
        const search = filters.location.toLowerCase().trim();

        if (filters.type === "SALES") {
          const destinationCountry = String(row[1]).toLowerCase();
          const destinationCity = String(row[2]).toLowerCase();

          const [searchCity, searchCountry] = search
            .split(",")
            .map((s) => s.trim());

          if (searchCity && searchCountry) {
            matchesLocation =
              destinationCity.includes(searchCity) &&
              destinationCountry.includes(searchCountry);
          } else {
            matchesLocation =
              destinationCity.includes(search) ||
              destinationCountry.includes(search);
          }
        } else if (filters.type === "STAFF_PERFORMANCE") {
          const agentName = String(row[0]).toLowerCase();
          matchesLocation = agentName.includes(search);
        }
      }

      return matchesDate && matchesLocation;
    });

    return { ...report, rows: filteredRows };
  };

  //MOCK FUNCTION TO SIMULATE LOADING
  //I WILL REPLACE IT WITH ACTUAL API CALL LATER
  const handleGenerateReport = (newFilters: typeof filters) => {
    setLoading(true);
    setTimeout(() => {
      setFilters(newFilters);
      setLoading(false);
    }, 500);
  };

  const data = getFilteredData();

  return (
    <section className="mt-10 flex flex-col text-blue-09">
      <h2 className="text-2xl font-bold text-center mb-6">Generate a report</h2>
      <ReportMenu onGenerateReport={handleGenerateReport} />

      {loading ? (
        <p className="text-center mt-20 text-xl font-semibold">Loading...</p>
      ) : data ? (
        data.rows.length > 0 ? (
          <>
            <ReportTable data={data} />
            <Dropdown
              options={["Download PDF", "Download Excel", "Download CSV"]}
              placeholder="Download"
              className="self-end"
            />
          </>
        ) : (
          <p className="text-center block mt-20 text-2xl font-semibold">
            No reports found for the selected criteria.
          </p>
        )
      ) : null}
    </section>
  );
};

export default ReportPage;
