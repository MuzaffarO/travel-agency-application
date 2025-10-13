import React from "react";
import defaultTourImage from "../../assets/default-tour.png";
import { useNavigate } from "react-router-dom";
import Button from "../Button";
import {
  MapPin,
  Calendar,
  Utensils,
  Wallet,
  Check,
  Star,
  Info,
} from "lucide-react";

type TourCardProps = {
  id: string;
  title: string;
  location: string;
  rating: number;
  reviewCount: number;
  date: string;
  duration: string[];
  boardType: string;
  priceFrom: number;
  currency?: string;
  freeCancellationDate?: string;
  imageUrl?: string;
  imageAlt?: string;
  onBookTour?: () => void;
};

const MyTourCard = ({
  id,
  title = "Dolomites: 7-day guided hike",
  location = "Dolomites, Italy",
  rating = 5.0,
  reviewCount = 19,
  date = "Jan 4",
  duration = ["7 days", "10 days", "12 days"],
  boardType = "Full-board(HB)",
  priceFrom = 1400,
  currency = "$",
  freeCancellationDate,
  imageUrl,
  imageAlt = "Tour card image",
  onBookTour,
}: TourCardProps) => {
  const hasCancellation = !!freeCancellationDate;
  const cancellationText = hasCancellation
    ? `Free cancellation until ${freeCancellationDate}`
    : "Free cancellation is no longer available";
  const cancellationIcon = hasCancellation ? Check : Info;
  const cancellationColor = hasCancellation ? "text-green-04" : "text-red-04";
  const navigate = useNavigate();

  return (
    <div className="rounded-xl bg-white shadow-card max-w-2xl p-6 gap-6 flex flex-col sm:flex-row">
      <div className="w-full h-[336px] max-w-[232px]">
        <img
          className="object-cover rounded-xl w-full h-full"
          src={imageUrl || defaultTourImage}
          alt={imageAlt}
        />
      </div>
      <div className="flex-1 flex flex-col">
        <div className="flex items-center justify-between gap-2 pb-6">
          <div>
            <p className="h3">{title}</p>
            <p className="caption text-[#677883] flex gap-1">
              <MapPin size={16} />
              {location}
            </p>
          </div>
          <div>
            <p className="caption text-blue-09 flex justify-end items-center gap-1 pb-1">
              <Star size={16} fill="#0b3857" />
              {rating.toFixed(1)}
            </p>
            <p className="caption text-blue-09">{reviewCount} reviews</p>
          </div>
        </div>
        <div className="flex flex-col gap-2">
          <p className="body text-blue-09 flex items-center gap-2">
            <Calendar size={16} />
            {date} ({duration.join(", ")})
          </p>
          <p className="body text-blue-09 flex items-center gap-2">
            <Utensils size={16} />
            {boardType}
          </p>
          <p className="body text-blue-09 flex items-center">
            <Wallet size={16} className="mr-2" />
            From{" "}
            <span className="body-bold px-1">
              {currency}
              {priceFrom}
            </span>{" "}
            for 1 person
          </p>
          <p className={`body ${cancellationColor} flex items-center gap-2`}>
            {React.createElement(cancellationIcon, { size: 16 })}
            {cancellationText}
          </p>
        </div>
        <div className="mt-auto flex justify-end gap-2">
          <Button variant="secondary" onClick={() => navigate(`/tours/${id}`)}>
            See details
          </Button>
          <Button onClick={onBookTour}>Book the tour</Button>
        </div>
      </div>
    </div>
  );
};

export default MyTourCard;
