import { Star } from "lucide-react";
import { useState, useEffect } from "react";
import axios from "axios";
import Pagination from "../components/Pagination";
import Dropdown from "../components/Dropdown/Dropdown";
import { BACK_URL } from "../constants";

type SortOption =
  | "RATING_DESC"
  | "RATING_ASC"
  | "NEWEST"
  | "OLDEST";

type SortOptionLabels = {
  [key in SortOption]: string;
};

const SORT_OPTION_LABELS: SortOptionLabels = {
  RATING_DESC: "Top rated first",
  RATING_ASC: "Low rated first",
  NEWEST: "Newest first",
  OLDEST: "Oldest first",
};

type Review = {
  authorName: string;
  authorImageUrl: string;
  createdAt: string;
  rate: number;
  reviewContent: string;
};

type ReviewsResponse = {
  reviews: Review[];
  page: number;
  pageSize: number;
  totalPages: number;
  totalItems: number;
};

type ReviewsProps = {
  tourId: string;
};

const Reviews = ({ tourId }: ReviewsProps) => {
  const [reviews, setReviews] = useState<Review[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const [currentPage, setCurrentPage] = useState<number>(1);
  const [totalPages, setTotalPages] = useState<number>(1);
  const [sortOption, setSortOption] = useState<SortOption>("RATING_DESC");

  useEffect(() => {
    const fetchReviews = async () => {
      try {
        setLoading(true);
        const response = await axios.get<ReviewsResponse>(
          `${BACK_URL}/tours/${tourId}/feedbacks`,
          {
            params: {
              page: currentPage,
              pageSize: 4,
              sortBy: sortOption,
            },
          }
        );
        setReviews(response.data.reviews);
        setTotalPages(response.data.totalPages);
      } catch (err) {
        setError(
          err instanceof Error ? err.message : "Failed to fetch reviews"
        );
      } finally {
        setLoading(false);
      }
    };
    if (tourId) {
      fetchReviews();
    }
  }, [tourId, currentPage, sortOption]);

  const handleSortingReviews = (selectedOption: string) => {
    setCurrentPage(1);
    switch (selectedOption) {
      case "Top rated first":
        setSortOption("RATING_DESC");
        break;
      case "Low rated first":
        setSortOption("RATING_ASC");
        break;
      case "Newest first":
        setSortOption("NEWEST");
        break;
      case "Oldest first":
        setSortOption("OLDEST");
        break;
      default:
        console.log("Unknown sort option:", selectedOption);
    }
  };

  const renderStars = (rating: number) => {
    return Array.from({ length: 5 }, (_, index) => (
      <Star
        key={index}
        size={16}
        fill={index < rating ? "#0b3857" : "none"}
        stroke="#0b3857"
      />
    ));
  };

  if (loading) {
    return <div className="text-center py-6">Loading reviews...</div>;
  }

  if (error) {
    return <div className="text-center py-6 text-red-500">Error: {error}</div>;
  }

  if (reviews.length === 0) {
    return <div className="text-center py-6">No reviews yet</div>;
  }

  return (
    <div>
      <div className="flex justify-between pb-4">
        <h2 className="h2 text-blue-09">Customer Reviews</h2>
        <div className="flex gap-4 justify-end items-center pb-8">
          <p className="body-bold text-blue-09">Sort by:</p>
          <Dropdown
            defaultValue={SORT_OPTION_LABELS[sortOption]}
            onSelect={handleSortingReviews}
            options={["Top rated first", "Low rated first", "Newest first", "Oldest first" ]}
          />
        </div>
      </div>

      <div className="flex gap-8 pb-6 flex-wrap">
        {reviews.map((review, index) => (
          <div
            key={index}
            className="rounded-xl min-h-[324px] w-[316px] max-w-[316px] bg-white p-6 shadow-card">
            <div className="pb-6 flex w-full items-center justify-between gap-3.5">
              <div className="w-16 h-16 rounded-full overflow-hidden">
                <img
                  src={review.authorImageUrl}
                  alt={review.authorName}
                  className="w-full h-full object-cover"
                />
              </div>
              <div className="flex-1 flex justify-between">
                <div>
                  <p className="body-bold text-blue-09 pb-1">
                    {review.authorName}
                  </p>
                  <p className="caption text-blue-09">
                    {new Date(review.createdAt).toLocaleDateString("en-US", {
                      month: "short",
                      day: "numeric",
                      year: "numeric",
                    })}
                  </p>
                </div>
                <div className="flex gap-1 text-blue-09">
                  {renderStars(review.rate)}
                </div>
              </div>
            </div>
            <p className="body text-blue-09">{review.reviewContent}</p>
          </div>
        ))}
      </div>

      {totalPages > 1 &&
          <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={(page: number) => setCurrentPage(page)}
          />
      }
    </div>
  );
};

export default Reviews;