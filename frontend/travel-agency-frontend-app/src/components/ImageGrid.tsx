import defaultTourImage from "../assets/default-tour.png";

type ImageGridProps = {
  imageUrls: string[];
};

const ImageGrid = ({ imageUrls }: ImageGridProps) => {
  const getImageUrl = (index: number) => {
    return imageUrls[index] || defaultTourImage;
  };

  return (
    <div className="bg-white shadow-card rounded-xl p-6">
      <div className="mx-auto">
        <div className="grid grid-cols-8 grid-rows-2 gap-3 h-[456px]">
          <div className="col-span-2 row-span-2 flex flex-col gap-3">
            <div className="flex-1 rounded-lg overflow-hidden shadow-lg">
              <img
                src={getImageUrl(0)}
                alt="Mountain landscape with green valley"
                className="w-full h-full object-cover"
              />
            </div>
            <div className="flex-1 rounded-lg overflow-hidden shadow-lg">
              <img
                src={getImageUrl(1)}
                alt="Alpine mountain peaks"
                className="w-full h-full object-cover"
              />
            </div>
          </div>
          <div className="col-span-2 rounded-lg row-span-2 overflow-hidden shadow-lg">
            <img
              src={getImageUrl(2)}
              alt="Hikers on mountain ridge"
              className="w-full h-full object-cover"
            />
          </div>
          <div className="col-span-2 row-span-2 grid grid-cols-2 gap-3">
            <div className="rounded-lg overflow-hidden shadow-lg">
              <img
                src={getImageUrl(3)}
                alt="Mountain path"
                className="w-full h-full object-cover"
              />
            </div>
            <div className="rounded-lg overflow-hidden shadow-lg">
              <img
                src={getImageUrl(4)}
                alt="Mountain lake reflection"
                className="w-full h-full object-cover"
              />
            </div>
            <div className="col-span-2 rounded-lg overflow-hidden shadow-lg">
              <img
                src={getImageUrl(5)}
                alt="Mountain valley panorama"
                className="w-full h-full object-cover"
              />
            </div>
          </div>
          <div className="row-span-2 rounded-lg overflow-hidden col-span-2">
            <img
              src={getImageUrl(6)}
              alt="Mountain valley panorama"
              className="w-full h-full object-cover"
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default ImageGrid;
