import { Star } from "lucide-react";
import { useState } from "react";

type StarRatingProps = {
  rating: number;
  setRating: (value: number) => void;
};

const StarRating = ({ rating, setRating }: StarRatingProps) => {
  const [hover, setHover] = useState(0);

  return (
    <div className="flex gap-4">
      {Array.from({ length: 5 }, (_, i) => (
        <Star
          key={i}
          size={32}
          fill={i < (hover || rating) ? "#0B3857" : "none"}
          stroke={i < (hover || rating) ? "#0B3857" : "#0B3857"}
          className="cursor-pointer transition-colors"
          onMouseEnter={() => setHover(i + 1)}
          onMouseLeave={() => setHover(0)}
          onClick={() => setRating(i + 1)}
        />
      ))}
    </div>
  );
};

export default StarRating;
