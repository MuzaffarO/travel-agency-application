import { ChevronsRight } from "lucide-react";

type PaginationProps = {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
};

const Pagination = ({
  currentPage,
  totalPages,
  onPageChange,
}: PaginationProps) => {
  const handleNext = () => {
    if (currentPage < totalPages) {
      onPageChange(currentPage + 1);
    }
  };

  return (
    <div className="flex items-center justify-center mt-6">
      <div className="flex items-center gap-2">
        {[...Array(Math.min(totalPages, 3))].map((_, index) => {
          const page = index + 1;
          return (
            <button
              key={page}
              onClick={() => onPageChange(page)}
              className={`w-6 h-7 flex items-center cursor-pointer justify-center transition-colors ${
                currentPage === page
                  ? "body-bold text-blue-09 border-b-2 border-blue-05"
                  : "body text-blue-09"
              }`}>
              {page}
            </button>
          );
        })}
        {totalPages > 3 && (
          <button
            onClick={handleNext}
            disabled={currentPage >= totalPages}
            className="flex items-center cursor-pointer justify-center disabled:opacity-50 disabled:cursor-not-allowed">
            <ChevronsRight className="text-blue-09" size={16} />
          </button>
        )}
      </div>
    </div>
  );
};

export default Pagination;
