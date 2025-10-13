import { Tooltip as ReactTooltip } from "react-tooltip";
import "react-tooltip/dist/react-tooltip.css";

const HeaderCellWithTooltip = ({ text }: { text: string }) => (
  <th className="table-header-cell relative max-w-[180px]">
    <div
      className="truncate cursor-pointer"
      data-tooltip-id="global-tooltip"
      data-tooltip-content={text}
    >
      {text}
    </div>
  </th>
);

type ReportTableProps = {
  data: {
    columns: string[];
    rows: (string | number)[][];
  };
};

const ReportTable = ({ data }: ReportTableProps) => {
  const { columns, rows } = data;

  return (
    <section className="mt-10 mb-2 overflow-x-scroll">
      <h2 className="text-2xl font-bold">Report</h2>
      <table className="w-full mt-6 text-left border-collapse border border-grey-05">
        <thead>
          <tr>
            {columns.map((col, index) => (
              <HeaderCellWithTooltip key={index} text={col} />
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, rowIndex) => (
            <tr key={rowIndex}>
              {row.map((cell, cellIndex) => (
                <td key={cellIndex} className="table-body-cell">
                  {cell}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>

      <ReactTooltip
        id="global-tooltip"
        place="bottom"
        className="max-w-60 text-sm !z-[9999]"
        style={{
          zIndex: 9999,
          backgroundColor: "#fff",
          color: "var(--color-blue-09)",
          boxShadow: "0px 4px 6px rgba(0, 0, 0, 0.3)",
        }}
      />
    </section>
  );
};

export default ReportTable;
